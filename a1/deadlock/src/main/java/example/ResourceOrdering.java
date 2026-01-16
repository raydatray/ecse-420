package example;

public class ResourceOrdering {

    public static void main(String[] args) {
        Object l1 = new Object();
        Object l2 = new Object();

        ResourceOrderingThread t1 = new ResourceOrderingThread("t1", l1, l2);
        ResourceOrderingThread t2 = new ResourceOrderingThread("t2", l2, l1);

        t1.start();
        t2.start();
    }

    private static class ResourceOrderingThread extends Thread {

        String name;
        Object resource1;
        Object resource2;

        public ResourceOrderingThread(
            String name,
            Object resource1,
            Object resource2
        ) {
            this.name = name;
            this.resource1 = resource1;
            this.resource2 = resource2;
        }

        public void run() {
            Object first, second;
            if (
                System.identityHashCode(resource1) <
                System.identityHashCode(resource2)
            ) {
                first = resource1;
                second = resource2;
            } else {
                first = resource2;
                second = resource1;
            }

            synchronized (first) {
                System.out.printf(
                    "thread %s holding object %x%n",
                    name,
                    System.identityHashCode(first)
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}

                System.out.printf(
                    "thread %s waiting for object %x%n",
                    name,
                    System.identityHashCode(second)
                );

                synchronized (second) {
                    System.out.printf(
                        "thread %s holding object %x and %x%n",
                        name,
                        System.identityHashCode(first),
                        System.identityHashCode(second)
                    );
                }
            }
        }
    }
}
