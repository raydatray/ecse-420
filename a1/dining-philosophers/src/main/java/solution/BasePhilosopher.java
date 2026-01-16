package solution;

public abstract class BasePhilosopher implements Runnable {

    protected int id;
    protected int eatCount = 0;

    public void run() {
        try {
            while (true) {
                think();
                if (pickUpChopsticks()) {
                    eat();
                    putDownChopsticks();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("philosopher %d was interrupted %n", id);
        }
    }

    protected void think() throws InterruptedException {
        System.out.printf("philosopher %d is thinking %n", id);

        Thread.sleep(50);
    }

    protected void eat() throws InterruptedException {
        System.out.printf("philosopher %d is eating%n", id);

        eatCount++;
        System.out.printf("philosopher %d has eaten %d times%n", id, eatCount);

        Thread.sleep(100);
    }

    protected abstract boolean pickUpChopsticks() throws InterruptedException;

    protected abstract void putDownChopsticks();
}
