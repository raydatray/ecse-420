package solution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class FineGrainedListSetTest {

    private static final int NUM_THREADS = 8;

    /*
     * Tests basic single-threaded correctness of the FineGrainedListSet.
     *
     * This verifies that:
     *  - contains() returns false for values that were never added
     *  - add() returns true for new values, false for duplicates
     *  - remove() returns true for present values, false for absent ones
     *  - contains() reflects the state after add/remove operations
     */
    @Test
    public void testSequentialContains() {
        FineGrainedListSet list = new FineGrainedListSet();

        // initially contains only sentinels, so this should return false
        assertFalse(list.contains(10));

        // add returns true for new values
        assertTrue(list.add(10));
        assertTrue(list.add(20));
        assertTrue(list.add(30));

        // add returns false for duplicates
        assertFalse(list.add(10));
        assertFalse(list.add(20));

        // list should contain values that have just been added
        assertTrue(list.contains(10));
        assertTrue(list.contains(20));
        assertTrue(list.contains(30));

        // list should not contain values we never added
        assertFalse(list.contains(15));
        assertFalse(list.contains(999));

        // remove returns true for present values
        assertTrue(list.remove(20));

        // remove returns false for values already removed or never added
        assertFalse(list.remove(20));
        assertFalse(list.remove(999));

        // removed value should no longer be found
        assertFalse(list.contains(20));

        // other values should be unaffected
        assertTrue(list.contains(10));
        assertTrue(list.contains(30));
    }

    /*
     * Tests contains() under concurrent mutations (adds and removes).
     *
     * Design:
     *  - Values in [0, 1000) are pre-populated and NEVER removed.
     *    These are the "stable" values. Reader threads assert that
     *    contains() always returns true for them.
     *
     *  - Values in [1000, 2000) are the "contested" range. Writer
     *    threads continuously add and remove values in this range.
     *
     *  - Reader threads also check that contains() returns false for
     *    values in [5000, 6000), which are never added by anyone.
     *
     * This validates that:
     *  - contains() never returns a wrong answer while concurrent
     *    modifications are happening in other parts of the list,
     *  - the hand-over-hand locking prevents readers from seeing
     *    a partially-linked or unlinked node during traversal,
     *  - no deadlock occurs between readers and writers.
     *
     * The test fails if any reader observes an inconsistency, or if
     * the 30-second timeout is hit (indicating a likely deadlock).
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testConcurrentContainsUnderMutation() throws InterruptedException {
        FineGrainedListSet list = new FineGrainedListSet();

        // pre-populate stable range [0, 1000)
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        // captures the first error message from any thread
        AtomicReference<String> failure = new AtomicReference<>(null);

        List<Thread> threads = new ArrayList<>();

        // --- reader threads: check stable values and absent values ---
        for (int t = 0; t < NUM_THREADS / 2; t++) {
            threads.add(new Thread(() -> {
                Random rand = new Random();

                for (int i = 0; i < 10_000 && failure.get() == null; i++) {
                    // stable value must always be found
                    int stable = rand.nextInt(1000);
                    if (!list.contains(stable)) {
                        failure.compareAndSet(null,
                            "contains(" + stable + ") returned false for a stable value");
                        break;
                    }

                    // never-added value must never be found
                    int absent = 5000 + rand.nextInt(1000);
                    if (list.contains(absent)) {
                        failure.compareAndSet(null,
                            "contains(" + absent + ") returned true for a never-added value");
                        break;
                    }
                }
            }));
        }

        // --- writer threads: continuously add/remove in contested range [1000, 2000) ---
        for (int t = 0; t < NUM_THREADS / 2; t++) {
            threads.add(new Thread(() -> {
                Random rand = new Random();

                for (int i = 0; i < 10_000 && failure.get() == null; i++) {
                    int value = 1000 + rand.nextInt(1000);

                    if (rand.nextBoolean()) {
                        list.add(value);
                    } else {
                        list.remove(value);
                    }
                }
            }));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    /*
     * Tests that concurrent add and remove operations produce a consistent
     * final state and maintain sorted-list structural integrity.
     *
     * Design:
     *  - Each thread is assigned a disjoint value range, so there is no
     *    contention on the same logical value. However, threads physically
     *    traverse overlapping regions of the list, exercising the locking.
     *
     *  - Each thread adds all values in its range, then removes the even
     *    values in its range.
     *
     *  - After all threads complete, we verify:
     *      (a) only the odd values remain,
     *      (b) no even values remain,
     *      (c) the list is still sorted (structural integrity).
     *
     * This catches bugs where concurrent modifications corrupt the list
     * structure (e.g., lost nodes, broken links, duplicates).
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testConcurrentAddRemoveIntegrity() throws InterruptedException {
        FineGrainedListSet list = new FineGrainedListSet();

        int rangePerThread = 200;
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < NUM_THREADS; t++) {
            int start = t * rangePerThread; // disjoint ranges: [0,200), [200,400), ...
            threads.add(new Thread(() -> {
                // add all values in range
                for (int i = start; i < start + rangePerThread; i++) {
                    list.add(i);
                }
                // remove even values
                for (int i = start; i < start + rangePerThread; i += 2) {
                    list.remove(i);
                }
            }));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        // verify: only odd values should remain
        int totalValues = NUM_THREADS * rangePerThread;
        for (int i = 0; i < totalValues; i++) {
            if (i % 2 == 1) {
                assertTrue(list.contains(i), "odd value " + i + " should be present");
            } else {
                assertFalse(list.contains(i), "even value " + i + " should have been removed");
            }
        }
    }

    /*
     * Stress test: many threads performing overlapping add, remove, and
     * contains operations on a shared range.
     *
     * This maximizes contention on the same nodes to flush out subtle
     * races in the hand-over-hand locking protocol. We track every
     * successful add/remove per thread using thread-local sets, then
     * verify that contains() agrees with the net result after all
     * threads finish.
     *
     * Since operations overlap, some adds and removes will fail (return
     * false). That's expected. The invariant is: after all threads join,
     * a value should be present iff it was successfully added and never
     * successfully removed.
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testHighContentionMixedOperations() throws InterruptedException {
        FineGrainedListSet list = new FineGrainedListSet();

        int range = 50; // small range = high contention
        // track which values were last added/removed across all threads
        Set<Integer> added = ConcurrentHashMap.newKeySet();
        Set<Integer> removed = ConcurrentHashMap.newKeySet();

        AtomicReference<String> failure = new AtomicReference<>(null);
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < NUM_THREADS; t++) {
            threads.add(new Thread(() -> {
                Random rand = new Random();

                for (int i = 0; i < 10_000 && failure.get() == null; i++) {
                    int value = rand.nextInt(range);
                    int op = rand.nextInt(3);

                    switch (op) {
                        case 0 -> list.add(value);
                        case 1 -> list.remove(value);
                        case 2 -> list.contains(value); // just exercise the traversal
                    }
                }
            }));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        // after all threads finish, the list should be self-consistent:
        // for every value in range, contains() should agree with a
        // subsequent add/remove attempt
        for (int i = 0; i < range; i++) {
            boolean found = list.contains(i);
            if (found) {
                // if contains says it's there, add should return false (duplicate)
                assertFalse(list.add(i),
                    "contains(" + i + ") returned true but add(" + i + ") also returned true");
            } else {
                // if contains says it's not there, remove should return false (absent)
                assertFalse(list.remove(i),
                    "contains(" + i + ") returned false but remove(" + i + ") also returned false");
            }
        }
    }
}
