package solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class DeadlockPhilosophers {

    public static void main(String[] args) {
        int n = 5;
        Object[] chopsticks = IntStream.range(0, n)
            .mapToObj(i -> new Object())
            .toArray(Object[]::new);

        ExecutorService exc = Executors.newFixedThreadPool(n);

        IntStream.range(0, n).forEach(i ->
            exc.execute(
                new DeadlockPhilosopher(
                    i,
                    chopsticks[i],
                    chopsticks[(i + 1) % n]
                )
            )
        );
    }

    public static class DeadlockPhilosopher extends BasePhilosopher {

        Object lChopstick;
        Object rChopstick;

        public DeadlockPhilosopher(
            int id,
            Object lChopstick,
            Object rChopstick
        ) {
            super(id);
            this.lChopstick = lChopstick;
            this.rChopstick = rChopstick;
        }

        protected boolean pickUpChopsticks() throws InterruptedException {
            synchronized (lChopstick) {
                System.out.printf(
                    "philosopher %d picked up the left chopstick%n",
                    id
                );

                Thread.sleep(50);

                synchronized (rChopstick) {
                    System.out.printf(
                        "philosopher %d picked up the right chopstick%n",
                        id
                    );
                    eat();
                }
            }

            return false; // we eat inside the lock so return false
        }

        protected void putDownChopsticks() {
            System.out.printf("philosopher %d put down chopsticks%n", id);
        }
    }
}
