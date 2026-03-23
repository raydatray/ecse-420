# ECSE-420 Assignment 2

We used Gradle to streamline the development of our assignment. The Gradle project contains multiple modules separated logically.

It includes:

- **Filter Lock**: A mutual exclusion algorithm using n-1 levels for n threads.
- **Bakery Lock**: Lamport's Bakery algorithm for mutual exclusion with ticket-based ordering.

---

## Project Structure

```bash
a2
├── bakery-lock
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           └── BakeryLock.java
│       └── test
│           └── java
│               └── solution
│                   └── BakeryLockTest.java
├── filter-lock
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           └── FilterLock.java
│       └── test
│           └── java
│               └── solution
│                   └── FilterLockTest.java
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
# Run bakery lock
just run-bakery

# Run filter lock
just run-filter

# Run tests for bakery lock
just test-bakery

# Run tests for filter lock
just test-filter

# Run all tests
just test
```

---

## Alternative: Using Gradle Directly

You can bypass just and run Gradle directly:

```bash
# Run bakery lock
./gradlew :bakery-lock:run

# Run filter lock
./gradlew :filter-lock:run

# Run tests for bakery lock
./gradlew :bakery-lock:test

# Run tests for filter lock
./gradlew :filter-lock:test

# Run all tests
./gradlew clean test
```
