package cl.ucn.pipefilter.verticles;

import cl.ucn.pipefilter.config.JPAUtil;
import cl.ucn.pipefilter.model.Order;
import cl.ucn.pipefilter.model.OrderItem;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.EntityManager;

import java.time.Instant;

public class PersistenceFilterVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        vertx.eventBus().consumer("order.persist", message -> {
            JsonObject json = (JsonObject) message.body();

            vertx.executeBlocking(promise -> {
                try {
                    persistOrder(json);
                    promise.complete();
                } catch (Exception e) {
                    promise.fail(e);
                }
            }, res -> {
                if (res.succeeded()) {
                    System.out.println("[PERSIST] Orden almacenada: " + json.getString("orderId"));
                    vertx.eventBus().send("order.done", json);
                } else {
                    System.err.println("[PERSIST] Error al almacenar orden: " + res.cause());
                }
            });
        });
        startPromise.complete();
    }

    private void persistOrder(JsonObject json) {

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Order order = new Order();
            order.setOrderId(json.getString("orderId"));
            order.setCustomerId(json.getString("customerId"));
            order.setCurrency(json.getString("currency"));
            order.setPaymentMethod(json.getString("paymentMethod"));
            order.setSubtotal(json.getLong("subtotal"));
            order.setDiscount(json.getLong("discount"));
            order.setTotal(json.getLong("total"));
            order.setStatus(json.getString("status"));

            String ts = json.getString("timestamp");
            if (ts != null) {
                order.setTimestamp(Instant.parse(ts));
            }

            JsonArray itemsArray = json.getJsonArray("items");
            if (itemsArray != null) {
                itemsArray.forEach(obj -> {
                    JsonObject itemJson = (JsonObject) obj;
                    OrderItem item = new OrderItem();
                    item.setProductId(itemJson.getString("productId"));
                    item.setQuantity(itemJson.getInteger("quantity"));
                    item.setUnitPrice(itemJson.getLong("unitPrice"));
                    item.setOrder(order);           // relación ManyToOne
                    order.getItems().add(item);     // relación OneToMany
                });
            }

            em.persist(order);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
