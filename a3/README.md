# ECSE-420 Assignment 3

We used Gradle to streamline the development of our assignment. The Gradle project contains multiple modules separated logically.

It includes:

- **Matrix Multiplication**: Parallel matrix-vector multiplication using a `ForkJoinPool`, benchmarked against a sequential baseline across a range of thresholds.
- **Bounded Array Queue**: A bounded concurrent queue supporting concurrent enqueue and dequeue operations.
- **Hand-Over-Hand Lock**: A fine-grained linked-list set that acquires locks hand-over-hand to allow concurrent traversal and modification.

---

## Project Structure

```bash
a3
├── bounded-array-queue
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           └── BoundedArrayQueue.java
│       └── test
│           └── java
│               └── solution
│                   └── BoundedArrayQueueTest.java
├── hand-over-hand-lock
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           ├── FineGrainedListSet.java
│       │           └── ListNode.java
│       └── test
│           └── java
│               └── solution
│                   └── FineGrainedListSetTest.java
├── matrix-multiplication
│   ├── build.gradle.kts
│   └── src
│       └── main
│           └── java
│               ├── benchmark
│               │   └── Benchmark.java
│               └── implementation
│                   ├── ParallelMatrixVector.java
│                   └── SequentialMatrixVector.java
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── justfile
└── settings.gradle.kts
```

Each subproject is independent and configured with its own `build.gradle.kts`.
The root `justfile` provides shortcuts for running and testing all projects.

---

## Prerequisites

- **Java 21** (configured via Gradle toolchain)
- **Gradle Wrapper** (already included: `./gradlew`; no need to install Gradle manually)
- **just** (optional, for running commands via `justfile`)
- A UNIX-like shell (Linux/Mac). On Windows, use Git Bash or WSL, or run `gradlew.bat` in PowerShell.

---

## Running Programs

The `justfile` defines commands to run each assignment:

### Using just

```bash
# Run matrix multiplication benchmark
just run matrix

# Run tests for bounded array queue
just test-baq

# Run tests for hand-over-hand lock
just test-hohl

# Run all tests
just test
```

---

## Alternative: Using Gradle Directly

You can bypass just and run Gradle directly:

```bash
# Run matrix multiplication benchmark
./gradlew :matrix-multiplication:run

# Run tests for bounded array queue
./gradlew :bounded-array-queue:test

# Run tests for hand-over-hand lock
./gradlew :hand-over-hand-lock:test

# Run all tests
./gradlew clean test
```
