package example;

public class Deadlock {

    public static void main(String[] args) {
        Object l1 = new Object();
        Object l2 = new Object();

        DeadlockThread t1 = new DeadlockThread("t1", l1, l2);
        DeadlockThread t2 = new DeadlockThread("t2", l2, l1);

        t1.start();
        t2.start();
    }

    private static class DeadlockThread extends Thread {

        String name;
        Object hold;
        Object wait;

        public DeadlockThread(String name, Object hold, Object wait) {
            this.name = name;
            this.hold = hold;
            this.wait = wait;
        }

        public void run() {
            synchronized (hold) {
                System.out.printf(
                    "thread %s holding onto object %x%n",
                    name,
                    System.identityHashCode(hold)
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}

                System.out.printf(
                    "thread %s waiting for object %x%n",
                    name,
                    System.identityHashCode(wait)
                );

                synchronized (wait) {
                    System.out.printf(
                        "thread %s holding object %x and %x%n",
                        name,
                        System.identityHashCode(hold),
                        System.identityHashCode(wait)
                    );
                }
            }
        }
    }
}
