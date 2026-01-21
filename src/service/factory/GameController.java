package service.factory;

import domain.contract.Buffer;
import domain.contract.Consumer;
import domain.contract.GameEventListener;
import domain.contract.GameEventPublisher;
import domain.contract.GameMetrics;
import domain.contract.Producer;
import domain.model.GameEvent;
import domain.model.Task;
import domain.model.TaskType;
import service.buffer.BoundedBuffer;
import service.consumer.MachineConsumer;
import service.producer.SupplierProducer;
import shared.FactoryConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

// controller to run the simulation and track stats
public final class GameController implements GameMetrics, GameEventPublisher {
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Producer> producers = new ArrayList<>();
    private final List<Consumer> consumers = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final AtomicLong producedCount = new AtomicLong();
    private final AtomicLong processedCount = new AtomicLong();
    private final AtomicLong taskId = new AtomicLong();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Buffer<Task> buffer;
    private FactoryConfig config;

    public synchronized void start(FactoryConfig config) {
        if (running.get()) {
            return;
        }
        this.config = config;
        this.buffer = new BoundedBuffer<>(config.getBufferCapacity());
        this.producedCount.set(0);
        this.processedCount.set(0);
        this.taskId.set(0);
        this.producers.clear();
        this.consumers.clear();
        this.threads.clear();

        Supplier<Task> taskSupplier = () -> new Task(
                taskId.incrementAndGet(),
                randomType(),
                Instant.now()
        );

        for (int i = 1; i <= config.getProducerCount(); i++) {
            Producer producer = new SupplierProducer(
                    "Supplier-" + i,
                    buffer,
                    taskSupplier,
                    this,
                    this,
                    config.getProducerMinDelayMs(),
                    config.getProducerMaxDelayMs()
            );
            producers.add(producer);
            threads.add(new Thread(producer, producer.getName()));
        }

        for (int i = 1; i <= config.getConsumerCount(); i++) {
            Consumer consumer = new MachineConsumer(
                    "Machine-" + i,
                    buffer,
                    this,
                    this
            );
            consumers.add(consumer);
            threads.add(new Thread(consumer, consumer.getName()));
        }

        running.set(true);
        threads.forEach(Thread::start);
        publish("Controller", "simulation started", buffer.size());
    }

    public synchronized void stop() {
        if (!running.get()) {
            return;
        }
        running.set(false);
        producers.forEach(Producer::stop);
        consumers.forEach(Consumer::stop);
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        publish("Controller", "simulation stopped", buffer == null ? 0 : buffer.size());
    }

    public synchronized void restart(FactoryConfig config) {
        stop();
        start(config);
    }

    public boolean isRunning() {
        return running.get();
    }

    public FactoryStats getStats() {
        int size = buffer == null ? 0 : buffer.size();
        int capacity = buffer == null ? 0 : buffer.capacity();
        return new FactoryStats(producedCount.get(), processedCount.get(), size, capacity);
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void recordProduced() {
        producedCount.incrementAndGet();
    }

    @Override
    public void recordProcessed() {
        processedCount.incrementAndGet();
    }

    @Override
    public void publish(String actor, String action, int bufferSize) {
        GameEvent event = new GameEvent(Instant.now(), actor, action, bufferSize);
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    private TaskType randomType() {
        TaskType[] values = TaskType.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
