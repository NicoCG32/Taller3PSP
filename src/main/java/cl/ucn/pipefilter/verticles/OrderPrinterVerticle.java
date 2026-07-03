package cl.ucn.pipefilter.verticles;

import cl.ucn.pipefilter.config.OrderPrinter;
import io.vertx.core.AbstractVerticle;

/**
 * Verticle encargado de mostrar la base de datos al finalizar el pipeline.
 *
 * PRECONDICIONES:
 * - Debe existir una orden persistida correctamente.
 *
 * POSCONDICIONES:
 * - Al recibir {@code order.done}, imprime las ordenes almacenadas.
 */
public class OrderPrinterVerticle extends AbstractVerticle {

    /**
     * Registra el consumidor que reacciona al fin del procesamiento.
     *
     * PRECONDICIONES:
     * - El EventBus debe publicar mensajes en {@code order.done}.
     *
     * POSCONDICIONES:
     * - Se invoca la impresion de ordenes almacenadas en la base de datos.
     */
    @Override
    public void start() {
        vertx.eventBus().consumer("order.done", message -> {
            System.out.println("[Printer] 🖨️ ¡Proceso completado! Mostrando el estado de la base de datos...");
            OrderPrinter.printAllOrders();
        });

        System.out.println("[Printer] Verticle activo. Escuchando canal order.done.");
    }
}