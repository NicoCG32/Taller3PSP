package cl.ucn.pipefilter.verticles;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class OrderPrinterVerticleTest extends VerticleTestBase {

    @Test
    public void constructorPorDefectoCreaElVerticle() {
        assertNotNull(new OrderPrinterVerticle());
    }

    @Test
    public void imprimeAlRecibirUnaOrdenTerminada() throws InterruptedException {
        Runnable printer = mock(Runnable.class);
        desplegar(new OrderPrinterVerticle(printer));

        vertx.eventBus().send("order.done", "ORD-TERMINADA");

        verify(printer, timeout(5000)).run();
    }
}
