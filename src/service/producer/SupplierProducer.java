package service.producer;

import domain.contract.Buffer;
import domain.contract.GameEventPublisher;
import domain.contract.GameMetrics;
import domain.contract.Producer;
import domain.model.Task;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

// supply tasks at random intervals
public final class SupplierProducer implements Producer {
    private final String name;
    private final Buffer<Task> buffer;
    private final Supplier<Task> taskSupplier;
    private final GameMetrics metrics;
    private final GameEventPublisher eventPublisher;
    private final int minDelayMs;
    private final int maxDelayMs;
    private volatile boolean running;

    public SupplierProducer(
            String name,
            Buffer<Task> buffer,
            Supplier<Task> taskSupplier,
            GameMetrics metrics,
            GameEventPublisher eventPublisher,
            int minDelayMs,
            int maxDelayMs
    ) {
        this.name = name;
        this.buffer = buffer;
        this.taskSupplier = taskSupplier;
        this.metrics = metrics;
        this.eventPublisher = eventPublisher;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.running = true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                int delay = ThreadLocalRandom.current().nextInt(minDelayMs, maxDelayMs + 1);
                Thread.sleep(delay);
                Task task = taskSupplier.get();
                buffer.put(task);
                metrics.recordProduced();
                eventPublisher.publish(name, "delivered " + task.getType() + " task#" + task.getId(), buffer.size());
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
