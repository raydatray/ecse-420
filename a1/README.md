# ECSE-420 Assignment 1

We used Gradle to streamline the development of our assignment. The Gradle project contains multiple modules separated logically.

It includes:

- **Matrix Multiplication**: Sequential vs parallel matrix multiplication with benchmarking CLI.
- **Dining Philosophers**: Classic concurrency problem with both deadlock-prone and deadlock-free solutions.
- **Deadlock Examples**: Demonstrations of deadlock and resource ordering to prevent it.

---

## 📦 Project Structure

```bash
a1
├── deadlock
│   ├── build.gradle.kts
│   └── src
│       └── main
│           └── java
│               └── example
│                   ├── Deadlock.java
│                   └── ResourceOrdering.java
├── dining-philosophers
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           ├── BasePhilosopher.java
│       │           ├── DeadlockPhilosophers.java
│       │           └── SafePhilosophers.java
│       └── test
│           └── java
│               └── solution
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── justfile
├── matrix-multiplication
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           ├── BenchmarkCLI.java
│       │           └── MatrixMultiplication.java
│       └── test
│           └── java
│               └── solution
│                   └── MatrixMultiplicationTest.java
└── settings.gradle.kts
```

Each subproject is independent and configured with its own `build.gradle.kts`.
The root `justfile` provides shortcuts for running and testing all projects.

---

## 🚀 Prerequisites

- **Java 21** (configured via Gradle toolchain)
- **Gradle Wrapper** (already included: `./gradlew`; no need to install Gradle manually)
- **just** (optional, for running commands via `justfile`)
- A UNIX-like shell (Linux/Mac). On Windows, use Git Bash or WSL, or run `gradlew.bat` in PowerShell.

---

## ▶️ Running Programs

The `justfile` defines commands to run each assignment:

### Using just

```bash
# Run matrix multiplication benchmark
just run-matrix --mode threads
just run-matrix --mode size

# Run dining philosophers (deadlock-free)
just run-phil

# Run dining philosophers (deadlock-prone)
just run-phil-deadlock

# Run deadlock example
just run-deadlock

# Run resource ordering example
just run-ordering

# Run unit tests
just test
```

---

## ⚙️ Alternative: Using Gradle Directly

You can bypass just and run Gradle directly:

```bash
# Matrix multiplication benchmarks
./gradlew :matrix-multiplication:run --args="--mode threads"
./gradlew :matrix-multiplication:run --args="--mode size"
./gradlew :matrix-multiplication:run --args="--mode threads --matrix-size 4000 --min-threads 1 --max-threads 16"

# Dining philosophers
./gradlew :dining-philosophers:run -PmainClass=solution.SafePhilosophers
./gradlew :dining-philosophers:run -PmainClass=solution.DeadlockPhilosophers

# Deadlock examples
./gradlew :deadlock:run -PmainClass=example.Deadlock
./gradlew :deadlock:run -PmainClass=example.ResourceOrdering

# Run tests
./gradlew clean test
```

---

## 🧪 Matrix Multiplication CLI

The matrix multiplication module includes a benchmarking CLI with two modes:

### Thread Benchmark Mode
Measures speedup across different thread counts for a fixed matrix size.

```bash
./gradlew :matrix-multiplication:run --args="--mode threads"
```

Options:
- `--matrix-size <n>` — Matrix dimension (default: 4000)
- `--min-threads <n>` — Minimum thread count (default: 1)
- `--max-threads <n>` — Maximum thread count (default: available processors)

### Size Benchmark Mode
Measures performance across different matrix sizes with a fixed thread count.

```bash
./gradlew :matrix-multiplication:run --args="--mode size"
```

This mode uses predefined sizes: 100, 200, 500, 1000, 2000, 3000, 4000.

---

## 📚 Notes

- All concurrency programs (dining philosophers, deadlock examples) run indefinitely by design; use `Ctrl+C` to stop them.
- The matrix multiplication benchmarks may take significant time for larger sizes.
- Deadlock-prone versions may hang as expected—this is intentional to demonstrate deadlock behavior.