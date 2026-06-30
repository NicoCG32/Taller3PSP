package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.format.DateTimeFormatter;

public class ValidationFilterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().consumer("order.raw", message -> {
            JsonObject order = (JsonObject) message.body();
            System.out.println("[ValidationFilter] 🔍 Validando orden: " + order.getString("orderId"));

            if (isValid(order)) {
                System.out.println("[ValidationFilter] ✅ Orden válida. Enviando a order.validated");
                vertx.eventBus().send("order.validated", order);
            } else {
                System.err.println("[ValidationFilter] ❌ Orden inválida. Descartando u ordenando a order.error");
                vertx.eventBus().send("order.error", order);
            }
        });
    }

    private boolean isValid(JsonObject order) {
        try {

            if (order.getString("orderId") == null || order.getString("orderId").trim().isEmpty()) return false;
            if (order.getString("customerId") == null || order.getString("customerId").trim().isEmpty()) return false;
            if (order.getString("currency") == null) return false;
            if (order.getString("paymentMethod") == null) return false;
            if (order.getString("timestamp") == null) return false;

            DateTimeFormatter.ISO_DATE_TIME.parse(order.getString("timestamp"));

            JsonArray items = order.getJsonArray("items");
            if (items == null || items.isEmpty()) return false;

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.getJsonObject(i);

                String productId = item.getString("productId");
                Integer quantity = item.getInteger("quantity");
                Number unitPrice = item.getNumber("unitPrice");

                if (productId == null || productId.trim().isEmpty()) return false;
                if (quantity == null || quantity <= 0) return false;
                if (unitPrice == null || unitPrice.longValue() < 0) return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}