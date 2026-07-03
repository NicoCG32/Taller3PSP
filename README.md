# Taller PSP - Pipeline de órdenes con Vert.x

**Docente:** Daniel San Martin  
**Curso:** Patrones de Software y Programación - Universidad Católica del Norte  
**Repositorio:** https://github.com/NicoCG32/Taller3PSP


## Descripción del proyecto

Implementación de un sistema de procesamiento de órdenes de compra basado en la arquitectura **Pipe & Filter**. El proyecto modela una tienda universitaria donde cada orden ingresa como un mensaje JSON, atraviesa una secuencia de filtros independientes y finalmente se almacena en una base de datos SQLite mediante JPA/Hibernate.

El objetivo principal es demostrar programación reactiva y arquitectura orientada a mensajes usando **Vert.x**. Cada filtro se implementa como un `Verticle`, y la comunicación entre etapas ocurre mediante canales del `EventBus`.

---

## Instrucciones del taller

El enunciado y las reglas originales del laboratorio se encuentran en:

```text
INSTRUCCIONES.md
```

---


## Arquitectura del sistema

El flujo principal del sistema es:

```text
OrderIngress
  -> ValidationFilter
  -> PricingFilter
  -> FraudCheckFilter
  -> PersistenceFilter
  -> OrderPrinter
```

[Diagrama de Arquitectura](docs/diagrams/Arch.md)

Cada etapa cumple una responsabilidad concreta:

| Componente | Canal de entrada | Canal de salida | Responsabilidad |
|---|---|---|---|
| `OrderIngressVerticle` | - | `order.raw` | Genera una orden de ejemplo y la publica en el pipeline. |
| `ValidationFilterVerticle` | `order.raw` | `order.validated` / `order.error` | Valida campos obligatorios, items y timestamp. |
| `PricingFilterVerticle` | `order.validated` | `order.priced` | Calcula subtotal, descuento, total y estado inicial. |
| `FraudCheckFilterVerticle` | `order.priced` | `order.persist` | Marca órdenes sospechosas para revisión. |
| `PersistenceFilterVerticle` | `order.persist` | `order.done` | Persiste la orden y sus items mediante JPA/Hibernate. |
| `OrderPrinterVerticle` | `order.done` | - | Muestra por consola el estado de la base de datos. |

Esta separacion permite estudiar el patron Pipe & Filter en un contexto reactivo: los filtros no se invocan directamente entre si, sino que se desacoplan mediante mensajes publicados en el EventBus.

---

## Reglas principales del pipeline

### Validación

Una orden válida debe incluir:

- `orderId`
- `customerId`
- `items`
- `currency`
- `paymentMethod`
- `timestamp` en formato ISO 8601

Cada item debe tener `productId`, `quantity > 0` y `unitPrice >= 0`.

### Cálculo de precios

El filtro de precios calcula:

```text
subtotal = sum(quantity * unitPrice)
total = subtotal - discount
```

Los cupones implementados son:

| Cupón | Regla |
|---|---|
| `DESCUENTO10` | 10% del subtotal. |
| `DESCUENTO20` | 20% solo si el subtotal es mayor o igual a 50.000. |

### Revisión de fraude

Una orden queda en estado `REVISION` si:

- `total > 200000` y el método de pago es `TARJETA_CREDITO` o `CREDIT_CARD`
- contiene más de 20 items.

Si no se detectan señales de fraude, mantiene el estado `CALCULADA`.

---

## Persistencia

La persistencia se realiza con:

- **JPA / Hibernate**
- **SQLite**
- entidades `Order` y `OrderItem`

El archivo de base de datos utilizado por defecto es:

```text
ordenes.db
```

---

## Compilación y ejecución

Para compilar el proyecto:

```bash
mvn clean compile
```

Para ejecutar las pruebas:

```bash
mvn test
```

Para ejecutar la aplicación:

```bash
mvn exec:java "-Dexec.mainClass=cl.ucn.pipefilter.main.MainVerticle"
```

En PowerShell es importante mantener entre comillas el argumento `-Dexec.mainClass`.

---

## Pruebas

El proyecto incluye pruebas unitarias con **JUnit 4** y pruebas arquitecturales con **ArchUnit**.

Aunque el enunciado del taller no solicitaba pruebas, se agregaron para aprovechar la instancia del curso y aplicar de forma integrada los contenidos vistos durante el semestre. Este tercer taller se concentra principalmente en programación reactiva con Vert.x, pero se mantuvieron pruebas básicas porque complementan la entrega y permiten verificar algunos criterios de diseño.

Las pruebas unitarias verifican:

- comportamiento básico de las entidades JPA.

Las pruebas arquitecturales verifican:

- que los filtros sean `Verticle`
- que el modelo no dependa de la ejecución reactiva
- que los filtros no se comuniquen mediante dependencias directas entre clases.

---

## Documentación técnica con Doxygen

El proyecto incluye una configuración inicial de Doxygen en:

```text
docs/doxygen/Doxyfile
```

**por implementar** en detalle. Falta agregar comentarios JavaDoc en las clases y métodos principales.

Para generar la documentación técnica:

```bash
doxygen docs/doxygen/Doxyfile
```

La documentación HTML se genera en:

```text
docs/doxygen/generated/html/index.html
```

En PowerShell puede abrirse con:

```powershell
Start-Process .\docs\doxygen\generated\html\index.html
```

Es necesario tener Doxygen instalado y disponible en el `PATH` del sistema.

---

## Estructura de carpetas

```text
Taller3PSP/
├── docs/
│   └── doxygen/
│       └── Doxyfile
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cl/ucn/pipefilter/
│   │   │       ├── config/
│   │   │       ├── main/
│   │   │       ├── model/
│   │   │       └── verticles/
│   │   └── resources/
│   │       └── META-INF/
│   └── test/
│       └── java/
│           └── cl/ucn/pipefilter/
│               ├── architecture/
│               └── model/
├── INSTRUCCIONES.md
├── ordenes.db
├── pom.xml
└── README.md
```

---

_Proyecto académico - enfoque en programación reactiva, arquitectura Pipe & Filter y sistemas orientados a mensajes._
