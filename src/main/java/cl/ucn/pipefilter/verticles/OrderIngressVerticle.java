package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Verticle de entrada del pipeline.
 *
 * PRECONDICIONES:
 * - El EventBus de Vert.x debe estar disponible.
 *
 * POSCONDICIONES:
 * - Publica una orden JSON de ejemplo en el canal {@code order.raw}.
 */
public class OrderIngressVerticle extends AbstractVerticle {

    private long orderSequence = System.currentTimeMillis();

    /**
     * Construye una orden de ejemplo y la envia al primer canal del pipeline.
     *
     * PRECONDICIONES:
     * - El verticle debe haber sido desplegado por Vert.x.
     *
     * POSCONDICIONES:
     * - Luego de dos segundos se publica un mensaje en {@code order.raw}.
     */
    @Override
    public void start() {

        JsonObject orderJson = new JsonObject()
                .put("orderId", nextOrderId())
                .put("customerId", "CUST-998")
                .put("items", new JsonArray()
                        .add(new JsonObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 2)
                                .put("unitPrice", 15000))
                        .add(new JsonObject()
                                .put("productId", "PROD-B")
                                .put("quantity", 1)
                                .put("unitPrice", 5000)))
                .put("couponCode", "DESCUENTO10")
                .put("currency", "CLP")
                .put("timestamp", "2025-11-20T12:34:56Z")
                .put("paymentMethod", "TARJETA_CREDITO");

        vertx.setTimer(2000, timerId -> {
            System.out.println("[Ingress] 📦 Generando nueva orden: " + orderJson.getString("orderId"));

            vertx.eventBus().send("order.raw", orderJson);
        });

        System.out.println("[Ingress] Verticle activo. Publicara una orden en order.raw.");
    }

    /**
     * Genera un identificador incremental para la orden de demostracion.
     *
     * PRECONDICIONES:
     * - La secuencia privada debe estar inicializada.
     *
     * POSCONDICIONES:
     * - La secuencia aumenta en una unidad.
     *
     * @return identificador unico para una orden de demostracion.
     */
    private String nextOrderId() {
        return "ORD-DEMO-" + orderSequence++;
    }
}


