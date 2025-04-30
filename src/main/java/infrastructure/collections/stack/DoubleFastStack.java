package infrastructure.collections.stack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A generic implementation of a dynamic stack in Java. This is considered fast because it uses fewer
 * method calls and checks, which in turn is fewer instructions.
 * <p>
 * Now implements Iterable so you can iterate over the stack's doubles (in LIFO order) using enhanced for-loops.
 * </p>
 * @author Albert Beaupre
 * @version 1.0
 * @since May 1st, 2024
 */
public class DoubleFastStack implements Iterable<Double> {
    private double[] stack;
    private int ordinal;

    /**
     * Constructs a FastStack with the default initial size.
     */
    public DoubleFastStack() {
        this(10);
    }

    /**
     * Constructs a FastStack with a specified initial size.
     *
     * @param size the initial size of the stack
     */
    public DoubleFastStack(int size) {
        this.stack = new double[size];
    }

    /**
     * Adds an element to the top of the stack.
     *
     * @param data the element to be added
     */
    public void push(double data) {
        if (ordinal == stack.length) {
            double[] copy = new double[stack.length * 2];
            for (int i = 0; i < stack.length; i++) {
                copy[i] = stack[i];
            }
            stack = copy;
        }
        stack[ordinal++] = data;
    }

    /**
     * Removes and returns the element at the top of the stack.
     *
     * @return the element removed from the top of the stack, or -1 if the stack is empty
     */
    public double pop() {
        if (ordinal == 0)
            return -1;
        double old = stack[--ordinal];
        stack[ordinal] = 0;
        return old;
    }

    /**
     * Returns the element at the top of the stack without removing it.
     *
     * @return the element at the top of the stack
     * @throws NoSuchElementException if the stack is empty.
     */
    public double peek() {
        if (ordinal == 0) {
            throw new NoSuchElementException("Stack is empty.");
        }
        return stack[ordinal - 1];
    }

    /**
     * Checks if the stack is empty.
     *
     * @return true if the stack is empty, false otherwise.
     */
    public boolean isEmpty() {
        return ordinal == 0;
    }

    /**
     * Returns the current number of elements in the stack.
     *
     * @return the size of the stack.
     */
    public int size() {
        return ordinal;
    }

    /**
     * Clears the stack by setting the number of elements to zero and filling the array with zeros.
     */
    public void clear() {
        Arrays.fill(stack, 0);
        ordinal = 0;
    }

    /**
     * Returns a string representation of the elements in the stack.
     *
     * @return a string representation of the stack.
     */
    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(stack, ordinal));
    }

    /**
     * Returns an iterator over the elements in this stack in LIFO order (from the top of the stack to the bottom).
     *
     * @return an Iterator of Double objects.
     */
    @Override
    public Iterator<Double> iterator() {
        return new DoubleFastStackIterator();
    }

    /**
     * An iterator that traverses the DoubleFastStack in LIFO order.
     */
    private class DoubleFastStackIterator implements Iterator<Double> {
        // Start iterating at the most recently added element (top of the stack)
        private int currentIndex = ordinal - 1;

        /**
         * Checks if there are more elements to iterate over.
         *
         * @return true if there is another element, false otherwise.
         */
        @Override
        public boolean hasNext() {
            return currentIndex >= 0;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next Double in the stack.
         * @throws NoSuchElementException if no more elements exist in the iteration.
         */
        @Override
        public Double next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the stack.");
            }
            return stack[currentIndex--];  // Autoboxing from double to Double.
        }

        /**
         * Remove operation is not supported.
         *
         * @throws UnsupportedOperationException always.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported.");
        }
    }
}