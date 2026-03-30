package solution;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class BoundedArrayQueue<T> {

    private final T[] items;
    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);

    private int head = 0;
    private int tail = 0;

    private final ReentrantLock enqLock = new ReentrantLock();
    private final ReentrantLock deqLock = new ReentrantLock();

    private final Condition notFull = enqLock.newCondition();
    private final Condition notEmpty = deqLock.newCondition();

    @SuppressWarnings("unchecked")
    public BoundedArrayQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
        this.items = (T[]) new Object[capacity];
    }

    /**
     * Acquires the lock, waits while the guard condition is true,
     * executes the action, then releases the lock.
     */
    private <R> R withGuardedLock(
        ReentrantLock lock,
        Condition condition,
        BooleanSupplier waitWhile,
        Supplier<R> action
    ) throws InterruptedException {
        lock.lock();
        try {
            while (waitWhile.getAsBoolean()) {
                condition.await();
            }
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void enqueue(T x) throws InterruptedException {
        if (x == null) throw new NullPointerException();

        boolean wasEmpty = withGuardedLock(
            enqLock, notFull,
            () -> size.get() == capacity,
            () -> {
                items[tail] = x;
                tail = (tail + 1) % capacity;
                int prevSize = size.getAndIncrement();
                if (prevSize + 1 < capacity) {
                    notFull.signal();
                }
                return prevSize == 0;
            }
        );

        // signal AFTER releasing enqLock to avoid nested lock deadlock
        if (wasEmpty) {
            signalNotEmpty();
        }
    }

    public T dequeue() throws InterruptedException {
        boolean[] wasFull = { false };

        T result = withGuardedLock(
            deqLock, notEmpty,
            () -> size.get() == 0,
            () -> {
                T x = items[head];
                items[head] = null;
                head = (head + 1) % capacity;
                int prevSize = size.getAndDecrement();
                if (prevSize > 1) {
                    notEmpty.signal();
                }
                wasFull[0] = (prevSize == capacity);
                return x;
            }
        );

        // signal AFTER releasing deqLock to avoid nested lock deadlock
        if (wasFull[0]) {
            signalNotFull();
        }

        return result;
    }

    private void signalNotEmpty() {
        deqLock.lock();
        try {
            notEmpty.signal();
        } finally {
            deqLock.unlock();
        }
    }

    private void signalNotFull() {
        enqLock.lock();
        try {
            notFull.signal();
        } finally {
            enqLock.unlock();
        }
    }
}
