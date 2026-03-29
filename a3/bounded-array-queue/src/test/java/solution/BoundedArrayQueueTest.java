package solution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class BoundedArrayQueueTest {

    private static final int NUM_THREADS = 8;

    /*
     * Tests basic single-threaded FIFO ordering and capacity behavior.
     *
     * Verifies that:
     *  - dequeue returns items in the order they were enqueued (FIFO),
     *  - the queue functions correctly up to its capacity,
     *  - the queue wraps around the internal array correctly.
     */
    @Test
    public void testSequentialFIFO() throws InterruptedException {
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(5);

        // enqueue 5 items (fills to capacity)
        for (int i = 0; i < 5; i++) {
            queue.enqueue(i);
        }

        // dequeue all and verify FIFO order
        for (int i = 0; i < 5; i++) {
            assertEquals(i, queue.dequeue());
        }

        // enqueue again to test wrap-around: tail was at index 0 after
        // the first 5 items, head was also at 0 after 5 dequeues
        for (int i = 10; i < 18; i++) {
            queue.enqueue(i);
            assertEquals(i, queue.dequeue());
        }
    }

    /*
     * Tests that enqueue blocks when the queue is full and resumes
     * after a dequeue frees space.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testBlockingOnFull() throws InterruptedException {
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(2);

        queue.enqueue(1);
        queue.enqueue(2);
        // queue is now full

        AtomicReference<String> failure = new AtomicReference<>(null);

        Thread producer = new Thread(() -> {
            try {
                queue.enqueue(3); // should block until space is available
            } catch (InterruptedException e) {
                failure.set("producer was interrupted");
            }
        });
        producer.start();

        // give the producer time to block
        Thread.sleep(100);

        // free one slot
        assertEquals(1, queue.dequeue());

        producer.join(2000);
        if (producer.isAlive()) {
            producer.interrupt();
            throw new AssertionError("producer thread did not unblock after dequeue");
        }

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }

        // the queue should now contain [2, 3]
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());
    }

    /*
     * Tests that dequeue blocks when the queue is empty and resumes
     * after an enqueue adds an item.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testBlockingOnEmpty() throws InterruptedException {
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(2);

        AtomicReference<Integer> result = new AtomicReference<>(null);
        AtomicReference<String> failure = new AtomicReference<>(null);

        Thread consumer = new Thread(() -> {
            try {
                result.set(queue.dequeue()); // should block until item available
            } catch (InterruptedException e) {
                failure.set("consumer was interrupted");
            }
        });
        consumer.start();

        // give the consumer time to block
        Thread.sleep(100);

        queue.enqueue(42);

        consumer.join(2000);
        if (consumer.isAlive()) {
            consumer.interrupt();
            throw new AssertionError("consumer thread did not unblock after enqueue");
        }

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }

        assertEquals(42, result.get());
    }

    /*
     * Tests that the capacity=1 edge case does not deadlock.
     *
     * This specifically targets the nested-lock deadlock that occurs
     * when cross-lock signaling (signalNotEmpty / signalNotFull) is
     * done while the primary lock is still held. With capacity=1,
     * both enqueue (prevSize==0) and dequeue (prevSize==capacity)
     * trigger cross-lock signals simultaneously.
     *
     * The fix (signaling after releasing the primary lock) is validated
     * here: if the deadlock bug were present, this test would hang and
     * be killed by the timeout.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testCapacityOneNoDeadlock() throws InterruptedException {
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(1);

        int itemsPerThread = 5_000;
        AtomicReference<String> failure = new AtomicReference<>(null);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < itemsPerThread; i++) {
                    queue.enqueue(i);
                }
            } catch (InterruptedException e) {
                failure.compareAndSet(null, "producer interrupted");
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < itemsPerThread; i++) {
                    int value = queue.dequeue();
                    if (value != i) {
                        failure.compareAndSet(null,
                            "expected " + i + " but got " + value);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                failure.compareAndSet(null, "consumer interrupted");
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    /*
     * Tests concurrent producers and consumers with a larger queue.
     *
     * Multiple producer threads each enqueue a known set of values.
     * Multiple consumer threads dequeue all values. After all threads
     * finish, we verify:
     *   - every value enqueued was dequeued exactly once (no lost or
     *     duplicated items),
     *   - no exceptions were thrown.
     *
     * This exercises the split-lock design under real contention on
     * both enqLock and deqLock simultaneously.
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testConcurrentProducersConsumers() throws InterruptedException {
        int capacity = 10; // small capacity to maximize blocking
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(capacity);

        int producers = NUM_THREADS / 2;
        int consumers = NUM_THREADS / 2;
        int itemsPerProducer = 10_000;
        int totalItems = producers * itemsPerProducer;

        ConcurrentLinkedQueue<Integer> consumed = new ConcurrentLinkedQueue<>();
        AtomicReference<String> failure = new AtomicReference<>(null);
        CountDownLatch done = new CountDownLatch(producers + consumers);

        // producers: each enqueues itemsPerProducer values
        // values are tagged with producer id to verify no duplicates
        for (int p = 0; p < producers; p++) {
            int producerId = p;
            new Thread(() -> {
                try {
                    for (int i = 0; i < itemsPerProducer; i++) {
                        queue.enqueue(producerId * itemsPerProducer + i);
                    }
                } catch (InterruptedException e) {
                    failure.compareAndSet(null, "producer " + producerId + " interrupted");
                } finally {
                    done.countDown();
                }
            }).start();
        }

        // consumers: each dequeues totalItems/consumers values
        int itemsPerConsumer = totalItems / consumers;
        for (int c = 0; c < consumers; c++) {
            int consumerId = c;
            new Thread(() -> {
                try {
                    for (int i = 0; i < itemsPerConsumer; i++) {
                        consumed.add(queue.dequeue());
                    }
                } catch (InterruptedException e) {
                    failure.compareAndSet(null, "consumer " + consumerId + " interrupted");
                } finally {
                    done.countDown();
                }
            }).start();
        }

        done.await();

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }

        // verify all items were consumed exactly once
        List<Integer> sorted = new ArrayList<>(consumed);
        Collections.sort(sorted);

        assertEquals(totalItems, sorted.size(),
            "wrong number of items consumed");

        for (int i = 0; i < totalItems; i++) {
            assertEquals(i, sorted.get(i),
                "missing or duplicate item at index " + i);
        }
    }

    /*
     * Tests that null enqueue is rejected immediately.
     */
    @Test
    public void testNullRejected() {
        BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(5);
        assertThrows(NullPointerException.class, () -> queue.enqueue(null));
    }

    /*
     * Tests that invalid capacity is rejected.
     */
    @Test
    public void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedArrayQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new BoundedArrayQueue<>(-1));
    }
}
