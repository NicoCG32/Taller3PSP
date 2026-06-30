package cl.ucn.pipefilter.verticles;

import cl.ucn.pipefilter.config.OrderPrinter;
import io.vertx.core.AbstractVerticle;

public class OrderPrinterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().consumer("order.done", message -> {
            System.out.println("[Printer] 🖨️ ¡Proceso completado! Mostrando el estado de la base de datos...");
            OrderPrinter.printAllOrders();
        });
    }
}