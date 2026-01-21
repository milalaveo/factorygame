package domain.contract;

// I use this interface to define a blocking buffer.
public interface Buffer<T> {
    void put(T item) throws InterruptedException;

    T take() throws InterruptedException;

    int size();

    int capacity();
}
