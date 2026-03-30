package solution;

import java.util.function.BiFunction;

public class FineGrainedListSet {

    private ListNode head;

    public FineGrainedListSet() {
        this.head = new ListNode(Integer.MIN_VALUE);

        ListNode tail = new ListNode(Integer.MAX_VALUE);
        head.next = tail;
    }

    private <R> R withLockedPair(Integer value, BiFunction<ListNode, ListNode, R> action) {
        ListNode pred = head;
        pred.lock();

        ListNode curr = pred.next;
        curr.lock();

        try {
            while (curr.value < value) {
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
            }

            return action.apply(pred, curr);
        } finally {
            curr.unlock();
            pred.unlock();
        }
    }

    public Boolean contains(Integer value) {
        return withLockedPair(value, (pred, curr) -> curr.value.equals(value));
    }

    public Boolean add(Integer value) {
        return withLockedPair(value, (pred, curr) -> {
            if (curr.value.equals(value)) {
                return false;
            }
            ListNode node = new ListNode(value);
            node.next = curr;
            pred.next = node;

            return true;
        });
    }

    public Boolean remove(Integer value) {
        return withLockedPair(value, (pred, curr) -> {
            if (curr.value.equals(value)) {
                pred.next = curr.next;

                return true;
            }
            return false;
        });
    }
}
