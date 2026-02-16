package solution;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.IntStream;

public class FilterLock implements Lock {

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 8;
        int iterations = 5;

        int[] sharedCounter = { 0 };
        FilterLock lock = new FilterLock(numThreads);

        List<Thread> threads = IntStream.range(0, numThreads)
            .mapToObj(i ->
                new Thread(() -> {
                    IntStream.range(0, iterations).forEach(j -> {
                        // deliberately slow down t0
                        if (i == 0) {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        lock.lock();
                        sharedCounter[0]++;
                        lock.unlock();
                    });
                })
            )
            .toList();

        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    private AtomicIntegerArray level;
    private AtomicIntegerArray victim;

    private final ThreadLocal<Integer> threadId = new ThreadLocal<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public FilterLock(Integer n) {
        this.level = new AtomicIntegerArray(n);
        this.victim = new AtomicIntegerArray(n);
    }

    public void lock() {
        Integer n = level.length();
        Integer id = getOrSetThreadId();

        IntStream.range(1, n).forEach(L -> {
            System.out.printf("thread %d has entered LEVEL%d%n", id, L);
            level.set(id, L);
            victim.set(L, id);

            while (
                IntStream.range(0, n).anyMatch(
                    k -> k != id && level.get(k) >= L && victim.get(L) == id
                )
            ) {
                Thread.onSpinWait();
            }
        });

        System.out.printf("thread %d has entered the CRITICAL SECTION &n", id);
    }

    public void unlock() {
        Integer id = getOrSetThreadId();

        level.set(id, 0);
    }

    public void lockInterruptibly() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Condition newCondition() {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean tryLock() {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean tryLock(long time, TimeUnit unit) {
        throw new UnsupportedOperationException("not implemented");
    }

    private Integer getOrSetThreadId() {
        Integer id = threadId.get();

        if (id == null) {
            id = idGenerator.getAndIncrement();
            threadId.set(id);
        }

        return id;
    }
}
