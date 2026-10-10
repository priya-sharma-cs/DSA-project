# Smart Temperature Analyzer (Monotonic Stack Demo)

A full-stack temperature analysis application solving the classic **Daily Temperatures** problem (LeetCode 739) using a **Monotonic Decreasing Stack** algorithm. Built with **Spring Boot 3** and modern web visualization technologies.

---

## 📋 Table of Contents

1. [Project Overview & Features](#-project-overview--features)
2. [Technologies Used](#-technologies-used)
3. [Project Structure](#-project-structure)
4. [Prerequisites](#-prerequisites)
5. [Database Setup & Configuration](#-database-setup--configuration)
6. [How to Run the Application](#-how-to-run-the-application)
7. [How to Use the Visualizer](#-how-to-use-the-visualizer)
8. [REST API Documentation](#-rest-api-documentation)
9. [How to Run Tests](#-how-to-run-tests)
10. [Algorithm Explanation & Complexity](#-algorithm-explanation--complexity)

---

## 🌟 Project Overview & Features

For any given sequence of daily temperatures, the system computes the number of days until the next warmer temperature for each day. If no future day is warmer, the waiting duration is `0`.

### Key Features:
* **High-Performance Algorithm:** Computes answers in **O(n)** time using a Monotonic Stack instead of an **O(n²)** brute force search.
* **Interactive UI Visualizer:**
  * Animated bar heights reflecting temperature variations.
  * Color coding: Sub-Zero (Cyan), Cool (Sky Blue), Mild (Emerald), Warm (Amber), Hot (Rose).
  * Interactive Day Inspector: Hover or click any day to highlight both that day and its next warmer day with step-by-step reasoning.
  * One-click presets: Standard, LeetCode 739, Strictly Increasing, Strictly Decreasing, Duplicates, Sub-Zero, Single Day.
  * Resilient offline fallback: If the backend is unreachable or the HTML is opened locally, a built-in JavaScript Monotonic Stack engine takes over automatically.
* **Robust Spring Boot Backend:**
  * Strict request validation and standardized JSON error responses (HTTP 400 Bad Request for nulls, malformed JSON, out-of-range arrays).
  * REST API endpoints for real-time analysis and historical query retrieval.
* **Dual Database Support:**
  * **H2 In-Memory (Default):** Runs immediately with zero configuration and zero external installations—ideal for demos and evaluations. Includes an accessible web console at `/h2-console`.
  * **MySQL Server (Production/Local):** Full persistence support via Spring profiles with externalized, secure credentials.

---

## 💻 Technologies Used

* **Java:** Version 17+ (Fully tested on Java 17 and Java 27)
* **Framework:** Spring Boot 3.5.6 (Spring Web, Spring Data JPA, Spring Validation)
* **Database:** 
  * H2 Database (In-Memory, default demo profile)
  * MySQL 8.0+ Connector (MySQL profile)
* **ORM:** Hibernate 6.6
* **Build Tool:** Apache Maven 3.9+ (includes genuine Maven Wrapper `mvnw` / `mvnw.cmd`)
* **Frontend:** HTML5, Modern Vanilla JavaScript (ES6+), Tailwind CSS
* **Testing:** JUnit 5, Mockito, Spring Test / MockMvc, AssertJ

---

## 📁 Project Structure

```text
SmartTemperatureAnalyzer/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── monotonic/
│   │   │           └── demo/
│   │   │               ├── MonotonicStackDemoApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── TemperatureController.java
│   │   │               ├── dto/
│   │   │               │   ├── TemperatureRequest.java
│   │   │               │   └── TemperatureResponse.java
│   │   │               ├── entity/
│   │   │               │   └── TemperatureAnalysis.java
│   │   │               ├── repository/
│   │   │               │   └── TemperatureAnalysisRepository.java
│   │   │               └── service/
│   │   │                   └── TemperatureService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-h2.properties
│   │       ├── application-mysql.properties
│   │       └── static/
│   │           └── index.html
│   └── test/
│       └── java/
│           └── com/
│               └── monotonic/
│                   └── demo/
│                       ├── MonotonicStackDemoApplicationTests.java
│                       ├── controller/
│                       │   └── TemperatureControllerTest.java
│                       └── service/
│                           └── TemperatureServiceTest.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## ⚙️ Prerequisites

1. **Java JDK 17 or newer:**
   Verify by running:
   ```bash
   java -version
   ```
2. **Maven (Optional):**
   The project includes the genuine Maven Wrapper (`mvnw` for Linux/macOS and `mvnw.cmd` for Windows), so installing a global Maven binary is **not required**.
3. **MySQL Server (Optional):**
   Only required if you choose to run with the `mysql` profile. The default `h2` profile runs in-memory without any external database server.

---

## 🗄️ Database Setup & Configuration

The application is configured to run out-of-the-box using Spring Profiles:

### 1. Default Profile: H2 In-Memory (Zero Setup)
By default, the application runs with the `h2` profile. It creates the schema automatically upon startup and requires no external server or credentials.

* **Console URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **JDBC URL:** `jdbc:h2:mem:smart_temperature`
* **User Name:** `sa`
* **Password:** *(leave blank)*

### 2. Optional Profile: MySQL Server
If you prefer running against a local or remote MySQL instance:

1. Create the MySQL database:
   ```sql
   CREATE DATABASE IF NOT EXISTS smart_temperature;
   ```
2. Set your database credentials using environment variables or application properties:
   * `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/smart_temperature?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   * `SPRING_DATASOURCE_USERNAME`: `root`
   * `SPRING_DATASOURCE_PASSWORD`: `your_mysql_password`

3. Launch the application with the `mysql` profile:
   ```bash
   # Windows PowerShell
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql

   # Linux / macOS
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 🚀 How to Run the Application

### Option A: Using the Maven Wrapper (Recommended)

**On Windows (PowerShell or Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Option B: Build and Run as a Standalone JAR

1. Package the executable JAR:
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```
2. Run the generated JAR:
   ```powershell
   java -jar target/SmartTemperatureAnalyzer-1.0-SNAPSHOT.jar
   ```

3. Open your browser and navigate to:
   ```text
   http://localhost:8080/
   ```

---

## 🎨 How to Use the Visualizer

1. **Enter Temperatures:** Type comma-separated integer temperatures (e.g., `73, 74, 75, 71, 69, 72, 76, 73`) in the input field.
2. **Use Presets:** Click any of the pre-configured buttons (`LeetCode 739`, `Strictly Increasing`, `Strictly Decreasing`, `With Duplicates`, `Sub-Zero`, `Single Day`) to load and run common test scenarios instantly.
3. **Click "Analyze":** The visualizer sends the data to the Spring Boot REST API (or calculates locally if offline), updates the metric cards, and animates the thermometer chart.
4. **Interactive Inspection:** Hover or click any day card:
   * The selected day is outlined in **Amber**.
   * The next warmer day is outlined in **Emerald Green**.
   * The inspector banner details exactly how many days to wait and why (e.g., *Day 2 (75°C) -> Next warmer day is Day 6 (76°C) -> Wait = 4 days*).
5. **View Database History:** Scroll to the "Database Analysis History" table. Click **"Replay"** on any past record to reload it into the visualizer.

---

## 📡 REST API Documentation

### 1. Analyze Temperatures
Computes the days until the next warmer temperature and records the analysis in the database.

* **Endpoint:** `POST /api/temperatures/analyze`
* **Content-Type:** `application/json`

#### Request Body:
```json
{
  "temperatures": [73, 74, 75, 71, 69, 72, 76, 73]
}
```

#### Successful Response (`200 OK`):
```json
{
  "temperatures": [73, 74, 75, 71, 69, 72, 76, 73],
  "waitingDays": [1, 1, 4, 2, 1, 1, 0, 0],
  "algorithm": "Monotonic Stack",
  "timeComplexity": "O(n)"
}
```

#### Error Response (`400 Bad Request`):
When temperatures are null, empty, or malformed:
```json
{
  "timestamp": "2026-10-09T23:25:22.918",
  "status": 400,
  "error": "Bad Request",
  "message": "Temperatures array must not be null",
  "path": "/api/temperatures/analyze"
}
```

---

### 2. Get Analysis History
Retrieves the 20 most recent analyses ordered from newest to oldest.

* **Endpoint:** `GET /api/temperatures/history`

#### Successful Response (`200 OK`):
```json
[
  {
    "id": 1,
    "temperatures": "[73, 74, 75, 71, 69, 72, 76, 73]",
    "waitingDays": "[1, 1, 4, 2, 1, 1, 0, 0]",
    "algorithm": "Monotonic Stack",
    "timeComplexity": "O(n)",
    "createdAt": "2026-10-09T23:25:31.046"
  }
]
```

---

## 🧪 How to Run Tests

The automated test suite contains **15 test cases** testing the core algorithm, edge cases, validation, REST controller responses, and Spring Boot context loading.

Run the entire test suite using:
```powershell
.\mvnw.cmd test
```

### Test Coverage Highlights:
* `TemperatureServiceTest`:
  * Standard LeetCode 739 Daily Temperatures example
  * Empty array input (`[]`)
  * Single element input (`[73]`)
  * Strictly increasing temperatures (`[30, 40, 50, 60]`)
  * Strictly decreasing temperatures (`[60, 50, 40, 30]`)
  * Duplicate temperatures (verifying equal temperatures do not qualify as warmer)
  * Negative / sub-zero temperatures (`[-5, -2, -10, -1]`)
  * Null safety validation (throws `IllegalArgumentException`)
  * Database persistence mapping verification
  * History query retrieval
* `TemperatureControllerTest`:
  * Valid POST requests return `200 OK` with matching JSON structure
  * Null temperature payloads return `400 Bad Request`
  * Malformed JSON requests return `400 Bad Request`
  * GET history requests return `200 OK` with JSON array
* `MonotonicStackDemoApplicationTests`:
  * Full Spring Boot context startup test with in-memory H2 database

---

## 🧠 Algorithm Explanation & Complexity

### Problem Statement
Given an array of daily temperatures `temperatures`, calculate an array `answer` such that `answer[i]` is the number of days you have to wait after the `i-th` day to experience a warmer temperature. If no future day is warmer, `answer[i] = 0`.

### Brute Force vs. Monotonic Stack

| Approach | Time Complexity | Space Complexity | Description |
| :--- | :---: | :---: | :--- |
| **Brute Force** | **O(n²)** | O(1) | For each day `i`, scan every forward day `j > i` until `temperatures[j] > temperatures[i]`. In the worst case (strictly decreasing temperatures), it compares $n(n-1)/2$ pairs. |
| **Monotonic Stack** | **O(n)** | O(n) | Iterates backward from day `n-1` to `0`, maintaining a stack of indices with strictly decreasing temperatures. |

### How the Monotonic Decreasing Stack Works:
1. Initialize an integer array `answer` of size `n` with all zeros.
2. Maintain a stack `stack` storing indices of future days.
3. Traverse the temperatures array backwards from `i = n - 1` down to `0`:
   * While the stack is not empty and `temperatures[stack.peek()] <= temperatures[i]`, pop the top index (since it is colder or equal and will never be the next warmer day for any day before day `i`).
   * If the stack is still not empty, `stack.peek() - i` is the distance to the next warmer day. Store this in `answer[i]`.
   * Push the current day index `i` onto the stack.
4. Return `answer`.

### Amortized O(n) Proof:
Although there is a `while` loop nested inside the `for` loop, **each index is pushed onto the stack exactly once and popped at most once** across the entire algorithm. The total number of stack operations across all iterations is at most $2n$, giving a strict linear time complexity of **O(n)**.
