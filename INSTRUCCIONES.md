# Laboratorio III: Arquitectura Pipe & Filter

**Profesor:** Daniel San Martin  
**Curso:** Patrones de Software y Programacion

---

## Objetivo del laboratorio

Implementar un pipeline de procesamiento de ordenes de compra usando:

- el patron **Pipe & Filter**;
- **Vert.x** y su EventBus;
- mensajes **JSON**;
- persistencia en base de datos mediante **JPA/Hibernate**.

El patron Pipe & Filter es un estilo arquitectural donde una tarea compleja se divide en multiples pasos independientes, llamados filtros, conectados entre si mediante tuberias o pipes.

### Que es un filtro

Un filtro es un componente que:

1. recibe datos de entrada;
2. los transforma, valida o procesa;
3. produce datos de salida o los descarta.

Cada filtro realiza una tarea especifica y autonoma.

### Que es un pipe

Un pipe es el canal que conecta un filtro con el siguiente. Transmite el resultado de un filtro como entrada del siguiente.

Este patron permite:

1. procesar datos en etapas;
2. aislar responsabilidades;
3. reutilizar filtros;
4. reemplazar filtros sin romper el sistema;
5. encadenar pasos facilmente.

En este laboratorio cada etapa del pipeline se implementa como un **Verticle**, y cada enlace como un canal del **EventBus**.

---

## Contexto

La universidad administra una tienda online para vender productos institucionales, tales como libros, poleras de carreras, tazas con logo, entre otros.

Cada compra genera un **JSON de orden**, pero estos datos pueden venir incompletos, con errores o con montos sospechosos. La tarea consiste en implementar un sistema modular basado en filtros encadenados.

---

## Arquitectura Pipe & Filter

```text
OrderIngress -> ValidationFilter -> PricingFilter -> FraudCheckFilter -> PersistenceFilter
```

---

## Formato JSON de entrada

```json
{
  "orderId": "...",
  "customerId": "...",
  "items": [
    {
      "productId": "...",
      "quantity": 2,
      "unitPrice": 15000
    }
  ],
  "couponCode": "DESCUENTO10",
  "currency": "CLP",
  "timestamp": "2025-11-20T12:34:56Z",
  "paymentMethod": "CREDIT_CARD"
}
```

---

## Filtros del pipeline

### 1. ValidationFilter

**Entrada:** JSON desde `order.raw`  
**Salida OK:** `order.validated`  
**Salida error:** `order.error`

#### Reglas

1. Campos obligatorios:
   - `orderId` como `String` no vacio;
   - `customerId` como `String`;
   - `items` como array no vacio;
   - `currency` como `String`;
   - `paymentMethod` como `String`;
   - `timestamp` como `String` en formato ISO 8601.

2. Reglas para cada item:
   - `productId` no vacio;
   - `quantity` entero mayor que `0`;
   - `unitPrice` entero mayor o igual que `0`.

3. Si alguna regla falla:
   - la orden se descarta o se envia a `order.error`.

4. Si pasa todas las validaciones:
   - se envia el mismo JSON a `order.validated`.

---

### 2. PricingFilter

**Entrada:** JSON desde `order.validated`  
**Salida:** `order.priced`

#### Reglas

1. Calculo del subtotal:

```text
subtotal = sum(quantity * unitPrice)
```

2. Descuentos segun cupon:
   - `DESCUENTO10`: 10% del subtotal;
   - `DESCUENTO20`: 20% si el subtotal es mayor o igual a 50.000;
   - otro caso: descuento igual a 0.

3. Calculo del total:

```text
total = subtotal - discount
```

4. Agregar o modificar campos en el JSON:
   - `subtotal`;
   - `discount`;
   - `total`;
   - `status = "CALCULADA"`.

5. Enviar el JSON a `order.priced`.

---

### 3. FraudCheckFilter

**Entrada:** JSON desde `order.priced`  
**Salida:** `order.persist`

#### Reglas

1. Monto alto con tarjeta de credito:
   - si `total > 200000` y `paymentMethod = "TARJETA_CREDITO"`, marcar la orden como sospechosa con `status = "REVISION"`.

2. Demasiados productos:
   - si `items.length > 20`, marcar la orden con `status = "REVISION"`.

3. Si no hay senales de fraude:
   - mantener `status = "CALCULADA"`.

4. En todos los casos:
   - enviar el JSON resultante a `order.persist`.

---

## Entidades JPA

### Order

- `orderId`
- `customerId`
- `timestamp`
- `currency`
- `paymentMethod`
- `subtotal`
- `discount`
- `total`
- `status`

### OrderItem

- `id`
- `productId`
- `quantity`
- `unitPrice`
