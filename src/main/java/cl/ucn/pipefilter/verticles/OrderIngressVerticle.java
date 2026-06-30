package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class OrderIngressVerticle extends AbstractVerticle {

    @Override
    public void start() {

        JsonObject orderJson = new JsonObject()
                .put("orderId", "ORD-12345")
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
    }
}