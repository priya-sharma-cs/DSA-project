# Queue Visualizer — FIFO Data Structure Project

An academic Java project demonstrating the **Queue Abstract Data Type (ADT)** built on the **First-In, First-Out (FIFO)** principle. This project includes a custom singly linked list queue implementation with $O(1)$ operations, a CLI console application, a lightweight Java HTTP web server with a REST API, an optional MySQL transaction logger with graceful offline fallback, and an interactive modern dark-mode web visualizer.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Data Structure & Algorithm](#data-structure--algorithm)
  - [Linked List Node Design](#linked-list-node-design)
  - [Time & Space Complexities](#time--space-complexities)
  - [Thread Safety](#thread-safety)
- [Project Directory Structure](#project-directory-structure)
- [System Requirements](#system-requirements)
- [Compilation & Execution Instructions](#compilation--execution-instructions)
  - [1. Recompiling From Source](#1-recompiling-from-source)
  - [2. Running the Web Visualizer (Recommended)](#2-running-the-web-visualizer-recommended)
  - [3. Running the Console (CLI) Application](#3-running-the-console-cli-application)
  - [4. Running Automated Tests](#4-running-automated-tests)
- [REST API Endpoints](#rest-api-endpoints)
- [Optional MySQL Database Configuration](#optional-mysql-database-configuration)
- [Troubleshooting & FAQs](#troubleshooting--faqs)

---

## Project Overview

In computer science, a **Queue** is a linear collection of elements that maintains ordering using the **FIFO (First-In, First-Out)** rule: the first element inserted is the first one removed.

Unlike naive projects that simply wrap `java.util.LinkedList`, this project implements a **genuine, custom linked list data structure** with explicit `Node` pointers (`head` and `tail`), ensuring true $O(1)$ constant time complexity for enqueue and dequeue operations without relying on standard library collections.

---

## Key Features

1. **Custom Linked List Queue (`CustomQueue.java`)**:
   * Uses custom `Node` objects with `head` (FRONT) and `tail` (REAR) references.
   * Direct implementations of `enqueue()`, `dequeue()`, `peek()`, `isEmpty()`, `size()`, and `clear()`.
   * Thread-safe using `synchronized` methods for multi-threaded HTTP server requests.
2. **Interactive Modern Web Visualizer**:
   * Dark charcoal/navy theme with subtle glassmorphic cards and glowing accents.
   * Real-time horizontal queue pipeline showing nodes, index numbers, and pointer arrows (`next`).
   * Distinct badges highlighting **`FRONT` (Head)** and **`REAR` (Tail)** elements, correctly handling single-item queues.
   * Live metrics: Queue Size, State (Empty vs Active), Head Value, and Tail Value.
   * Smooth CSS slide-in and exit animations.
3. **Dual Execution Modes**:
   * **Web Server Mode (`QueueServer`)**: JDK-native `HttpServer` serving on `http://localhost:8080`.
   * **CLI Console Mode (`AppMain`)**: Interactive terminal menu with keyboard inputs.
4. **Fault-Tolerant Database Persistence (`DatabaseManager.java`)**:
   * Logs queue operations (`ENQUEUE`, `DEQUEUE`) into MySQL `queue_history`.
   * **100% Graceful Fallback**: If MySQL is stopped or unconfigured, queue operations continue seamlessly in memory with zero crashes or slowdowns.
   * Credentials configured via environment variables with safe defaults (passwords never exposed).
5. **Zero External Framework Dependencies**:
   * Standard JDK SE only (no Spring, Maven, Gradle, or npm setup required).

---

## Data Structure & Algorithm

### Linked List Node Design

The queue uses a singly linked list architecture:

```text
 FRONT (Head)                                              REAR (Tail)
 [ Node 0 ]  ----next---->  [ Node 1 ]  ----next---->  [ Node 2 ]  ----next----> null
   (Leaves)                                               (Enters)
```

* **Enqueue (Insert at Rear)**: A new `Node` is created. If the queue is empty, both `head` and `tail` point to the new node. Otherwise, `tail.next` is updated to point to the new node, and `tail` moves to the new node.
* **Dequeue (Remove from Front)**: The value at `head` is captured. `head` advances to `head.next`. If `head` becomes `null`, `tail` is also set to `null`.
* **Peek (View Front)**: Returns `head.data` without modifying node pointers.

### Time & Space Complexities

| Operation | Method Call | Time Complexity | Space Complexity | Explanation |
| :--- | :--- | :---: | :---: | :--- |
| **Enqueue** | `queue.enqueue(val)` | **$O(1)$** | $O(1)$ | Direct insertion at `tail` pointer |
| **Dequeue** | `queue.dequeue()` | **$O(1)$** | $O(1)$ | Direct removal from `head` pointer |
| **Peek** | `queue.peek()` | **$O(1)$** | $O(1)$ | Direct inspection of `head.data` |
| **isEmpty** | `queue.isEmpty()` | **$O(1)$** | $O(1)$ | Single pointer check (`head == null`) |
| **Size** | `queue.size()` | **$O(1)$** | $O(1)$ | Maintained via integer counter |
| **Clear** | `queue.clear()` | **$O(1)$** | $O(1)$ | Resets `head`, `tail`, and `size` to 0 |
| **Overall Queue** | — | — | **$O(n)$** | Linear space proportional to elements |

### Thread Safety

Because the Java HTTP web server dispatches requests across multiple worker threads, every mutating and reading operation in `CustomQueue` is marked `synchronized`. This prevents race conditions and corrupted pointer states during simultaneous requests.

---

## Project Directory Structure

```text
Queue_Project_Submission/
├── src/com/queue/project/
│   ├── CustomQueue.java        # Core Queue ADT (singly linked list implementation)
│   ├── CustomQueueTest.java    # Standalone unit & concurrency test suite
│   ├── QueueServer.java        # HTTP web server & REST API controller
│   ├── AppMain.java            # Terminal CLI interactive application
│   └── DatabaseManager.java    # MySQL connector & history persistence helper
├── web/
│   ├── index.html              # Modern dark-mode visualization page
│   ├── style.css               # Styling, animations, and responsive layout
│   └── script.js               # Client controller connecting to REST API
├── lib/
│   └── mysql-connector-j-26.7.0.jar  # JDBC MySQL Connector driver
├── out/                        # Pre-compiled .class binaries (ready to run)
└── README.md                   # Comprehensive project documentation
```

---

## System Requirements

* **Java Development Kit (JDK)**: JDK 17, 21, or 27+ (Verified on JDK 27).
* **Web Browser**: Any modern browser (Google Chrome, Microsoft Edge, Mozilla Firefox, Safari).
* **Operating System**: Windows, macOS, or Linux.
* **Optional**: MySQL Server 8.0+ (only required if persistent database transaction history is desired).

---

## Compilation & Execution Instructions

Open your terminal or PowerShell and navigate to the project root folder:

```powershell
cd Queue_Project_Submission
```

### 1. Recompiling From Source

To recompile all Java classes into the `out` directory:

* **Windows (PowerShell / Command Prompt)**:
  ```powershell
  javac -cp "lib/*" -d out src/com/queue/project/*.java
  ```
* **macOS / Linux**:
  ```bash
  javac -cp "lib/*" -d out src/com/queue/project/*.java
  ```

---

### 2. Running the Web Visualizer (Recommended)

Start the web server:

* **Windows**:
  ```powershell
  java -cp "out;lib/*" com.queue.project.QueueServer
  ```
* **macOS / Linux**:
  ```bash
  java -cp "out:lib/*" com.queue.project.QueueServer
  ```

Once started, open your web browser and visit:
👉 **`http://localhost:8080`**

---

### 3. Running the Console (CLI) Application

If you prefer testing via terminal:

* **Windows**:
  ```powershell
  java -cp "out;lib/*" com.queue.project.AppMain
  ```
* **macOS / Linux**:
  ```bash
  java -cp "out:lib/*" com.queue.project.AppMain
  ```

Follow the numbered prompts (1 to 9) to enqueue, dequeue, peek, and inspect the queue.

---

### 4. Running Automated Tests

Run the standalone verification test suite (verifies FIFO ordering, edge cases, thread safety, and JSON handling):

* **Windows**:
  ```powershell
  java -cp "out;lib/*" com.queue.project.CustomQueueTest
  ```
* **macOS / Linux**:
  ```bash
  java -cp "out:lib/*" com.queue.project.CustomQueueTest
  ```

---

## REST API Endpoints

The web server exposes the following JSON endpoints:

| Endpoint | Method | Request Payload | Success Response | Description |
| :--- | :---: | :--- | :--- | :--- |
| `/` | `GET` | — | HTML Page | Serves the web interface |
| `/api/queue` | `GET` | — | `{"queue":["A","B"],"size":2,"front":"A","rear":"B"}` | Returns active queue items |
| `/api/enqueue` | `POST` | `{"value":"Item"}` | `{"success":true,"message":"Enqueued: Item","size":1}` | Enqueues value at tail |
| `/api/dequeue` | `POST` | — | `{"success":true,"message":"Dequeued: Item","value":"Item"}` | Dequeues value from head |
| `/api/peek` | `GET` | — | `{"success":true,"message":"Front: Item","value":"Item"}` | Inspects head element |
| `/api/clear` | `POST` | — | `{"success":true,"message":"Queue cleared successfully."}` | Clears all queue items |
| `/api/history` | `GET` | — | `{"history":[...],"databaseConnected":false}` | Retrieves MySQL audit log |
| `/api/history` | `DELETE`| — | `{"success":true,"message":"History cleared."}` | Clears MySQL audit log |

---

## Optional MySQL Database Configuration

> **Note**: A database is **NOT required** to run the queue. If MySQL is offline, the project automatically runs 100% in-memory.

To enable persistent database history logging:

1. **Start MySQL Server**:
   Ensure the MySQL service is running on `localhost:3306`.
2. **Create the Database**:
   Open MySQL Workbench or your MySQL terminal and run:
   ```sql
   CREATE DATABASE queuedb;
   ```
3. **Table Creation**:
   The application automatically creates the `queue_history` table on startup:
   ```sql
   USE queuedb;
   CREATE TABLE IF NOT EXISTS queue_history (
       id INT AUTO_INCREMENT PRIMARY KEY,
       operation VARCHAR(50),
       value VARCHAR(255),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```
4. **Environment Variables (Optional)**:
   You can configure database credentials without editing Java code:
   * `DB_HOST`: default `localhost`
   * `DB_PORT`: default `3306`
   * `DB_NAME`: default `queuedb`
   * `DB_USER`: default `root`
   * `DB_PASSWORD`: your MySQL password

---

## Troubleshooting & FAQs

1. **Port 8080 is already in use (`BindException`)**:
   * Another process is using port 8080.
   * On Windows PowerShell, find and terminate it:
     ```powershell
     Get-NetTCPConnection -LocalPort 8080
     Stop-Process -Id <OwningProcessId> -Force
     ```
   * Or run the server on a custom port using the `PORT` environment variable:
     ```powershell
     $env:PORT="9000"; java -cp "out;lib/*" com.queue.project.QueueServer
     ```
2. **"Database Notice: MySQL is offline" displayed**:
   * This is normal and expected when MySQL is not running.
   * Queue operations (Enqueue, Dequeue, Peek, Clear) are completely operational in memory.
3. **Class not found when running `java`**:
   * Ensure you include `-cp "out;lib/*"` (on Windows) or `-cp "out:lib/*"` (on macOS/Linux).
4. **Browser changes not appearing**:
   * Clear browser cache or press `Ctrl + F5` (Windows) / `Cmd + Shift + R` (Mac) to reload static CSS/JS files.
