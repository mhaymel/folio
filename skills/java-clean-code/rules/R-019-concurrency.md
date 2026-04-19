# Concurrency Rules

## R-019a

Prefer immutability over synchronization. Immutable state is inherently thread-safe and needs no locks, atomics, or `volatile` at all. Reach for synchronization primitives only after you have confirmed that mutable shared state is unavoidable.

**Bad:**

```java
final class PriceCache {
    private final Map<Isin, Money> prices;
    private final Object lock;

    private PriceCache(Map<Isin, Money> prices, Object lock) {
        this.prices = requireNonNull(prices);
        this.lock = requireNonNull(lock);
    }

    PriceCache() {
        this(new HashMap<>(), new Object());
    }

    void replaceAll(Map<Isin, Money> snapshot) {
        requireNonNull(snapshot);
        synchronized (lock) {
            prices.clear();
            prices.putAll(snapshot);
        }
    }

    Money findPrice(Isin isin) {
        requireNonNull(isin);
        synchronized (lock) {
            return prices.get(isin);
        }
    }
}
```

**Good:**

```java
record PriceSnapshot(Map<Isin, Money> prices) {
    PriceSnapshot {
        prices = Map.copyOf(requireNonNull(prices));
    }

    Money findPrice(Isin isin) {
        requireNonNull(isin);
        return prices.get(isin);
    }
}

final class PriceCache {
    private final AtomicReference<PriceSnapshot> snapshot;

    PriceCache(PriceSnapshot initial) {
        this.snapshot = new AtomicReference<>(requireNonNull(initial));
    }

    void replace(PriceSnapshot next) {
        snapshot.set(requireNonNull(next));
    }

    Money findPrice(Isin isin) {
        requireNonNull(isin);
        return snapshot.get().findPrice(isin);
    }
}
```

---

## R-019b

For single-variable shared state, use the atomic types from `java.util.concurrent.atomic` (`AtomicBoolean`, `AtomicInteger`, `AtomicLong`, `AtomicReference`) instead of `synchronized` blocks or `volatile` fields.

Reserve `synchronized` for the rare case where multiple fields must change together as one atomic unit.

**Bad:**

```java
final class FetchTracker {
    private boolean inProgress;
    private Instant lastScheduledFetch;

    synchronized boolean tryStart(Instant now) {
        if (inProgress) {
            return false;
        }
        inProgress = true;
        lastScheduledFetch = now;
        return true;
    }

    synchronized void done() {
        inProgress = false;
    }
}
```

**Good:**

```java
final class FetchTracker {
    private final AtomicBoolean inProgress;
    private final AtomicReference<Instant> lastScheduledFetch;

    private FetchTracker(AtomicBoolean inProgress, AtomicReference<Instant> lastScheduledFetch) {
        this.inProgress = requireNonNull(inProgress);
        this.lastScheduledFetch = requireNonNull(lastScheduledFetch);
    }

    FetchTracker() {
        this(new AtomicBoolean(false), new AtomicReference<>());
    }

    boolean tryStart(Instant now) {
        requireNonNull(now);
        if (!inProgress.compareAndSet(false, true)) {
            return false;
        }
        lastScheduledFetch.set(now);
        return true;
    }

    void done() {
        inProgress.set(false);
    }
}
```

---

## R-019c

Use the dedicated concurrent collections from `java.util.concurrent` (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `ConcurrentLinkedQueue`, `BlockingQueue`, …) when a collection is shared across threads. Do not wrap a regular collection with `Collections.synchronizedXxx(...)` and do not guard a plain `HashMap`/`ArrayList` with an external lock.

**Bad:**

```java
record QuoteEntry(Isin isin, Quote quote) {
    QuoteEntry {
        requireNonNull(isin);
        requireNonNull(quote);
    }
}

final class QuoteRegistry {
    private final Map<Isin, Quote> quotes;

    QuoteRegistry() {
        this.quotes = Collections.synchronizedMap(new HashMap<>());
    }

    void put(QuoteEntry entry) {
        requireNonNull(entry);
        quotes.put(entry.isin(), entry.quote());
    }

    Quote quote(Isin isin) {
        return quotes.get(isin);
    }
}
```

**Good:**

```java
record QuoteEntry(Isin isin, Quote quote) {
    QuoteEntry {
        requireNonNull(isin);
        requireNonNull(quote);
    }
}

final class QuoteRegistry {
    private final Map<Isin, Quote> quotes;

    private QuoteRegistry(Map<Isin, Quote> quotes) {
        this.quotes = requireNonNull(quotes);
    }

    QuoteRegistry() {
        this(new ConcurrentHashMap<>());
    }

    void put(QuoteEntry entry) {
        requireNonNull(entry);
        quotes.put(entry.isin(), entry.quote());
    }

    Quote quote(Isin isin) {
        return quotes.get(isin);
    }
}
```

---

## R-019d

Do not synchronize on `this`, on a class literal, on a `String` literal, or on a boxed primitive. Use a dedicated `private final Object lock = new Object();` — a lock that no caller can see and therefore no caller can contend on by accident.

**Bad:**

```java
final class ExportService {
    private final StringBuilder buffer;

    ExportService() {
        this.buffer = new StringBuilder();
    }

    void append(String row) {
        synchronized (this) { // callers can also lock on the instance
            buffer.append(row);
        }
    }

    void appendId(Long id) {
        synchronized (id) { // boxed primitive, shared across the JVM
            buffer.append(id);
        }
    }
}
```

**Good:**

```java
final class ExportService {
    private final StringBuilder buffer;
    private final Object bufferLock;

    private ExportService(StringBuilder buffer, Object bufferLock) {
        this.buffer = requireNonNull(buffer);
        this.bufferLock = requireNonNull(bufferLock);
    }

    ExportService() {
        this(new StringBuilder(), new Object());
    }

    void append(String row) {
        synchronized (bufferLock) {
            buffer.append(row);
        }
    }
}
```

---

## R-019e

Do not create threads directly with `new Thread(...)` or `Thread.ofPlatform().start(...)` in service code. Submit work to an `ExecutorService` (obtained from Spring or `Executors` factory methods) and let the pool own the thread lifecycle. Field types must be the interface (`Executor`, `ExecutorService`, `ScheduledExecutorService`), per [R-003o](R-003-class-field.md#r-003o).

**Bad:**

```java
final class ImportService {
    void importAsync(Path file) {
        new Thread(() -> parse(file)).start();
    }
}
```

**Good:**

```java
final class ImportService {
    private final ExecutorService executor;

    ImportService(ExecutorService executor) {
        this.executor = requireNonNull(executor);
    }

    void importAsync(Path file) {
        executor.submit(() -> parse(file));
    }
}
```

---

## R-019f

An `ExecutorService` that a class creates itself must be shut down by that class. Use try-with-resources (Java 19+ makes `ExecutorService` `AutoCloseable`) or an explicit `shutdown()` + `awaitTermination(...)` in a lifecycle method (`@PreDestroy`, `close()`). A pool whose owner disappears keeps its threads alive and leaks the JVM.

Executors injected by Spring are owned by the container — do not shut them down manually.

**Bad:**

```java
final class ReportRunner {
    private final ExecutorService executor;

    ReportRunner() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    void run(Report report) {
        executor.submit(() -> render(report));
    }
    // no shutdown — threads outlive the ReportRunner
}
```

**Good:**

```java
final class ReportRunner implements AutoCloseable {
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final ExecutorService executor;

    ReportRunner(ExecutorService executor) {
        this.executor = requireNonNull(executor);
    }

    void run(Report report) {
        executor.submit(() -> render(report));
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
```

---

## R-019g

When you catch `InterruptedException` and do not rethrow it, you must restore the interrupt status with `Thread.currentThread().interrupt()`. Swallowing the interrupt hides the cancellation signal from the rest of the call stack and the thread pool.

**Bad:**

```java
final class QuoteFetcher {
    void await(Duration timeout) {
        try {
            Thread.sleep(timeout.toMillis());
        } catch (InterruptedException exception) {
            // interrupt flag silently cleared — caller will never see it
        }
    }
}
```

**Good (restore the flag):**

```java
final class QuoteFetcher {
    void await(Duration timeout) {
        try {
            Thread.sleep(timeout.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FetchInterruptedException("sleep interrupted", exception);
        }
    }
}
```

**Good (propagate):**

```java
final class QuoteFetcher {
    void await(Duration timeout) throws InterruptedException {
        Thread.sleep(timeout.toMillis());
    }
}
```

---

## R-019h

Do not use `Thread.sleep` to schedule future work or to poll for a condition. Use a `ScheduledExecutorService`, Spring's `@Scheduled`, or a blocking primitive (`CountDownLatch.await`, `BlockingQueue.take`, `Future.get(timeout)`). `Thread.sleep` is only acceptable in test code and in back-off that surrounds an otherwise event-driven wait.

**Bad:**

```java
final class PriceSync {
    void runForever() {
        while (true) {
            fetchAndStore();
            try {
                Thread.sleep(60_000); // blocks a thread for a whole minute
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
```

**Good:**

```java
final class PriceSync {
    private final ScheduledExecutorService scheduler;

    PriceSync(ScheduledExecutorService scheduler) {
        this.scheduler = requireNonNull(scheduler);
    }

    void start() {
        scheduler.scheduleAtFixedRate(this::fetchAndStore, 0, 1, TimeUnit.MINUTES);
    }
}
```

---

## R-019i

Do not use `Object.wait`, `notify`, or `notifyAll`. They are low-level, error-prone (spurious wakeups, missed signals, easily misused without the surrounding lock), and always have a higher-level replacement: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `BlockingQueue`, `CompletableFuture`, or `Phaser`.

**Bad:**

```java
final class JobCoordinator {
    private final Object lock;
    private boolean isReady;

    JobCoordinator() {
        this.lock = new Object();
        this.isReady = false;
    }

    void awaitReady() throws InterruptedException {
        synchronized (lock) {
            while (!isReady) {
                lock.wait();
            }
        }
    }

    void markReady() {
        synchronized (lock) {
            isReady = true;
            lock.notifyAll();
        }
    }
}
```

**Good:**

```java
final class JobCoordinator {
    private final CountDownLatch ready;

    private JobCoordinator(CountDownLatch ready) {
        this.ready = requireNonNull(ready);
    }

    JobCoordinator() {
        this(new CountDownLatch(1));
    }

    void awaitReady() throws InterruptedException {
        ready.await();
    }

    void markReady() {
        ready.countDown();
    }
}
```

---

## R-019j

Do not implement double-checked locking for lazy initialization. It is easy 
to get wrong (missing `volatile`, partial publication) and unnecessary. 
Use `AtomicReference.updateAndGet` for instance-level lazy values.

**Bad:**

```java
final class ExchangeRates {
    private static ExchangeRates instance;

    static ExchangeRates get() {
        if (instance == null) {
            synchronized (ExchangeRates.class) {
                if (instance == null) {
                    instance = new ExchangeRates();
                }
            }
        }
        return instance;
    }
}
```

**Good (instance-level lazy value):**

```java
final class QuoteClient {
    private final AtomicReference<HttpClient> httpClient;

    private QuoteClient(AtomicReference<HttpClient> httpClient) {
        this.httpClient = requireNonNull(httpClient);
    }

    QuoteClient() {
        this(new AtomicReference<>());
    }

    HttpClient httpClient() {
        return httpClient.updateAndGet(existing -> existing != null ? existing : HttpClient.newHttpClient());
    }
}
```

---
