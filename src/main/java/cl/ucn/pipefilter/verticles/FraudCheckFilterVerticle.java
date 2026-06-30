package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FraudCheckFilterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().consumer("order.priced", message -> {
            JsonObject order = (JsonObject) message.body();
            System.out.println("[FraudCheckFilter] 🛡️ Evaluando riesgos para la orden: " + order.getString("orderId"));

            long total = order.getLong("total");
            String paymentMethod = order.getString("paymentMethod");
            JsonArray items = order.getJsonArray("items");

            boolean esSospechosa = false;

            if (total > 200000 && ("TARJETA_CREDITO".equals(paymentMethod) || "CREDIT_CARD".equals(paymentMethod))) {
                esSospechosa = true;
                System.out.println("[FraudCheckFilter] ⚠️ Alerta: Monto alto (" + total + ") con tarjeta de crédito.");
            }

            if (items != null && items.size() > 20) {
                esSospechosa = true;
                System.out.println("[FraudCheckFilter] ⚠️ Alerta: La orden contiene demasiados tipos de productos (" + items.size() + ").");
            }

            if (esSospechosa) {
                order.put("status", "REVISION");
            }

            System.out.println("[FraudCheckFilter] 🚦 Estado final de la orden: " + order.getString("status"));

            vertx.eventBus().send("order.persist", order);
        });
    }
}