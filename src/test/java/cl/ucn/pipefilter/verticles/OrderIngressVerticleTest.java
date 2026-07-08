package cl.ucn.pipefilter.verticles;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderIngressVerticleTest extends VerticleTestBase {

    @Test
    public void publicaOrdenDemoEnCanalRaw() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JsonObject> recibido = new AtomicReference<>();

        vertx.eventBus().<JsonObject>consumer("order.raw", message -> {
            recibido.set(message.body());
            latch.countDown();
        });

        desplegar(new OrderIngressVerticle());

        assertTrue("No llego la orden demo a order.raw", latch.await(4, TimeUnit.SECONDS));

        JsonObject orden = recibido.get();
        assertTrue(orden.getString("orderId").startsWith("ORD-DEMO-"));
        assertEquals("CUST-998", orden.getString("customerId"));
        assertEquals("DESCUENTO10", orden.getString("couponCode"));
        assertEquals(2, orden.getJsonArray("items").size());
    }
}
