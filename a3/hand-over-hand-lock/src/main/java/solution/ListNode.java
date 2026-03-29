package solution;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ListNode {

    Integer value;
    volatile ListNode next;

    private Lock lock;

    public ListNode(Integer value) {
        this.value = value;

        this.lock = new ReentrantLock();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
