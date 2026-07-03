package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Filtro encargado de calcular subtotal, descuento y total de una orden.
 *
 * PRECONDICIONES:
 * - La orden debe haber sido validada previamente.
 * - El mensaje debe llegar por el canal {@code order.validated}.
 *
 * POSCONDICIONES:
 * - Agrega los campos {@code subtotal}, {@code discount}, {@code total} y {@code status}.
 * - Publica la orden calculada en {@code order.priced}.
 */
public class PricingFilterVerticle extends AbstractVerticle {

    /**
     * Registra el consumidor del canal de precios y aplica las reglas de descuentos.
     *
     * PRECONDICIONES:
     * - El JSON recibido debe contener un arreglo {@code items}.
     * - Cada item debe tener {@code quantity} y {@code unitPrice}.
     *
     * POSCONDICIONES:
     * - La orden queda marcada con estado {@code CALCULADA}.
     * - La orden se envia al canal {@code order.priced}.
     */
    @Override
    public void start() {
        vertx.eventBus().consumer("order.validated", message -> {
            JsonObject order = (JsonObject) message.body();
            System.out.println("[PricingFilter] 💰 Calculando precios para la orden: " + order.getString("orderId"));

            long subtotal = 0;
            JsonArray items = order.getJsonArray("items");

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.getJsonObject(i);
                long quantity = item.getInteger("quantity");
                long unitPrice = item.getNumber("unitPrice").longValue();

                subtotal += (quantity * unitPrice);
            }

            String couponCode = order.getString("couponCode");
            long discount = 0;

            if ("DESCUENTO10".equals(couponCode)) {
                discount = (long) (subtotal * 0.10);
            } else if ("DESCUENTO20".equals(couponCode) && subtotal >= 50000) {
                discount = (long) (subtotal * 0.20);
            }

            long total = subtotal - discount;
            order.put("subtotal", subtotal);
            order.put("discount", discount);
            order.put("total", total);
            order.put("status", "CALCULADA");

            System.out.println("[PricingFilter] ✨ Resultados -> Subtotal: " + subtotal +
                    ", Descuento: " + discount + ", Total: " + total);

            vertx.eventBus().send("order.priced", order);
        });

        System.out.println("[PricingFilter] Filtro activo. Escuchando canal order.validated.");
    }
}