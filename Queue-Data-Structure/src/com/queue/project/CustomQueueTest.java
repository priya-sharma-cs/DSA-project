package com.queue.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Self-contained unit & integration test runner for CustomQueue.
 * Requires only standard JDK (no external JUnit dependency required).
 */
public class CustomQueueTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        RUNNING CUSTOM QUEUE TEST SUITE           ");
        System.out.println("==================================================");

        runTest("Test Empty Queue", CustomQueueTest::testEmptyQueue);
        runTest("Test Basic FIFO Order", CustomQueueTest::testBasicFIFO);
        runTest("Test Single Item Lifecycle", CustomQueueTest::testSingleItemLifecycle);
        runTest("Test Duplicate Items", CustomQueueTest::testDuplicateItems);
        runTest("Test Clear Operation", CustomQueueTest::testClearOperation);
        runTest("Test Special Characters and Quotes", CustomQueueTest::testSpecialCharacters);
        runTest("Test Concurrent Multi-Thread Safety", CustomQueueTest::testConcurrency);
        runTest("Test JSON String Parser", CustomQueueTest::testJsonParser);

        System.out.println("==================================================");
        System.out.printf("Test Summary: %d / %d Tests Passed.\n", testsPassed, testsRun);
        System.out.println("==================================================");

        if (testsPassed != testsRun) {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100% OK)");
        }
    }

    private static void runTest(String name, Runnable test) {
        testsRun++;
        try {
            test.run();
            testsPassed++;
            System.out.println(" [PASS] " + name);
        } catch (Throwable t) {
            System.err.println(" [FAIL] " + name + " -> " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(String.format("%s (Expected: %s, Actual: %s)", message, expected, actual));
    }

    private static void testEmptyQueue() {
        CustomQueue q = new CustomQueue();
        assertTrue(q.isEmpty(), "Queue should initially be empty");
        assertEquals(0, q.size(), "Queue size should be 0");
        assertEquals(null, q.peek(), "Peek on empty queue should return null");
        assertEquals(null, q.dequeue(), "Dequeue on empty queue should return null");
        assertEquals(0, q.toList().size(), "toList() should be empty list");
    }

    private static void testBasicFIFO() {
        CustomQueue q = new CustomQueue();
        q.enqueue("First");
        q.enqueue("Second");
        q.enqueue("Third");

        assertEquals(3, q.size(), "Size should be 3");
        assertTrue(!q.isEmpty(), "Queue should not be empty");
        assertEquals("First", q.peek(), "Peek should return the head element");

        assertEquals("First", q.dequeue(), "First dequeue must return 'First'");
        assertEquals("Second", q.peek(), "Peek should now return 'Second'");
        assertEquals(2, q.size(), "Size should decrease to 2");

        assertEquals("Second", q.dequeue(), "Second dequeue must return 'Second'");
        assertEquals("Third", q.dequeue(), "Third dequeue must return 'Third'");

        assertTrue(q.isEmpty(), "Queue should be empty after dequeuing all items");
        assertEquals(0, q.size(), "Size should be 0");
        assertEquals(null, q.dequeue(), "Subsequent dequeue should return null");
    }

    private static void testSingleItemLifecycle() {
        CustomQueue q = new CustomQueue();
        q.enqueue("Solo");
        assertEquals(1, q.size(), "Size should be 1");
        assertEquals("Solo", q.peek(), "Peek should return 'Solo'");
        assertEquals("Solo", q.dequeue(), "Dequeue should return 'Solo'");
        assertTrue(q.isEmpty(), "Queue should be empty");
        assertEquals(null, q.peek(), "Peek should be null");
        assertEquals(null, q.dequeue(), "Dequeue should be null");
    }

    private static void testDuplicateItems() {
        CustomQueue q = new CustomQueue();
        q.enqueue("Dup");
        q.enqueue("Dup");
        q.enqueue("Dup");

        assertEquals(3, q.size(), "Size should be 3 with duplicates");
        assertEquals("Dup", q.dequeue(), "Should dequeue first duplicate");
        assertEquals("Dup", q.dequeue(), "Should dequeue second duplicate");
        assertEquals("Dup", q.dequeue(), "Should dequeue third duplicate");
        assertTrue(q.isEmpty(), "Queue should now be empty");
    }

    private static void testClearOperation() {
        CustomQueue q = new CustomQueue();
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        assertEquals(3, q.size(), "Size should be 3");

        q.clear();
        assertTrue(q.isEmpty(), "Queue should be empty after clear()");
        assertEquals(0, q.size(), "Size should be 0 after clear()");
        assertEquals(null, q.peek(), "Peek should be null after clear()");
        assertEquals(null, q.dequeue(), "Dequeue should be null after clear()");

        // Can still enqueue after clearing
        q.enqueue("D");
        assertEquals(1, q.size(), "Size should be 1 after new enqueue");
        assertEquals("D", q.dequeue(), "Should dequeue 'D'");
    }

    private static void testSpecialCharacters() {
        CustomQueue q = new CustomQueue();
        String str1 = "Quotes: \"hello\" & 'world'";
        String str2 = "Slashes: \\ and / and newlines \n\t";
        String str3 = "Symbols: !@#$%^&*()_+{}[]:;<>,.?/~`";

        q.enqueue(str1);
        q.enqueue(str2);
        q.enqueue(str3);

        assertEquals(str1, q.dequeue(), "Should preserve string with quotes");
        assertEquals(str2, q.dequeue(), "Should preserve string with slashes/newlines");
        assertEquals(str3, q.dequeue(), "Should preserve string with symbols");
    }

    private static void testConcurrency() {
        CustomQueue q = new CustomQueue();
        int threadCount = 8;
        int operationsPerThread = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        q.enqueue("T" + threadId + "-" + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Concurrent enqueue should finish within timeout");
        } catch (InterruptedException e) {
            throw new AssertionError("Thread interrupted during concurrent test", e);
        } finally {
            pool.shutdown();
        }

        int expectedTotal = threadCount * operationsPerThread;
        assertEquals(expectedTotal, q.size(), "Queue size after concurrent enqueues must match total items");

        // Verify elements can be completely drained
        int dequeuedCount = 0;
        while (q.dequeue() != null) {
            dequeuedCount++;
        }
        assertEquals(expectedTotal, dequeuedCount, "Total dequeued items must match enqueued total");
        assertTrue(q.isEmpty(), "Queue should be empty after draining");
    }

    private static void testJsonParser() {
        String json1 = "{\"value\":\"Simple text\"}";
        assertEquals("Simple text", QueueServer.extractJsonString(json1, "value"), "Should parse simple value");

        String json2 = "  { \n \"value\" : \"With escaped \\\"quotes\\\" and \\\\backslashes\" } ";
        assertEquals("With escaped \"quotes\" and \\backslashes", QueueServer.extractJsonString(json2, "value"), "Should parse escaped quotes and slashes");

        String json3 = "{\"invalid\": 123}";
        assertEquals(null, QueueServer.extractJsonString(json3, "value"), "Missing value should return null");
    }
}
