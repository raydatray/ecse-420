package solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class SafePhilosophers {

    public static void main(String[] args) {
        int n = 5;
        ReentrantLock[] chopsticks = IntStream.range(0, n)
            .mapToObj(i -> new ReentrantLock(true)) // true for fairness
            .toArray(ReentrantLock[]::new);

        ExecutorService exc = Executors.newFixedThreadPool(n);

        IntStream.range(0, n).forEach(i ->
            exc.execute(
                new SafePhilosopher(i, chopsticks[i], chopsticks[(i + 1) % n])
            )
        );
    }

    public static class SafePhilosopher extends BasePhilosopher {

        ReentrantLock lChopstick;
        ReentrantLock rChopstick;
        long timeoutMs = 100;

        public SafePhilosopher(
            int id,
            ReentrantLock lChopstick,
            ReentrantLock rChopstick
        ) {
            super(id);
            this.lChopstick = lChopstick;
            this.rChopstick = rChopstick;
        }

        protected boolean pickUpChopsticks() throws InterruptedException {
            boolean lAcq = false;
            boolean rAcq = false;

            try {
                lAcq = lChopstick.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
                if (lAcq) {
                    System.out.printf(
                        "philosopher %d picked up the left chopstick%n",
                        id
                    );

                    rAcq = rChopstick.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
                    if (rAcq) {
                        System.out.printf(
                            "philosopher %d picked up the right chopstick%n",
                            id
                        );

                        return true;
                    }
                }
                return false;
            } finally {
                if (lAcq && !rAcq) {
                    lChopstick.unlock();
                    System.out.printf(
                        "philosopher %d put down left chopstick%n",
                        id
                    );
                }
            }
        }

        protected void putDownChopsticks() {
            if (lChopstick.isHeldByCurrentThread()) lChopstick.unlock();
            if (rChopstick.isHeldByCurrentThread()) rChopstick.unlock();

            System.out.printf("philosopher %d put down chopsticks%n", id);
        }
    }
}
