package infrastructure.collections.stack;

import java.util.Arrays;

/**
 * A generic implementation of a dynamic stack in Java. This is considered fast because it uses fewer
 * method calls and checks, which in turn is fewer instructions.
 *
 * @param <T> the type of elements stored in the stack
 * @author Albert Beaupre
 * @version 1.0
 * @since May 1st, 2024
 */
public class FastStack<T> {
    private T[] stack;
    private int ordinal;

    /**
     * Constructs a FastStack with the default initial size.
     */
    public FastStack() {
        this(10);
    }

    /**
     * Constructs a FastStack with a specified initial size.
     *
     * @param size the initial size of the stack
     */
    public FastStack(int size) {
        this.stack = (T[]) new Object[size];
    }

    /**
     * Adds an element to the top of the stack.
     *
     * @param data the element to be added
     */
    public void push(T data) {
        if (ordinal == stack.length) { // Resize the stack array if it reaches its capacity
            // If the array is full, create a new array with double the capacity
            T[] copy = (T[]) new Object[stack.length * 2];
            // Copy the elements from the old array to the new array
            for (int i = 0; i < stack.length; i++)
                copy[i] = stack[i];
            // Update the reference to the new array
            stack = copy;
        }
        stack[ordinal++] = data;
    }

    /**
     * Removes and returns the element at the top of the stack.
     *
     * @return the element removed from the top of the stack
     */
    public T pop() {
        if (ordinal == 0)
            return null;
        T old = stack[--ordinal];
        stack[ordinal] = null;
        return old;
    }

    /**
     * Returns the element at the top of the stack without removing it.
     *
     * @return the element at the top of the stack
     */
    public T peek() {
        return stack[ordinal - 1];
    }

    /**
     * Checks if the stack is empty.
     *
     * @return true if the stack is empty, false otherwise
     */
    public boolean isEmpty() {
        return ordinal == 0;
    }

    /**
     * Returns the current number of elements in the stack.
     *
     * @return the size of the stack
     */
    public int size() {
        return ordinal;
    }

    /**
     * Clears the stack by setting the number of elements to zero and filling the array with null values.
     */
    public void clear() {
        for (int i = 0, len = stack.length; i < len; i++)
            stack[i] = null;
        ordinal = 0;
    }

    /**
     * Returns a string representation of the elements in the stack.
     *
     * @return a string representation of the stack
     */
    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(stack, ordinal));
    }
}