package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.format.DateTimeFormatter;

/**
 * Filtro encargado de validar ordenes recibidas como JSON.
 *
 * PRECONDICIONES:
 * - Debe existir un mensaje JSON publicado en el canal {@code order.raw}.
 *
 * POSCONDICIONES:
 * - Las ordenes validas son publicadas en {@code order.validated}.
 * - Las ordenes invalidas son publicadas en {@code order.error}.
 */
public class ValidationFilterVerticle extends AbstractVerticle {

    /**
     * Registra el consumidor del canal de entrada del filtro de validacion.
     *
     * PRECONDICIONES:
     * - El verticle debe estar desplegado dentro de una instancia de Vert.x.
     *
     * POSCONDICIONES:
     * - El filtro queda escuchando mensajes desde {@code order.raw}.
     */
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

        System.out.println("[ValidationFilter] Filtro activo. Escuchando canal order.raw.");
    }

    /**
     * Verifica que una orden cumpla las reglas minimas del taller.
     *
     * PRECONDICIONES:
     * - {@code order} debe representar una orden recibida desde el EventBus.
     *
     * POSCONDICIONES:
     * - No modifica el JSON recibido.
     *
     * @param order orden en formato JSON que sera validada.
     * @return {@code true} si la orden contiene campos obligatorios, timestamp valido e items correctos; {@code false} en caso contrario.
     */
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