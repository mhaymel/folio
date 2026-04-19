# Unit Test Rules

## R-022a

Unit test classes must be named `<ClassUnderTest>Test`. Integration test classes must be named `<ClassUnderTest>IntegrationTest`. Do not use the `Test` prefix, the `Tests` suffix (plural), or any other variation.

**Bad:**

```java
final class TestStockService {
}

final class StockServiceTests {
}

final class StockServiceSpec {
}
```

**Good:**

```java
final class StockServiceTest {
}

final class QuoteFetcherIntegrationTest {
}
```

---

## R-022b

Test classes must be package-private and `final`. `public` test classes are forbidden.

**Bad:**

```java
public class StockServiceTest {
}
```

**Good:**

```java
final class StockServiceTest {
}
```

---

## R-022c

Test method names must follow the pattern `should<ExpectedBehaviour>[When<Condition>]`. Do not use a `test` prefix and do not use underscores (see also [R-011e](R-011-method-naming.md#r-011e)).

**Bad:**

```java
final class StockServiceTest {
    @Test
    void testFindByIsin() {
    }

    @Test
    void should_return_empty_when_isin_not_found() {
    }

    @Test
    void findByIsinReturnsEmpty() {
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnStockWhenIsinExists() {
    }

    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
    }
}
```

---

## R-022d

Every test method must follow the **Given / When / Then** structure, marked with `// given`, `// when`, and `// then` line comments. This keeps the intent of the arrangement, action, and assertion explicit.

**Exception:** when a test has no distinct action — for instance, a test that asserts initial state — combine the setup and action into a single `// given / when` marker rather than adding an empty `// when` block.

**Bad:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
        var service = new StockService(mockRepo);
        when(mockRepo.findByIsin("XX")).thenReturn(Optional.empty());
        var result = service.findByIsin("XX");
        assertThat(result).isEmpty();
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
        // given
        var service = new StockService(mockRepo);
        when(mockRepo.findByIsin("XX")).thenReturn(Optional.empty());

        // when
        var result = service.findByIsin("XX");

        // then
        assertThat(result).isEmpty();
    }
}
```

---

## R-022e

Use AssertJ's `assertThat(...)` exclusively for assertions. JUnit's `assertEquals`, `assertTrue`, `assertFalse`, `assertNotNull`, and similar assertions are forbidden. AssertJ offers richer, more readable failure messages and fluent chaining.

**Bad:**

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StockServiceTest {
    @Test
    void shouldReturnStock() {
        // given
        var service = new StockService(mockRepo);

        // when
        var stock = service.findByIsin("US0378331005");

        // then
        assertEquals("Apple", stock.name());
        assertTrue(stock.isActive());
    }
}
```

**Good:**

```java
import static org.assertj.core.api.Assertions.assertThat;

final class StockServiceTest {
    @Test
    void shouldReturnStock() {
        // given
        var service = new StockService(mockRepo);

        // when
        var stock = service.findByIsin("US0378331005");

        // then
        assertThat(stock.name()).isEqualTo("Apple");
        assertThat(stock.isActive()).isTrue();
    }
}
```

---

## R-022f

One concept per test. A test method verifies exactly one behaviour. Multiple `assertThat` calls are allowed only when they assert different facets of the same concept (e.g. all fields of a returned DTO).

**Bad:**

```java
final class StockServiceTest {
    @Test
    void shouldHandleIsinLookup() {
        // given
        var service = new StockService(mockRepo);

        // when / then — mixing two unrelated behaviours
        assertThat(service.findByIsin("US0378331005")).isPresent();
        assertThat(service.findByIsin("UNKNOWN")).isEmpty();
        assertThat(service.count()).isEqualTo(1);
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnStockWhenIsinExists() {
        // given
        var service = new StockService(mockRepo);

        // when
        var result = service.findByIsin("US0378331005");

        // then
        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
        // given
        var service = new StockService(mockRepo);

        // when
        var result = service.findByIsin("UNKNOWN");

        // then
        assertThat(result).isEmpty();
    }
}
```

---

## R-022g

Test methods must be independent. Do not share mutable state between test methods via non-`final` static fields, non-`final` instance fields, or external resources (files, shared database rows). Construct fresh fixtures inside each test method or in a `@BeforeEach` setup.

**Bad:**

```java
final class StockServiceTest {
    private static List<Stock> sharedStocks = new ArrayList<>();

    @Test
    void shouldAddStock() {
        // given
        sharedStocks.add(new Stock("AAPL"));

        // when / then
        assertThat(sharedStocks).hasSize(1);
    }

    @Test
    void shouldStartEmpty() {
        // then — depends on execution order
        assertThat(sharedStocks).isEmpty();
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldAddStock() {
        // given
        var stocks = new ArrayList<Stock>();

        // when
        stocks.add(new Stock("AAPL"));

        // then
        assertThat(stocks).hasSize(1);
    }

    @Test
    void shouldStartEmpty() {
        // given / when
        var stocks = new ArrayList<Stock>();

        // then
        assertThat(stocks).isEmpty();
    }
}
```

---

## R-022h

Do not use separator comments inside test classes (e.g. `// --- methodToBeTested ---`, `// === happy path ===`, `// --- edge cases ---`). Test method names are self-documenting. Let the method names and structure speak for themselves.

**Bad:**

```java
final class StockServiceTest {
    // --- findByIsin ---

    @Test
    void shouldReturnStockWhenIsinExists() {
    }

    // --- edge cases ---

    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnStockWhenIsinExists() {
    }

    @Test
    void shouldReturnEmptyWhenIsinNotFound() {
    }
}
```

---

## R-022i

Mock external dependencies (repositories, HTTP clients, the file system, the clock, schedulers). A unit test must not perform real network calls, real disk I/O, or rely on the system clock.

Use Mockito to mock collaborators; inject a fixed `Clock` for time-dependent code.

**Bad:**

```java
final class QuoteFetcherTest {
    @Test
    void shouldFetchQuote() {
        // given — hits the real network
        var fetcher = new QuoteFetcher(HttpClient.newHttpClient());

        // when
        var quote = fetcher.fetch(new Isin("US0378331005"));

        // then
        assertThat(quote).isPresent();
    }
}
```

**Good:**

```java
final class QuoteFetcherTest {
    @Test
    void shouldFetchQuote() {
        // given
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenReturn(stubResponse());
        var fetcher = new QuoteFetcher(httpClient);

        // when
        var quote = fetcher.fetch(new Isin("US0378331005"));

        // then
        assertThat(quote).isPresent();
    }
}
```

---

## R-022j

Every non-trivial production class must have a corresponding test class covering all public methods and constructors. "Trivial" means classes with no behaviour (pure marker interfaces, empty Spring configuration classes).

**Bad:** Adding a `StockService` without a `StockServiceTest`.

**Good:** Every new class ships with a `*Test` class covering its public API.

---

## R-022k

Disabled tests (`@Disabled`) must not be committed without an explicit reason given as the annotation value. A silently disabled test is dead code that rots.

**Bad:**

```java
final class StockServiceTest {
    @Test
    @Disabled
    void shouldReturnStockWhenIsinExists() {
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    @Disabled("flaky against live Yahoo API — see issue #42")
    void shouldReturnStockWhenIsinExists() {
    }
}
```

---

## R-022l

Do not assert on fields or state that the test did not set up. A test that asserts "whatever the production code happened to produce" documents incidental behaviour, not intended behaviour. Either arrange the value in the **given** block or do not assert it.

**Bad:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnStock() {
        // given — no stubbing of createdAt
        var service = new StockService(mockRepo);

        // when
        var stock = service.findByIsin("US0378331005");

        // then — asserts an incidental value
        assertThat(stock.createdAt()).isNotNull();
    }
}
```

**Good:**

```java
final class StockServiceTest {
    @Test
    void shouldReturnStockWithStubbedTimestamp() {
        // given
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(mockRepo.findByIsin("US0378331005"))
                .thenReturn(Optional.of(new Stock("Apple", createdAt)));
        var service = new StockService(mockRepo);

        // when
        var stock = service.findByIsin("US0378331005");

        // then
        assertThat(stock).contains(new Stock("Apple", createdAt));
    }
}
```

---