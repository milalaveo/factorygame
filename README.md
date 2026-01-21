# Factory Game (Producer-Consumer)

## Overview
This project is a small Swing-based "Factory Game" that demonstrates the Producer-Consumer pattern with a bounded buffer. Suppliers produce hardware tasks (CPU/RAM/SSD/GPU), machines consume them, and a controller orchestrates lifecycle and statistics. The design focuses on clean OOP, thread safety, and clear separation of responsibilities.

## Goal
Allocate a fixed pool of resources across suppliers, machines, and buffer capacity, then choose a time limit. The processed-task target is fixed in code; when the processed count reaches the goal, the factory stops automatically and logs a goal completion message. If time runs out before the goal is met, the round ends as a loss.

## Architecture
- `app`: entry point (`Main`) to launch the UI.
- `domain.contract`: core interfaces (buffer, producer, consumer, event/log hooks).
- `domain.model`: task and event data models.
- `service.buffer`: bounded buffer with explicit locking and conditions.
- `service.producer` / `service.consumer`: suppliers and machines.
- `service.factory`: controller and stats aggregation.
- `shared`: configuration and utilities.
- `ui`: Swing UI (start/stop, configuration, logs, live stats).

## How the Buffer Blocks
`BoundedBuffer` uses a `ReentrantLock` with two `Condition`s:
- `put()` waits while the queue is full.
- `take()` waits while the queue is empty.
Both methods use `while` loops to guard against spurious wakeups.

## How to Run
This is a pure Java project

### Maven
```
mvn clean compile
mvn exec:java
```

### Manual (javac)
Windows PowerShell:
```
mkdir out
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out app.Main
```

macOS/Linux:
```
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out app.Main
```

## Thread Safety Notes
- Producers and consumers are stoppable and respond to interruption.
- The controller interrupts and joins all threads to avoid leaks.
- UI updates are marshaled to the EDT using `SwingUtilities.invokeLater`.
