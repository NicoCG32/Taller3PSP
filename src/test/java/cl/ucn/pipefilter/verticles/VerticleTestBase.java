package cl.ucn.pipefilter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

abstract class VerticleTestBase {

    protected Vertx vertx;

    @Before
    public void crearVertx() {
        vertx = Vertx.vertx();
    }

    @After
    public void cerrarVertx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        vertx.close(ar -> latch.countDown());

        assertTrue("Vert.x no se cerro a tiempo", latch.await(5, TimeUnit.SECONDS));
    }

    protected void desplegar(AbstractVerticle verticle) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        vertx.deployVerticle(verticle, result -> {
            if (result.failed()) {
                failure.set(result.cause());
            }
            latch.countDown();
        });

        assertTrue("El verticle no se desplego a tiempo", latch.await(5, TimeUnit.SECONDS));

        if (failure.get() != null) {
            fail("No se pudo desplegar el verticle: " + failure.get().getMessage());
        }
    }

    protected JsonObject enviarYEsperar(String entrada, String salida, JsonObject mensaje) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JsonObject> recibido = new AtomicReference<>();

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(salida, message -> {
            recibido.set(message.body());
            latch.countDown();
        });

        vertx.eventBus().send(entrada, mensaje);

        assertTrue("No llego ningun mensaje a " + salida, latch.await(5, TimeUnit.SECONDS));
        consumer.unregister();

        return recibido.get();
    }
}
