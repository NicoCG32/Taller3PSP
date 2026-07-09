package cl.ucn.pipefilter.main;

import cl.ucn.pipefilter.verticles.FraudCheckFilterVerticle;
import cl.ucn.pipefilter.verticles.OrderIngressVerticle;
import cl.ucn.pipefilter.verticles.OrderPrinterVerticle;
import cl.ucn.pipefilter.verticles.PersistenceFilterVerticle;
import cl.ucn.pipefilter.verticles.PricingFilterVerticle;
import cl.ucn.pipefilter.verticles.ValidationFilterVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public class MainVerticleTest {

    @Test
    public void mainCreaVertxYDespliegaLaAplicacion() {
        Vertx vertx = mock(Vertx.class);

        try (MockedStatic<Vertx> vertxStatic = mockStatic(Vertx.class)) {
            vertxStatic.when(Vertx::vertx).thenReturn(vertx);

            MainVerticle.main(new String[0]);

            vertxStatic.verify(Vertx::vertx);
            verify(vertx).deployVerticle(isA(MainVerticle.class));
        }
    }

    @Test
    public void startDespliegaTodosLosVerticlesCuandoPersistenciaTieneExito() {
        Vertx vertx = vertxConResultadoPersistencia(Future.succeededFuture("persistence-id"));
        MainVerticle main = inicializar(vertx);

        main.start();

        verificarDespliegues(vertx);
    }

    @Test
    public void startManejaElFalloDeDespliegueDePersistencia() {
        Vertx vertx = vertxConResultadoPersistencia(
                Future.failedFuture(new IllegalStateException("despliegue fallido"))
        );
        MainVerticle main = inicializar(vertx);

        main.start();

        verificarDespliegues(vertx);
    }

    private MainVerticle inicializar(Vertx vertx) {
        MainVerticle main = new MainVerticle();
        main.init(vertx, mock(Context.class));
        return main;
    }

    private Vertx vertxConResultadoPersistencia(Future<String> result) {
        Vertx vertx = mock(Vertx.class);
        doAnswer(invocation -> {
            Handler<io.vertx.core.AsyncResult<String>> handler = invocation.getArgument(2);
            handler.handle(result);
            return null;
        }).when(vertx).deployVerticle(
                isA(PersistenceFilterVerticle.class),
                any(DeploymentOptions.class),
                any()
        );
        return vertx;
    }

    private void verificarDespliegues(Vertx vertx) {
        verify(vertx).deployVerticle(isA(OrderIngressVerticle.class));
        verify(vertx).deployVerticle(isA(ValidationFilterVerticle.class));
        verify(vertx).deployVerticle(isA(PricingFilterVerticle.class));
        verify(vertx).deployVerticle(isA(FraudCheckFilterVerticle.class));
        verify(vertx).deployVerticle(
                isA(PersistenceFilterVerticle.class),
                any(DeploymentOptions.class),
                any()
        );
        verify(vertx).deployVerticle(isA(OrderPrinterVerticle.class));
    }
}
