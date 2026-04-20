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

    @Test
    public void testBoundedArrayQueue() {
        assertDoesNotThrow(() -> {
            BoundedArrayQueue<Integer> queue = new BoundedArrayQueue<>(5);
            int items = 30;

            Thread producer = new Thread(() ->
                IntStream.range(0, items).forEach(i -> {
                    try {
                        System.out.println("Enqueue: " + i);
                        queue.enqueue(i);
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
            );

            Thread consumer = new Thread(() ->
                IntStream.range(0, items).forEach(i -> {
                    try {
                        Integer val = queue.dequeue();
                        System.out.println("\tDequeue: " + val);
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
            );

            producer.start();
            consumer.start();
            producer.join();
            consumer.join();
        });
    }
}
