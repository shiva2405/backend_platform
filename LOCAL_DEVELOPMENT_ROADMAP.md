# ShopEase - Amazon-Like Features Development Roadmap

> **Note**: This file is for local development reference only. NOT to be pushed to Git.

## Current Architecture (5 Microservices)

```
Frontend (5173)
    │
    ▼
┌─────────────────────┐
│   BFF Service       │  Port: 8080
│   (API Gateway)     │  Stateless - No DB
│   - JWT Validation  │
│   - Request Routing │
│   - Orchestration   │
└─────────┬───────────┘
          │
    ┌─────┴─────┬─────────────┬─────────────┐
    ▼           ▼             ▼             ▼
┌────────┐ ┌────────┐   ┌──────────┐  ┌──────────┐
│Identity│ │Inventory│   │  Cart    │  │  Order   │
│Service │ │Service  │   │ Service  │  │ Service  │
│:8084   │ │:8081    │   │ :8082    │  │ :8085    │
│        │ │         │   │          │  │          │
│H2:     │ │H2:      │   │H2:       │  │H2:       │
│identity│ │inventory│   │cartdb    │  │orderdb   │
│db      │ │db       │   │          │  │          │
└────────┘ └─────────┘   └──────────┘  └──────────┘
```

---

## AMAZON-LIKE FEATURES TO BUILD (Backend Specific, Offline, Complex)

### 🏗️ PHASE 1: FOUNDATION (Week 1-2)

#### 1. Custom Distributed Cache Layer
**Complexity**: High | **Offline**: Yes

Build a custom caching system without Redis/Memcached:

```java
// Core Components to Build:
1. CacheEntry<K, V>
   - Key, Value, TTL, CreatedAt, AccessCount
   
2. LRUEvictionPolicy
   - LinkedHashMap with access-order
   - MaxSize configuration
   - Custom eviction listeners
   
3. WriteBackCache (Write-behind)
   - Dirty flag tracking
   - Batch write scheduler
   - Async persistence
   
4. CacheRegion
   - Products, Users, Sessions, Cart
   - Per-region TTL and size limits
   
5. CacheStatistics
   - Hit/Miss ratio
   - Eviction count
   - Memory usage estimation
```

**Implementation**:
```
cache-service/
├── src/main/java/com/example/cache/
│   ├── core/
│   │   ├── CacheEntry.java
│   │   ├── CacheStore.java
│   │   ├── CacheRegion.java
│   │   └── CacheConfiguration.java
│   ├── eviction/
│   │   ├── EvictionPolicy.java
│   │   ├── LRUEvictionPolicy.java
│   │   ├── LFUEvictionPolicy.java
│   │   └── TTLEvictionPolicy.java
│   ├── persistence/
│   │   ├── WriteBehindQueue.java
│   │   └── SnapshotManager.java
│   └── monitoring/
│       ├── CacheMetrics.java
│       └── CacheHealthIndicator.java
```

---

#### 2. Custom Message Queue System
**Complexity**: High | **Offline**: Yes

Build async processing without RabbitMQ/Kafka:

```java
// Queue Components:
1. MessageQueue<T>
   - BlockingQueue implementation
   - Priority support
   - Message persistence to file
   
2. DeadLetterQueue
   - Failed message storage
   - Retry counter
   - Manual reprocessing API
   
3. MessageConsumer
   - Consumer groups
   - Acknowledgment handling
   - Retry with exponential backoff
   
4. MessageProducer
   - Async publishing
   - Batch publishing
   - Delivery confirmation
```

**Use Cases**:
- Order processing (PENDING → CONFIRMED → SHIPPED)
- Email notifications
- Inventory updates
- Cart abandonment detection

---

#### 3. Database Migration to PostgreSQL
**Complexity**: Medium | **Offline**: Yes (local PostgreSQL)

```yaml
# docker-compose.yml (local only)
version: '3.8'
services:
  postgres-identity:
    image: postgres:15
    environment:
      POSTGRES_DB: identitydb
      POSTGRES_USER: shopease
      POSTGRES_PASSWORD: local_dev_only
    ports:
      - "5432:5432"
      
  postgres-inventory:
    image: postgres:15
    environment:
      POSTGRES_DB: inventorydb
    ports:
      - "5433:5432"
      
  postgres-order:
    image: postgres:15
    environment:
      POSTGRES_DB: orderdb
    ports:
      - "5434:5432"
```

**Migration Strategy**:
1. Flyway migration scripts per service
2. Read replicas for heavy read operations
3. Connection pooling with HikariCP tuning

---

### 🔥 PHASE 2: CORE E-COMMERCE (Week 3-4)

#### 4. Inventory Management System
**Complexity**: High | **Offline**: Yes

```java
// Stock Management Components:
1. StockReservation
   - Reserve stock when added to cart
   - Release after timeout (30 min)
   - Decrement on order confirmation
   
2. InventoryTransaction
   - RESERVE, RELEASE, DECREMENT, RESTOCK
   - Audit trail with timestamps
   - Idempotency keys to prevent double processing
   
3. LowStockAlert
   - Configurable thresholds per product
   - Alert queue when stock < threshold
   
4. WarehouseAllocation
   - Multi-warehouse support
   - Nearest warehouse selection
   - Stock transfer between warehouses
```

**Database Schema**:
```sql
-- inventory_transactions
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    reference_id VARCHAR(50), -- order_id or cart_id
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- stock_reservations
CREATE TABLE stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

---

#### 5. Order State Machine
**Complexity**: High | **Offline**: Yes

```
Order States:
┌─────────┐
│ CREATED │
└────┬────┘
     │ validate_payment()
     ▼
┌──────────┐    ┌────────────┐
│ PENDING  │───▶│ CANCELLED  │
└────┬─────┘    └────────────┘
     │ payment_confirmed()
     ▼
┌───────────┐
│ CONFIRMED │
└─────┬─────┘
      │ start_processing()
      ▼
┌────────────┐
│ PROCESSING │
└─────┬──────┘
      │ ship_order()
      ▼
┌─────────┐
│ SHIPPED │
└────┬────┘
     │ confirm_delivery()
     ▼
┌───────────┐
│ DELIVERED │
└───────────┘
```

**State Machine Implementation**:
```java
public class OrderStateMachine {
    private final Map<OrderStatus, Set<OrderStatus>> validTransitions;
    private final Map<OrderStatus, Consumer<Order>> onEnterActions;
    private final Map<OrderStatus, Consumer<Order>> onExitActions;
    
    public Order transition(Order order, OrderStatus targetStatus) {
        validateTransition(order.getStatus(), targetStatus);
        executeExitActions(order);
        order.setStatus(targetStatus);
        executeEnterActions(order);
        publishStateChangeEvent(order);
        return order;
    }
}
```

---

#### 6. Payment Integration Abstraction
**Complexity**: Medium | **Offline**: Yes (Mock payments)

```java
// Payment Gateway Interface
public interface PaymentGateway {
    PaymentResult authorize(PaymentRequest request);
    PaymentResult capture(String authorizationId, Money amount);
    PaymentResult refund(String transactionId, Money amount);
    PaymentResult void(String authorizationId);
}

// Implementations (all local/mock for offline)
public class MockPaymentGateway implements PaymentGateway {
    // Simulates success/failure based on card number patterns
    // 4111111111111111 → Always succeeds
    // 4000000000000002 → Always declines
    // 4000000000000010 → Random failure (30%)
}
```

---

### 🚀 PHASE 3: ADVANCED FEATURES (Week 5-6)

#### 7. Search & Filter Engine
**Complexity**: High | **Offline**: Yes

Build without Elasticsearch:

```java
// Components:
1. InvertedIndex
   - Term → Document IDs mapping
   - TF-IDF scoring
   - Phrase matching support
   
2. FacetedSearch
   - Category facets
   - Price range facets
   - Rating facets
   - Dynamic facet counts
   
3. AutocompleteService
   - Trie-based suggestions
   - Fuzzy matching (Levenshtein distance)
   - Popular searches ranking
   
4. SearchRanking
   - Relevance score
   - Popularity boost
   - Price factor
   - Recency factor
```

**Search Algorithm**:
```java
public class SearchEngine {
    private final InvertedIndex invertedIndex;
    private final FacetCalculator facetCalculator;
    
    public SearchResult search(SearchQuery query) {
        // 1. Tokenize and normalize query
        List<String> tokens = tokenize(query.getText());
        
        // 2. Find matching documents
        Set<Long> candidates = invertedIndex.findDocuments(tokens);
        
        // 3. Score and rank
        List<ScoredDocument> ranked = candidates.stream()
            .map(id -> new ScoredDocument(id, calculateScore(id, tokens)))
            .sorted(Comparator.comparing(ScoredDocument::getScore).reversed())
            .collect(toList());
        
        // 4. Apply filters
        ranked = applyFilters(ranked, query.getFilters());
        
        // 5. Calculate facets
        Map<String, List<FacetValue>> facets = facetCalculator.calculate(ranked);
        
        return new SearchResult(ranked, facets);
    }
}
```

---

#### 8. Recommendation Engine
**Complexity**: High | **Offline**: Yes

```java
// Recommendation Strategies:
1. CollaborativeFiltering
   - User-based: "Users like you also bought..."
   - Item-based: "Frequently bought together"
   
2. ContentBasedFiltering
   - Category similarity
   - Price range similarity
   - Brand affinity
   
3. HybridRecommender
   - Weighted combination of strategies
   - A/B testing support
```

**Data Model**:
```sql
-- user_interactions (for collaborative filtering)
CREATE TABLE user_interactions (
    user_id BIGINT,
    product_id BIGINT,
    interaction_type VARCHAR(20), -- VIEW, ADD_CART, PURCHASE, REVIEW
    weight DECIMAL(3,2),
    created_at TIMESTAMP
);

-- product_similarities (precomputed)
CREATE TABLE product_similarities (
    product_id_a BIGINT,
    product_id_b BIGINT,
    similarity_score DECIMAL(5,4),
    PRIMARY KEY (product_id_a, product_id_b)
);
```

---

#### 9. Rate Limiting & Throttling
**Complexity**: Medium | **Offline**: Yes

```java
// Token Bucket Implementation
public class RateLimiter {
    private final Map<String, TokenBucket> buckets;
    
    public boolean tryAcquire(String clientId, int tokens) {
        TokenBucket bucket = buckets.computeIfAbsent(
            clientId, 
            k -> new TokenBucket(MAX_TOKENS, REFILL_RATE)
        );
        return bucket.tryConsume(tokens);
    }
}

// Sliding Window Counter (for API endpoints)
public class SlidingWindowRateLimiter {
    private final int windowSizeSeconds;
    private final int maxRequests;
    private final Map<String, LinkedList<Long>> requestTimestamps;
}
```

**Rate Limit Rules**:
```yaml
rate-limits:
  - endpoint: /api/auth/login
    limit: 5/minute
    per: IP
    
  - endpoint: /api/orders/checkout
    limit: 10/minute
    per: USER
    
  - endpoint: /api/products
    limit: 100/minute
    per: IP
```

---

#### 10. Circuit Breaker Pattern
**Complexity**: Medium | **Offline**: Yes

```java
public class CircuitBreaker {
    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final AtomicInteger failureCount;
    private volatile CircuitState state;
    private volatile long lastFailureTime;
    
    public <T> T execute(Supplier<T> action, Supplier<T> fallback) {
        if (state == CircuitState.OPEN) {
            if (shouldAttemptReset()) {
                state = CircuitState.HALF_OPEN;
            } else {
                return fallback.get();
            }
        }
        
        try {
            T result = action.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            return fallback.get();
        }
    }
}
```

---

### 📊 PHASE 4: OBSERVABILITY (Week 7)

#### 11. Distributed Tracing (Custom)
**Complexity**: High | **Offline**: Yes

```java
// Trace Components:
1. TraceContext
   - TraceId (UUID across all services)
   - SpanId (current operation)
   - ParentSpanId (caller operation)
   
2. TraceInterceptor
   - Inject trace headers on outgoing calls
   - Extract trace headers on incoming calls
   
3. SpanCollector
   - In-memory buffer
   - Periodic flush to file/DB
   
4. TraceVisualization
   - REST API to query traces
   - Waterfall view of spans
```

**Header Propagation**:
```
X-Trace-Id: abc123
X-Span-Id: span456
X-Parent-Span-Id: span789
```

---

#### 12. Analytics & Reporting Dashboard
**Complexity**: Medium | **Offline**: Yes

```java
// Analytics Events:
1. PageView
2. ProductView
3. AddToCart
4. RemoveFromCart
5. Checkout
6. OrderComplete
7. Search

// Precomputed Aggregations:
1. DailySales
2. TopProducts (by views, sales, revenue)
3. CategoryPerformance
4. UserCohorts
5. ConversionFunnel
```

**Materialized Views**:
```sql
-- Hourly sales aggregation
CREATE MATERIALIZED VIEW hourly_sales AS
SELECT 
    DATE_TRUNC('hour', order_date) as hour,
    COUNT(*) as order_count,
    SUM(total_amount) as revenue,
    AVG(total_amount) as avg_order_value
FROM orders
WHERE status != 'CANCELLED'
GROUP BY DATE_TRUNC('hour', order_date);
```

---

### 🔐 PHASE 5: SECURITY (Week 8)

#### 13. Advanced Authentication
**Complexity**: High | **Offline**: Yes

```java
// Features:
1. Refresh Tokens
   - Short-lived access token (15 min)
   - Long-lived refresh token (7 days)
   - Token rotation on refresh
   
2. Multi-Factor Authentication (TOTP)
   - Google Authenticator compatible
   - Backup codes
   
3. Session Management
   - Track active sessions
   - Remote logout capability
   - Device fingerprinting
   
4. Brute Force Protection
   - Account lockout after N failures
   - Exponential backoff
   - CAPTCHA integration (mock)
```

---

#### 14. Audit Trail System
**Complexity**: Medium | **Offline**: Yes

```java
// Audit Events:
@AuditLog(action = "PRODUCT_CREATED")
@AuditLog(action = "ORDER_STATUS_CHANGED")
@AuditLog(action = "USER_ROLE_CHANGED")

// Audit Entry:
public class AuditEntry {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String userId;
    private String oldValue; // JSON
    private String newValue; // JSON
    private String ipAddress;
    private LocalDateTime timestamp;
}
```

---

### 📦 PHASE 6: ADDITIONAL FEATURES (Week 9-10)

#### 15. Wishlist Service
```java
// Features:
- Add/Remove from wishlist
- Move to cart
- Price drop notifications
- Share wishlist
```

#### 16. Reviews & Ratings System
```java
// Features:
- Star ratings (1-5)
- Text reviews
- Helpful votes
- Verified purchase badge
- Review moderation queue
- Aggregate rating calculation
```

#### 17. Promotions & Discount Engine
```java
// Discount Types:
1. PercentageOff (10% off)
2. FixedAmountOff ($20 off)
3. BuyXGetY (Buy 2 Get 1 Free)
4. FreeShipping
5. BundleDiscount

// Rules Engine:
- Minimum order value
- Maximum discount cap
- User segment targeting
- Product category restrictions
- Usage limits (per user, total)
- Date/time validity
```

#### 18. Notification Service
```java
// Channels (all mock/logged for offline):
1. Email (logs to file)
2. SMS (logs to file)
3. In-App (stored in DB)
4. Push (stored in DB)

// Templates:
- Order Confirmation
- Shipping Update
- Password Reset
- Price Drop Alert
- Cart Abandonment
- Review Request
```

---

## EFFORT ESTIMATES

| Feature | Complexity | Est. Effort | Dependencies |
|---------|------------|-------------|--------------|
| Custom Cache | High | 3-4 days | None |
| Message Queue | High | 4-5 days | None |
| DB Migration | Medium | 2-3 days | Docker |
| Inventory Mgmt | High | 3-4 days | Message Queue |
| Order State Machine | High | 2-3 days | Message Queue |
| Payment Abstraction | Medium | 2 days | None |
| Search Engine | High | 4-5 days | None |
| Recommendations | High | 4-5 days | Search Engine |
| Rate Limiting | Medium | 1-2 days | Cache |
| Circuit Breaker | Medium | 1-2 days | None |
| Distributed Tracing | High | 3-4 days | None |
| Analytics Dashboard | Medium | 3-4 days | None |
| Auth Enhancements | High | 3-4 days | None |
| Audit Trail | Medium | 2 days | None |
| Wishlist | Low | 1-2 days | None |
| Reviews & Ratings | Medium | 2-3 days | None |
| Promotions Engine | High | 3-4 days | None |
| Notifications | Medium | 2-3 days | Message Queue |

**Total Estimated Effort**: 45-55 days

---

## RUNNING LOCALLY

```bash
# Start all services
./start-services.sh

# Services will be available at:
# - Frontend: http://localhost:5173
# - BFF/Gateway: http://localhost:8080
# - Identity: http://localhost:8084
# - Inventory: http://localhost:8081
# - Cart: http://localhost:8082
# - Order: http://localhost:8085

# Stop all services
./stop-services.sh
```

---

## KEY PRINCIPLES

1. **No External Dependencies** - Everything runs offline
2. **Build From Scratch** - Learn by building, not importing
3. **Production Patterns** - Follow Amazon/industry patterns
4. **Scalable Design** - Easy to migrate to distributed systems later
5. **Observable** - Every operation should be traceable
6. **Resilient** - Graceful degradation on failures
