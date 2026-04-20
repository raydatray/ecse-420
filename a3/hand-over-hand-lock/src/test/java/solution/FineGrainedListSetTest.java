package solution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class FineGrainedListSetTest {

    private static final int NUM_THREADS = 8;

    @Test
    public void testSequentialContains() {
        FineGrainedListSet list = new FineGrainedListSet();

        // initially contains only sentinel values - this should return false
        assertFalse(list.contains(1));

        // add things to list
        list.add(1);
        list.add(2);
        list.add(3);

        // should return true for all added items
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));

        // should return false for not added items
        assertFalse(list.contains(4));
        assertFalse(list.contains(5));

        // remove smthn from list
        list.remove(2);

        // should return false for removed items
        assertFalse(list.contains(2));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testConcurrentContains() throws InterruptedException {
        FineGrainedListSet list = new FineGrainedListSet();
        IntStream.range(0, 1000).forEach(list::add);

        AtomicBoolean hasFailed = new AtomicBoolean(false);

        List<Thread> threads = IntStream.range(0, NUM_THREADS)
            .mapToObj(t ->
                new Thread(() -> {
                    boolean failed = IntStream.range(0, 10_000).anyMatch(i ->
                        !list.contains(ThreadLocalRandom.current().nextInt(100))
                    );
                    if (failed) {
                        hasFailed.set(true);
                    }
                })
            )
            .collect(Collectors.toList());

        threads.forEach(Thread::start);

        for (Thread t : threads) {
            t.join();
        }

        assertFalse(hasFailed.get());
    }
}
