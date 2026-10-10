package com.queue.project;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomQueue implements a genuine First-In, First-Out (FIFO) Queue
 * data structure using a singly linked list with explicit Node objects.
 *
 * Design:
 * - head: Points to the FRONT of the queue (where elements leave via dequeue).
 * - tail: Points to the REAR of the queue (where elements enter via enqueue).
 * - size: Tracks total elements in O(1) time.
 *
 * Time Complexities:
 * - enqueue(value): O(1)
 * - dequeue():      O(1)
 * - peek():         O(1)
 * - isEmpty():      O(1)
 * - size():         O(1)
 * - clear():        O(1)
 *
 * Thread Safety:
 * - All state-altering and reading operations are synchronized to ensure safe
 *   concurrent access across multiple HTTP server worker threads.
 */
public class CustomQueue {

    /**
     * Internal node representing an element in the linked list queue.
     */
    private static class Node {
        String data;
        Node next;

        Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    /**
     * Initializes an empty CustomQueue.
     */
    public CustomQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts an element at the rear of the queue.
     *
     * @param value the string item to enqueue
     * @return true if enqueued successfully, false if value is null
     */
    public synchronized boolean enqueue(String value) {
        if (value == null) {
            return false;
        }

        Node newNode = new Node(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
        return true;
    }

    /**
     * Removes and returns the front element from the queue.
     *
     * @return the front string element, or null if the queue is empty
     */
    public synchronized String dequeue() {
        if (isEmpty()) {
            return null;
        }

        String value = head.data;
        head = head.next;
        size--;

        if (head == null) {
            tail = null;
        }

        return value;
    }

    /**
     * Inspects the front element of the queue without removing it.
     *
     * @return the front string element, or null if the queue is empty
     */
    public synchronized String peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /**
     * Checks if the queue has no elements.
     *
     * @return true if empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return head == null || size == 0;
    }

    /**
     * Returns the number of elements in the queue.
     *
     * @return current size of the queue
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Clears all elements from the queue.
     */
    public synchronized void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Returns a snapshot list of current queue elements from FRONT to REAR.
     * Safe for display and JSON serialization without exposing internal Node pointers.
     *
     * @return list of strings from front to rear
     */
    public synchronized List<String> toList() {
        List<String> list = new ArrayList<>(size);
        Node current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    @Override
    public synchronized String toString() {
        return toList().toString();
    }
}
