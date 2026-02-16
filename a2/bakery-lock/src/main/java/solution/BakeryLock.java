package solution;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.IntStream;

public class BakeryLock implements Lock {

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 8;
        int iterations = 5;

        int[] sharedCounter = { 0 };
        BakeryLock lock = new BakeryLock(numThreads);

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

    private final AtomicIntegerArray flag;
    private final AtomicIntegerArray label;

    private final ThreadLocal<Integer> threadId = new ThreadLocal<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public BakeryLock(Integer n) {
        this.flag = new AtomicIntegerArray(n);
        this.label = new AtomicIntegerArray(n);
    }

    public void lock() {
        int n = flag.length();
        int id = getOrSetThreadId();

        flag.set(id, 1);

        int highestLabel = IntStream.range(0, n)
            .map(k -> label.get(k))
            .max()
            .orElse(0);

        label.set(id, highestLabel + 1);
        flag.set(id, 0);

        System.out.printf("thread %d obtained label%d%n", id, label.get(id));

        IntStream.range(0, n)
            .filter(k -> k != id)
            .forEach(k -> {
                while (flag.get(k) == 1) {
                    Thread.onSpinWait();
                }

                while (
                    label.get(k) != 0 &&
                    (label.get(k) < label.get(id) || (label.get(k) == label.get(id) && k < id))
                ) {
                    Thread.onSpinWait();
                }
            });

        System.out.printf(
            "thread %d with label %d has entered the CRITICAL SECTION%n",
            id,
            label.get(id)
        );
    }

    public void unlock() {
        int id = getOrSetThreadId();

        flag.set(id, 0);
        label.set(id, 0);
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
