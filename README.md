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
│    │                    BFF Service - API Gateway (Port 8080)                     │      │
│    │                           *** STATELESS - NO DB ***                          │      │
│    │                                                                              │      │
│    │    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐     │      │
│    │    │  AuthController  │    │ ProductController│    │  CartController  │     │      │
│    │    │  /api/auth/*     │    │  /api/products/* │    │  /api/cart/*     │     │      │
│    │    └────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘     │      │
│    │             │                       │                       │               │      │
│    │    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐     │      │
│    │    │  OrderController │    │ AdminController  │    │ JwtAuthFilter    │     │      │
│    │    │  /api/orders/*   │    │  /api/admin/*    │    │ (Token Validate) │     │      │
│    │    └────────┬─────────┘    └────────┬─────────┘    └──────────────────┘     │      │
│    │             │                       │                                       │      │
│    │    ┌────────┴───────────────────────┴─────────────────────────────────┐     │      │
│    │    │              Service Layer (HTTP Clients to Microservices)        │     │      │
│    │    │  AuthService | ProductService | CartService | OrderService        │     │      │
│    │    └───────────────────────────────────────────────────────────────────┘     │      │
│    └─────────────────────────────────────────────────────────────────────────────┘      │
│                           │           │           │           │                         │
│                           ▼           ▼           ▼           ▼                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                  MICROSERVICES LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐           │
│  │  Identity Service    │  │  Inventory Service   │  │    Cart Service      │           │
│  │     (Port 8084)      │  │     (Port 8081)      │  │    (Port 8082)       │           │
│  │                      │  │                      │  │                      │           │
│  │  /api/auth/login     │  │  /api/products/*     │  │  /api/cart/*         │           │
│  │  /api/auth/register  │  │                      │  │                      │           │
│  │  /api/auth/validate  │  │                      │  │                      │           │
│  │  /api/users/*        │  │                      │  │                      │           │
│  │                      │  │                      │  │                      │           │
│  │  ┌────────────────┐  │  │  ┌────────────────┐  │  │  ┌────────────────┐  │           │
│  │  │ H2: identitydb │  │  │  │ H2: inventorydb│  │  │  │  H2: cartdb    │  │           │
│  │  │ - USERS        │  │  │  │ - PRODUCTS     │  │  │  │ - CART_ITEMS   │  │           │
│  │  └────────────────┘  │  │  └────────────────┘  │  │  └────────────────┘  │           │
│  └──────────────────────┘  └──────────────────────┘  └──────────────────────┘           │
│                                                                                          │
│  ┌──────────────────────┐                                                               │
│  │    Order Service     │                                                               │
│  │     (Port 8085)      │                                                               │
│  │                      │                                                               │
│  │  /api/orders/*       │                                                               │
│  │  /api/orders/checkout│                                                               │
│  │                      │                                                               │
│  │  ┌────────────────┐  │                                                               │
│  │  │  H2: orderdb   │  │                                                               │
│  │  │ - ORDERS       │  │                                                               │
│  │  │ - ORDER_ITEMS  │  │                                                               │
│  │  └────────────────┘  │                                                               │
│  └──────────────────────┘                                                               │
│                                                                                          │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## Service Overview

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **BFF Service** | 8080 | API Gateway - Routes & orchestrates requests | None (Stateless) |
| **Identity Service** | 8084 | Authentication, Users, JWT | H2: identitydb |
| **Inventory Service** | 8081 | Products, Stock Management | H2: inventorydb |
| **Cart Service** | 8082 | Shopping Cart Operations | H2: cartdb |
| **Order Service** | 8085 | Order Processing, Checkout | H2: orderdb |
| **Frontend** | 5173 | React SPA | N/A |

## Service Communication Flow

### Login Flow
```
Frontend → BFF (8080) → Identity Service (8084)
                              │
                              ▼
                        Generate JWT
                              │
                              ▼
                        Return Token
```

### Add to Cart Flow
```
Frontend → BFF (8080) → Cart Service (8082)
               │              │
               │              ▼
               │        Add Cart Item
               │              │
               ▼              ▼
         Inventory (8081)  Return Cart
         (Get Product Info)
```

### Checkout Flow
```
Frontend → BFF (8080) ─┬─→ Cart Service (8082) [Get Cart Items]
                       │
                       └─→ Order Service (8085) [Create Order]
                                   │
                                   ▼
                             Cart Service (8082) [Clear Cart]
                                   │
                                   ▼
                             Return Order
```

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
# Terminal 1: Identity Service
cd identity-service && mvn spring-boot:run

# Terminal 2: Inventory Service
cd inventory-service && mvn spring-boot:run

# Terminal 3: Cart Service
cd cart-service && mvn spring-boot:run

# Terminal 4: Order Service
cd order-service && mvn spring-boot:run

# Terminal 5: BFF Service (Start last)
cd bff-service && mvn spring-boot:run

# Terminal 6: Frontend
cd frontend && npm install && npm run dev
```

3. **Access the application**
- Frontend: http://localhost:5173
- BFF Swagger: http://localhost:8080/swagger-ui.html
- Identity Swagger: http://localhost:8084/swagger-ui.html
- Inventory Swagger: http://localhost:8081/swagger-ui.html
- Cart Swagger: http://localhost:8082/swagger-ui.html
- Order Swagger: http://localhost:8085/swagger-ui.html

### Demo Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| User  | user     | user123   |
| User  | jane     | jane123   |
| Admin | admin    | admin123  |

## API Endpoints

### BFF Service (Port 8080) - API Gateway

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

#### Products (Public)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `GET /api/products/category/{category}` - Products by category

#### Cart (Authenticated)
- `GET /api/cart` - Get user's cart
- `POST /api/cart` - Add to cart
- `PUT /api/cart/{id}` - Update quantity
- `DELETE /api/cart/{id}` - Remove item

#### Orders (Authenticated)
- `POST /api/orders/checkout` - Place order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details

#### Admin (ADMIN role only)
- `POST /api/admin/products` - Add product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product
- `GET /api/admin/orders` - All orders
- `PUT /api/admin/orders/{id}/status` - Update order status

## Project Structure

```
backend_platform/
├── bff-service/                 # API Gateway (Stateless)
│   ├── src/main/java/com/example/bff/
│   │   ├── config/              # Security, JWT, CORS config
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   └── service/             # HTTP clients to microservices
│   └── pom.xml
│
├── identity-service/            # Authentication & User Management
│   ├── src/main/java/com/example/identity/
│   │   ├── config/              # Security, JWT util
│   │   ├── controller/          # Auth & User controllers
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # User entity
│   │   ├── repository/          # User repository
│   │   └── service/             # Auth service
│   └── pom.xml
│
├── inventory-service/           # Product & Stock Management
│   ├── src/main/java/com/example/inventoryservice/
│   │   ├── config/              # Data initializer
│   │   ├── controller/          # Product controller
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # Product entity
│   │   ├── repository/          # Product repository
│   │   └── service/             # Product service
│   └── pom.xml
│
├── cart-service/                # Shopping Cart Service
│   ├── src/main/java/com/example/cartservice/
│   │   ├── controller/          # Cart controller
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # CartItem entity
│   │   ├── repository/          # Cart repository
│   │   └── service/             # Cart service
│   └── pom.xml
│
├── order-service/               # Order Processing Service
│   ├── src/main/java/com/example/order/
│   │   ├── config/              # CORS config
│   │   ├── controller/          # Order controller
│   │   ├── dto/                 # DTOs
│   │   ├── model/               # Order, OrderItem entities
│   │   ├── repository/          # Order repository
│   │   └── service/             # Order service
│   └── pom.xml
│
├── frontend/                    # React Application
│   ├── src/
│   │   ├── components/          # Header, Footer, ProductCard
│   │   ├── context/             # AuthContext, CartContext
│   │   ├── pages/               # Home, Login, Cart, Orders
│   │   ├── services/            # api.js (Axios instance)
│   │   └── index.css            # Tailwind + custom styles
│   ├── public/images/products/  # Local SVG product images
│   └── package.json
│
├── start-services.sh            # Start all services script
├── stop-services.sh             # Stop all services script
└── README.md
```

## Offline Support

All product images are stored locally in `/frontend/public/images/products/` as SVG files.
The application works completely offline without requiring internet for images.

## Security

- JWT-based authentication (issued by Identity Service)
- Role-based access control (USER, ADMIN)
- Password encryption with BCrypt
- Stateless API Gateway (validates JWT only)
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
Check the logs:
```bash
cat logs/bff.log
cat logs/identity.log
cat logs/inventory.log
cat logs/cart.log
cat logs/order.log
```

## License

This project is for demonstration purposes.
