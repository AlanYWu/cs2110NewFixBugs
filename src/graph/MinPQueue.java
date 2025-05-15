package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) double
 * priorities, implemented using a binary heap paired with a hash table.
 */
public class MinPQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, double priority) {
        // Note: This is equivalent to declaring a static nested class with fields `key` and
        //  `priority` and a corresponding constructor and observers, overriding `equals()` and
        //  `hashCode()` to depend on the fields, and overriding `toString()` to print their values.
        // https://docs.oracle.com/en/java/javase/17/language/records.html
    }

    /**
     * ArrayList representing a binary min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size())`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Associates each element in the queue with its index in `heap`.  Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final Map<KeyType, Integer> index;

    /**
     * Asserts the class invariants.
     */
    private void assertInv() {
        assert index.size() == heap.size() : "index.size() = " + index.size() + ", heap.size() = " + heap.size();
        for (int i = 0; i < heap.size(); i++) {
            Entry<KeyType> currentElement = heap.get(i);
            KeyType key = currentElement.key();
            assert i < 1 || heap.get(i).priority() >= heap.get((i - 1) / 2).priority(); //min-heap invariant

            assert index.containsKey(key); //check if index contains same element as in heap

            int mappedIdx = index.get(key);
            assert heap.get(mappedIdx).key().equals(key); //Check reverse mapping
        }
    }

    /**
     * Create an empty queue.
     */
    public MinPQueue() {
        index = new HashMap<>();
        heap = new ArrayList<>();
    }

    /**
     * Return whether this queue contains no elements.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType peek() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws
     * NoSuchElementException if this queue is empty.
     */
    public double minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * Swap the Entries at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires
     * {@code 0 <= i,j < heap.size()}.
     */
    private void swap(int i, int j) {
        assert 0 <= i && 0 <= j && i < heap.size() && j <= heap.size();

        Entry<KeyType> temp1 = heap.get(i);
        Entry<KeyType> temp2 = heap.get(j);
        heap.set(i, temp2);
        heap.set(j, temp1);

        index.put(temp1.key(), j);
        index.put(temp2.key(), i);
    }

    void bubbleUp(int k) {
        int parent = (k - 1) / 2;
        while (k > 0 && heap.get(k).priority() < heap.get(parent).priority()) {
            swap(k, parent);
            k = parent;
            parent = (k - 1) / 2;
        }
    }

    void bubbleDown(int k) {
        int lc = 2 * k + 1;
        while (lc < heap.size()) {  // while the left child exists
            int cMin = lc;  // Index of smallest child
            // Set `cMin` to the right child if it exists and its priority is smaller
            int rc = lc + 1;
            if (rc < heap.size() && heap.get(rc).priority() < heap.get(lc).priority()) {
                cMin = rc;
            }

            // If order invariant is locally satisfied, no need to bubble down further
            if (heap.get(k).priority() <= heap.get(cMin).priority()) {
                return;
            }

            // Swap `k` with smaller child, then update `c` to point to new left child
            swap(k, cMin);
            k = cMin;
            lc = 2 * k + 1;
        }
    }

    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, double priority) {
        index.put(key, index.size());
        heap.add(new Entry<>(key, priority));
        bubbleUp(heap.size() - 1);

        assertInv();
    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, double priority) {
        assert index.containsKey(key);

        int i = index.get(key);
        Entry<KeyType> entry = new Entry<>(key, priority);
        heap.set(i, entry);

        int parentIdx = (i - 1) / 2;

        if (heap.get(i).priority() < heap.get(parentIdx).priority) {
            bubbleUp(i);
        } else {
            bubbleDown(i);
        }
        assertInv();
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    public void addOrUpdate(KeyType key, double priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType remove() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException();
        }
        KeyType returnValue = heap.getFirst().key();
        index.remove(returnValue);

        if (heap.size() == 1) {
            heap.removeFirst();
        } else {
            Entry<KeyType> lastEntry = heap.removeLast();
            heap.set(0, lastEntry);
            index.put(lastEntry.key(), 0);
            bubbleDown(0);
        }

        assertInv();
        return returnValue;
    }
}
