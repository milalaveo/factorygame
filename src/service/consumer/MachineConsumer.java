package service.consumer;

import domain.contract.Buffer;
import domain.contract.Consumer;
import domain.contract.GameEventPublisher;
import domain.contract.GameMetrics;
import domain.model.Task;

// process tasks from the buffer
public final class MachineConsumer implements Consumer {
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
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Task task = buffer.take();
                eventPublisher.publish(name, "picked " + task.getType() + " task#" + task.getId(), buffer.size());
                Thread.sleep(task.getProcessingTimeMs());
                metrics.recordProcessed();
                eventPublisher.publish(name, "assembled " + task.getType() + " task#" + task.getId(), buffer.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public String getName() {
        return name;
    }
}
