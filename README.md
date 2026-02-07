# ShopEase - E-commerce Platform

A full-stack e-commerce platform built with microservices architecture, featuring a React frontend and Spring Boot backend services.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    CLIENT LAYER                                          │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│    ┌─────────────────────────────────────────────────────────────────────────────┐      │
│    │                         React Frontend (Port 5173)                           │      │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │      │
│    │  │   Pages     │  │ Components  │  │  Context    │  │   Services (Axios)  │ │      │
│    │  │ - Home      │  │ - Header    │  │ - AuthCtx   │  │   - api.js          │ │      │
│    │  │ - Product   │  │ - Footer    │  │ - CartCtx   │  │   (HTTP Client)     │ │      │
│    │  │ - Cart      │  │ - Cards     │  │             │  │                     │ │      │
│    │  │ - Orders    │  │ - Loading   │  │             │  │                     │ │      │
│    │  └─────────────┘  └─────────────┘  └─────────────┘  └──────────┬──────────┘ │      │
│    └────────────────────────────────────────────────────────────────┼────────────┘      │
│                                                                     │                    │
└─────────────────────────────────────────────────────────────────────┼────────────────────┘
                                                                      │ HTTP/REST
                                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                     API GATEWAY LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│    ┌─────────────────────────────────────────────────────────────────────────────┐      │
│    │                    BFF Service - Backend for Frontend (Port 8080)            │      │
│    │                                                                              │      │
│    │    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐     │      │
│    │    │  AuthController  │    │ ProductController│    │  CartController  │     │      │
│    │    │  /api/auth/*     │    │  /api/products/* │    │  /api/cart/*     │     │      │
│    │    └────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘     │      │
│    │             │                       │                       │               │      │
│    │    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐     │      │
│    │    │  OrderController │    │ AdminController  │    │  SecurityConfig  │     │      │
│    │    │  /api/orders/*   │    │  /api/admin/*    │    │  JWT Filter      │     │      │
│    │    └────────┬─────────┘    └────────┬─────────┘    └──────────────────┘     │      │
│    │             │                       │                                       │      │
│    │    ┌────────┴───────────────────────┴─────────────────────────────────┐     │      │
│    │    │                         Service Layer                             │     │      │
│    │    │  AuthService | ProductService | CartService | OrderService        │     │      │
│    │    └─────────────────────────────────┬────────────────────────────────┘     │      │
│    │                                      │                                       │      │
│    │    ┌─────────────────────────────────┴────────────────────────────────┐     │      │
│    │    │                    DATABASE: H2 (bffdb)                           │     │      │
│    │    │              Tables: USERS, ORDERS, ORDER_ITEMS                   │     │      │
│    │    └──────────────────────────────────────────────────────────────────┘     │      │
│    └─────────────────────────────────────────────────────────────────────────────┘      │
│                           │                               │                              │
│                           │ HTTP/REST                     │ HTTP/REST                    │
│                           ▼                               ▼                              │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                  MICROSERVICES LAYER                                     │
├──────────────────────────────────────────┬──────────────────────────────────────────────┤
│                                          │                                               │
│  ┌────────────────────────────────────┐  │  ┌────────────────────────────────────────┐  │
│  │  Inventory Service (Port 8081)     │  │  │     Cart Service (Port 8082)           │  │
│  │                                    │  │  │                                        │  │
│  │  ┌──────────────────────────────┐  │  │  │  ┌──────────────────────────────────┐  │  │
│  │  │      ProductController       │  │  │  │  │        CartController            │  │  │
│  │  │       /api/products/*        │  │  │  │  │         /api/cart/*              │  │  │
│  │  └──────────────┬───────────────┘  │  │  │  └──────────────┬───────────────────┘  │  │
│  │                 │                  │  │  │                 │                      │  │
│  │  ┌──────────────┴───────────────┐  │  │  │  ┌──────────────┴───────────────────┐  │  │
│  │  │       ProductService         │  │  │  │  │         CartService              │  │  │
│  │  │  - getAll(), getById()       │  │  │  │  │  - addItem(), updateQty()        │  │  │
│  │  │  - add(), update(), delete() │  │  │  │  │  - getByUser(), clearCart()      │  │  │
│  │  └──────────────┬───────────────┘  │  │  │  └──────────────┬───────────────────┘  │  │
│  │                 │                  │  │  │                 │                      │  │
│  │  ┌──────────────┴───────────────┐  │  │  │  ┌──────────────┴───────────────────┐  │  │
│  │  │     ProductRepository        │  │  │  │  │      CartItemRepository          │  │  │
│  │  │        (JPA)                 │  │  │  │  │           (JPA)                  │  │  │
│  │  └──────────────┬───────────────┘  │  │  │  └──────────────┬───────────────────┘  │  │
│  │                 │                  │  │  │                 │                      │  │
│  │  ┌──────────────┴───────────────┐  │  │  │  ┌──────────────┴───────────────────┐  │  │
│  │  │   DATABASE: H2 (inventorydb) │  │  │  │  │     DATABASE: H2 (cartdb)        │  │  │
│  │  │   Table: PRODUCTS            │  │  │  │  │     Table: CART_ITEMS            │  │  │
│  │  │   - id, name, description    │  │  │  │  │     - id, user_id, product_id    │  │  │
│  │  │   - price, stock_quantity    │  │  │  │  │     - quantity, product_name     │  │  │
│  │  │   - category, image_url      │  │  │  │  │     - price                      │  │  │
│  │  │   - rating, review_count     │  │  │  │  │                                  │  │  │
│  │  └──────────────────────────────┘  │  │  │  └──────────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │  └────────────────────────────────────────┘  │
│                                          │                                               │
└──────────────────────────────────────────┴───────────────────────────────────────────────┘
```

## Database Architecture

**Each microservice has its own independent H2 in-memory database:**

| Service | Port | Database | JDBC URL | Tables |
|---------|------|----------|----------|--------|
| BFF Service | 8080 | bffdb | `jdbc:h2:mem:bffdb` | USERS, ORDERS, ORDER_ITEMS |
| Inventory Service | 8081 | inventorydb | `jdbc:h2:mem:inventorydb` | PRODUCTS |
| Cart Service | 8082 | cartdb | `jdbc:h2:mem:cartdb` | CART_ITEMS |

### Database Schema

```sql
-- BFF Service (bffdb)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    address VARCHAR(255),
    phone VARCHAR(20)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    shipping_address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(255),
    quantity INT,
    price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Inventory Service (inventorydb)
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    category VARCHAR(100),
    image_url VARCHAR(500),
    rating DECIMAL(2,1) DEFAULT 0,
    review_count INT DEFAULT 0
);

-- Cart Service (cartdb)
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    price DECIMAL(10,2),
    quantity INT DEFAULT 1,
    UNIQUE(user_id, product_id)
);
```

## Service Communication Flow

### Flow 1: User Login
```
┌────────┐    ┌─────────────┐    ┌─────────────┐
│Frontend│───▶│BFF /api/auth│───▶│  H2 bffdb   │
│        │    │   /login    │    │   USERS     │
└────────┘    └─────────────┘    └─────────────┘
     ▲              │
     │              │ JWT Token
     └──────────────┘
```

### Flow 2: Browse Products
```
┌────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│Frontend│───▶│BFF /api/    │───▶│ Inventory   │───▶│H2 inventorydb│
│        │    │  products   │    │ :8081       │    │  PRODUCTS   │
└────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Flow 3: Add to Cart
```
┌────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│Frontend│───▶│BFF /api/cart│───▶│ Cart Service│───▶│ H2 cartdb   │
│ + JWT  │    │  (POST)     │    │ :8082       │    │ CART_ITEMS  │
└────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Flow 4: Checkout (Complex Multi-Service Flow)
```
┌────────┐                                    
│Frontend│─────┐                              
│ + JWT  │     │                              
└────────┘     │                              
               │                              
               ▼                              
┌──────────────────────────────────────────────────────────────────────────┐
│                    BFF Service - Checkout Flow                            │
│                                                                           │
│  Step 1: Validate JWT & Get User                                          │
│  ┌──────────────┐                                                         │
│  │ JWT Filter   │──▶ Extract userId from token                            │
│  └──────────────┘                                                         │
│         │                                                                 │
│         ▼                                                                 │
│  Step 2: Get Cart Items                                                   │
│  ┌──────────────┐    HTTP GET     ┌──────────────┐    ┌──────────────┐   │
│  │ CartService  │────────────────▶│ Cart Service │───▶│ H2 cartdb    │   │
│  │   (BFF)      │◀────────────────│   :8082      │◀───│ CART_ITEMS   │   │
│  └──────────────┘   Cart Items    └──────────────┘    └──────────────┘   │
│         │                                                                 │
│         ▼                                                                 │
│  Step 3: Validate Products & Stock                                        │
│  ┌──────────────┐    HTTP GET     ┌──────────────┐    ┌──────────────┐   │
│  │ProductService│────────────────▶│  Inventory   │───▶│H2 inventorydb│   │
│  │   (BFF)      │◀────────────────│   :8081      │◀───│  PRODUCTS    │   │
│  └──────────────┘  Product Details└──────────────┘    └──────────────┘   │
│         │                                                                 │
│         ▼                                                                 │
│  Step 4: Create Order in BFF Database                                     │
│  ┌──────────────┐                 ┌──────────────┐                        │
│  │ OrderService │────────────────▶│  H2 bffdb    │                        │
│  │   (BFF)      │                 │ ORDERS +     │                        │
│  └──────────────┘                 │ ORDER_ITEMS  │                        │
│         │                         └──────────────┘                        │
│         ▼                                                                 │
│  Step 5: Clear User's Cart                                                │
│  ┌──────────────┐   HTTP DELETE   ┌──────────────┐    ┌──────────────┐   │
│  │ CartService  │────────────────▶│ Cart Service │───▶│ H2 cartdb    │   │
│  │   (BFF)      │                 │   :8082      │    │ CART_ITEMS   │   │
│  └──────────────┘                 └──────────────┘    └──────────────┘   │
│         │                                                                 │
│         ▼                                                                 │
│  Step 6: Return Order Confirmation                                        │
│  ┌──────────────┐                                                         │
│  │ Response     │──▶ { orderId, status, total, items[] }                  │
│  └──────────────┘                                                         │
└──────────────────────────────────────────────────────────────────────────┘
```

## Accessing H2 Database Console

Each service exposes an H2 web console for database visualization:

| Service | Console URL | JDBC URL | Username | Password |
|---------|-------------|----------|----------|----------|
| BFF | http://localhost:8080/h2-console | `jdbc:h2:mem:bffdb` | sa | password |
| Inventory | http://localhost:8081/h2-console | `jdbc:h2:mem:inventorydb` | sa | password |
| Cart | http://localhost:8082/h2-console | `jdbc:h2:mem:cartdb` | sa | password |

### Steps to Access:
1. Open the console URL in browser
2. Set Driver Class: `org.h2.Driver`
3. Enter JDBC URL from table above
4. Username: `sa`
5. Password: `password`
6. Click Connect

## Features

### Customer Features
- Browse products by category
- Search products
- Add items to cart
- Checkout and place orders
- View order history
- User registration and login

### Admin Features
- Add/Edit/Delete products
- Manage inventory stock
- View all orders
- Update order status

## Tech Stack

### Frontend
- React 18 with Vite
- Tailwind CSS v4 for styling
- React Router for navigation
- Axios for API calls
- Context API for state management

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- H2 Database (in-memory, separate per service)
- Swagger/OpenAPI documentation

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+

### Quick Start

1. **Clone the repository**
```bash
cd backend_platform
```

2. **Start all services**
```bash
./start-services.sh
```

Or start services manually:

```bash
# Terminal 1: Inventory Service
cd inventory-service && mvn spring-boot:run

# Terminal 2: Cart Service
cd cart-service && mvn spring-boot:run

# Terminal 3: BFF Service
cd bff-service && mvn spring-boot:run

# Terminal 4: Frontend
cd frontend && npm install && npm run dev
```

3. **Access the application**
- Frontend: http://localhost:5173
- BFF Swagger: http://localhost:8080/swagger-ui.html
- Inventory Swagger: http://localhost:8081/swagger-ui.html
- Cart Swagger: http://localhost:8082/swagger-ui.html

### Demo Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| User  | user     | user123   |
| User  | jane     | jane123   |
| Admin | admin    | admin123  |

## API Endpoints

### BFF Service (Port 8080)

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

#### Products (Public)
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?q=query` - Search products
- `GET /api/products/category/{category}` - Get by category

#### Cart (Authenticated)
- `GET /api/cart` - Get cart items
- `POST /api/cart` - Add to cart
- `PUT /api/cart/{itemId}` - Update quantity
- `DELETE /api/cart/{itemId}` - Remove item
- `DELETE /api/cart` - Clear cart

#### Orders (Authenticated)
- `POST /api/orders/checkout` - Place order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details

#### Admin (Admin Role Required)
- `POST /api/admin/products` - Add product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product
- `GET /api/admin/orders` - Get all orders
- `PUT /api/admin/orders/{id}/status` - Update order status

### Inventory Service (Port 8081)
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Add product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Cart Service (Port 8082)
- `GET /api/cart` - Get all cart items
- `GET /api/cart/user/{userId}` - Get cart by user
- `POST /api/cart` - Add to cart
- `PUT /api/cart/{id}` - Update item
- `DELETE /api/cart/{id}` - Remove item
- `DELETE /api/cart/user/{userId}` - Clear user's cart

## Project Structure

```
backend_platform/
├── bff-service/                 # Backend for Frontend (API Gateway)
│   ├── src/main/java/com/example/bff/
│   │   ├── config/              # Security, JWT, CORS config
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── model/               # JPA entities (User, Order)
│   │   ├── repository/          # JPA repositories
│   │   └── service/             # Business logic + HTTP clients
│   └── pom.xml
│
├── inventory-service/           # Product & Stock Management
│   ├── src/main/java/com/example/inventoryservice/
│   │   ├── config/              # Data initializer (seeds products)
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # JPA entities (Product)
│   │   ├── repository/          # JPA repositories
│   │   └── service/             # Business logic
│   └── pom.xml
│
├── cart-service/                # Shopping Cart Service
│   ├── src/main/java/com/example/cartservice/
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # JPA entities (CartItem)
│   │   ├── repository/          # JPA repositories
│   │   └── service/             # Business logic
│   └── pom.xml
│
├── frontend/                    # React Application
│   ├── src/
│   │   ├── components/          # Header, Footer, ProductCard, Loading
│   │   ├── context/             # AuthContext, CartContext
│   │   ├── pages/               # Home, Login, Cart, Orders, Admin, etc.
│   │   ├── services/            # api.js (Axios instance)
│   │   ├── index.css            # Tailwind + custom styles
│   │   └── App.jsx              # Routes
│   ├── package.json
│   └── tailwind.config.js
│
├── start-services.sh            # Start all services script
├── stop-services.sh             # Stop all services script
├── test_all_services.sh         # Test script
└── README.md
```

## Sample Products

The inventory service automatically seeds 15 sample products across 5 categories:
- Electronics (iPhone, MacBook, Samsung, Sony headphones, iPad)
- Clothing (Nike shoes, Levi's jeans, North Face jacket)
- Home & Kitchen (Instant Pot, Dyson vacuum, KitchenAid mixer)
- Books (Atomic Habits, Psychology of Money)
- Sports & Outdoors (Yeti tumbler, Fitbit)

## Security

- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Password encryption with BCrypt
- CORS configuration for frontend

## Stopping Services

```bash
./stop-services.sh
```

## Troubleshooting

### Port already in use
```bash
# Check what's using a port
lsof -i :8080

# Kill process on port
lsof -ti:8080 | xargs kill -9
```

### Service not starting
Check the logs in the `logs/` directory:
```bash
cat bff-service/logs/bff.log
cat inventory-service/logs/inventory.log
cat cart-service/logs/cart.log
```

## Future Scope of Work

### Phase 1: Critical (Must Have)
| Feature | Description | Effort |
|---------|-------------|--------|
| **Database Migration** | Migrate from H2 to PostgreSQL/MySQL for production | 2-3 days |
| **Caching Layer** | Build custom in-memory cache with LRU eviction for products, sessions | 3-4 days |
| **Message Queue** | Async processing for orders, notifications using custom BlockingQueue | 4-5 days |
| **Rate Limiting** | Token bucket algorithm to prevent API abuse | 2 days |
| **Logging & Monitoring** | Structured JSON logging, correlation IDs, metrics | 2-3 days |

### Phase 2: Important (Should Have)
| Feature | Description | Effort |
|---------|-------------|--------|
| **Analytics Dashboard** | Sales reports, user behavior, product performance | 3-4 days |
| **Search Engine** | Full-text search with facets, autocomplete | 3-4 days |
| **Inventory Management** | Stock reservation, low stock alerts, batch updates | 3-4 days |
| **Notification Service** | Email templates, in-app notifications | 2-3 days |
| **Circuit Breaker** | Resilience pattern for service failures | 2-3 days |

### Phase 3: Nice to Have
| Feature | Description | Effort |
|---------|-------------|--------|
| **Recommendation Engine** | "Users who bought X also bought Y" | 4-5 days |
| **Reviews & Ratings** | Product reviews with moderation | 2-3 days |
| **Wishlist** | Save for later with price drop alerts | 1-2 days |
| **Promotions Engine** | Coupons, flash sales, bundle deals | 3-4 days |
| **Audit Trail** | Track all entity changes | 2 days |

### Additional Scope Items
| Feature | Description | Effort |
|---------|-------------|--------|
| **API Versioning** | URL/Header versioning for backward compatibility | 1-2 days |
| **Service Discovery** | Custom registry for microservice registration | 3-4 days |
| **Payment Abstraction** | Pluggable payment gateway interface | 2-3 days |
| **Multi-Warehouse** | Support for multiple inventory locations | 3-4 days |
| **Bulk Import/Export** | CSV/Excel import for products, orders | 2-3 days |
| **Scheduled Jobs** | Cart abandonment reminders, report generation | 2 days |
| **Feature Flags** | Enable/disable features without deployment | 1-2 days |
| **A/B Testing** | Test different UI/pricing strategies | 3-4 days |
| **Localization** | Multi-language support | 2-3 days |
| **PDF Generation** | Invoice, order confirmation PDFs | 1-2 days |

## Offline Support

All product images are stored locally in `/frontend/public/images/products/` as SVG files.
The application works completely offline without requiring internet for images.

## License

This project is for demonstration purposes.
