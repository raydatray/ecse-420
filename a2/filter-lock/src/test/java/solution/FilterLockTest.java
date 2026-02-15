package solution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FilterLockTest {

    private static final int ITERATIONS = 1000;

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8 })
    public void testMutex(Integer numThreads) throws InterruptedException {
        int[] sharedCounter = { 0 };
        FilterLock lock = new FilterLock(numThreads);

        List<Thread> threads = IntStream.range(0, numThreads)
            .mapToObj(i ->
                new Thread(() -> {
                    IntStream.range(0, ITERATIONS).forEach(j -> {
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

        assertEquals(
            numThreads * ITERATIONS,
            sharedCounter[0],
            String.format("final counter mismatch for numThreads = %d", numThreads)
        );
    }
}
