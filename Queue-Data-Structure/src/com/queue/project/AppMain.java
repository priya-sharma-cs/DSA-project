package com.queue.project;

import java.util.Scanner;

/**
 * AppMain provides an interactive Command Line Interface (CLI) for testing
 * and demonstrating the CustomQueue data structure.
 */
public class AppMain {

    public static final CustomQueue queue = new CustomQueue();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Attempt optional database initialization
        DatabaseManager.createTable();

        while (true) {
            System.out.println("\n========== QUEUE DATA STRUCTURE (FIFO) ==========");
            System.out.println("1. Enqueue (Insert at Rear)");
            System.out.println("2. Dequeue (Remove from Front)");
            System.out.println("3. Peek (View Front)");
            System.out.println("4. Display Queue (Front -> Rear)");
            System.out.println("5. Queue Size");
            System.out.println("6. Clear Queue");
            System.out.println("7. View Database History");
            System.out.println("8. Clear Database History");
            System.out.println("9. Exit");
            System.out.print("Enter your choice (1-9): ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number (1-9).");
                sc.nextLine();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter value to enqueue: ");
                    String value = sc.nextLine();
                    if (value == null || value.trim().isEmpty()) {
                        System.out.println("Warning: Cannot enqueue an empty value.");
                        break;
                    }
                    if (value.length() > 255) {
                        System.out.println("Warning: Value exceeds 255 characters limit.");
                        break;
                    }
                    queue.enqueue(value.trim());
                    System.out.println("Enqueued successfully: \"" + value.trim() + "\"");
                    DatabaseManager.saveOperation("ENQUEUE", value.trim());
                }
                case 2 -> {
                    if (queue.isEmpty()) {
                        System.out.println("Queue is empty. Cannot dequeue.");
                    } else {
                        String removed = queue.dequeue();
                        System.out.println("Dequeued from front: \"" + removed + "\"");
                        DatabaseManager.saveOperation("DEQUEUE", removed);
                    }
                }
                case 3 -> {
                    if (queue.isEmpty()) {
                        System.out.println("Queue is empty. Front has no elements.");
                    } else {
                        System.out.println("Front (Peek): \"" + queue.peek() + "\"");
                    }
                }
                case 4 -> {
                    if (queue.isEmpty()) {
                        System.out.println("Queue is empty: []");
                    } else {
                        System.out.println("Queue [FRONT -> REAR]: " + queue.toList());
                    }
                }
                case 5 -> {
                    System.out.println("Current Queue Size: " + queue.size());
                }
                case 6 -> {
                    queue.clear();
                    System.out.println("Queue has been cleared.");
                }
                case 7 -> {
                    DatabaseManager.showHistory();
                }
                case 8 -> {
                    if (DatabaseManager.clearHistory()) {
                        System.out.println("Database queue history cleared.");
                    } else {
                        System.out.println("Notice: Could not clear history (MySQL is offline).");
                    }
                }
                case 9 -> {
                    System.out.println("Exiting Queue CLI. Goodbye!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid choice! Please choose an option between 1 and 9.");
            }
        }
    }
}