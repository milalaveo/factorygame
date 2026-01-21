package service.consumer;

import domain.contract.Buffer;
import domain.contract.Consumer;
import domain.contract.GameEventPublisher;
import domain.contract.GameMetrics;
import domain.model.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

// process tasks from the buffer
public final class MachineConsumer implements Consumer {
    private static final Logger LOGGER = Logger.getLogger(MachineConsumer.class.getName());
    private final String name;
    private final Buffer<Task> buffer;
    private final GameMetrics metrics;
    private final GameEventPublisher eventPublisher;
    private volatile boolean running;

    public MachineConsumer(
            String name,
            Buffer<Task> buffer,
            GameMetrics metrics,
            GameEventPublisher eventPublisher
    ) {
        this.name = name;
        this.buffer = buffer;
        this.metrics = metrics;
        this.eventPublisher = eventPublisher;
        this.running = true;
    }

    @Override
    public void run() {
        LOGGER.info(() -> name + " consumer started");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Task task = buffer.take();
                eventPublisher.publish(name, "picked " + task.getType() + " task#" + task.getId(), buffer.size());
                Thread.sleep(task.getProcessingTimeMs());
                metrics.recordProcessed();
                eventPublisher.publish(name, "assembled " + task.getType() + " task#" + task.getId(), buffer.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.INFO, name + " consumer interrupted", e);
                break;
            }
        }
        LOGGER.info(() -> name + " consumer stopped");
    }

    @Override
    public void stop() {
        running = false;
        LOGGER.info(() -> name + " consumer stop requested");
    }

    @Override
    public String getName() {
        return name;
    }
}
