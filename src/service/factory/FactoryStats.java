package service.factory;

// I use this class to carry a stats snapshot.
public final class FactoryStats {
    private final long producedCount;
    private final long processedCount;
    private final int bufferSize;
    private final int bufferCapacity;

    public FactoryStats(long producedCount, long processedCount, int bufferSize, int bufferCapacity) {
        this.producedCount = producedCount;
        this.processedCount = processedCount;
        this.bufferSize = bufferSize;
        this.bufferCapacity = bufferCapacity;
    }

    public long getProducedCount() {
        return producedCount;
    }

    public long getProcessedCount() {
        return processedCount;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }
}
