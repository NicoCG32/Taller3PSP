# Diagrama de arquitectura

Este diagrama resume el flujo principal del taller. La aplicacion se organiza como un pipeline **Pipe & Filter** donde cada filtro se implementa como un `Verticle` y se comunica mediante canales del `EventBus`.

```mermaid
flowchart LR
    Usuario([Estudiante / evaluador])

    subgraph App["Aplicacion Java Vert.x"]
        Main["MainVerticle<br/>Arranque"]
        Ingress["OrderIngressVerticle<br/>Orden JSON de demo"]
        Bus(("EventBus<br/>Pipes asincronicos"))
        Validation["ValidationFilterVerticle<br/>Validacion"]
        Pricing["PricingFilterVerticle<br/>Precios y descuentos"]
        Fraud["FraudCheckFilterVerticle<br/>Revision de fraude"]
        Persistence["PersistenceFilterVerticle<br/>JPA/Hibernate"]
        Printer["OrderPrinterVerticle<br/>Impresion final"]
        Model["Order / OrderItem<br/>Entidades JPA"]
    end

    DB[("ordenes.db<br/>SQLite")]

    Usuario -->|"Ejecuta"| Main

    Main -->|"Despliega"| Ingress
    Main -->|"Despliega"| Validation
    Main -->|"Despliega"| Pricing
    Main -->|"Despliega"| Fraud
    Main -->|"Despliega worker"| Persistence
    Main -->|"Despliega"| Printer

    Ingress -->|"order.raw"| Bus
    Bus -->|"order.raw"| Validation

    Validation -->|"order.validated"| Bus
    Bus -->|"order.validated"| Pricing

    Pricing -->|"order.priced"| Bus
    Bus -->|"order.priced"| Fraud

    Fraud -->|"order.persist"| Bus
    Bus -->|"order.persist"| Persistence

    Persistence -->|"Construye"| Model
    Persistence -->|"Persiste"| DB
    Persistence -->|"order.done"| Bus
    Bus -->|"order.done"| Printer

    Printer -->|"Consulta e imprime"| DB
```
