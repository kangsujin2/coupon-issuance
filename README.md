# First-Come, First-Served Coupon Issuance System

A Spring Boot service for issuing coupons on a first-come, first-served basis. This system uses MariaDB for persistence, Redis (with Redisson) for distributed locks, and Caffeine for in-memory caching.

## Features

- **FCFS Issuance Strategies**: Two implementations for coupon issuance:  
  - Lock-based (`FCFSCouponIssueServiceV1`)  
  - Optimistic version-based (`FCFSCouponIssueServiceV2`)  
- **Distributed Locking**: Ensures single issuance per coupon using Redis locks  
- **Caching**: Uses Caffeine for local caching and Spring Data Redis for shared cache  
- **Event-Driven**: Publishes issuance completion events to decouple components  
- **JPA Persistence**: Stores coupon and issuance data in MariaDB  
- **REST API**: Simple HTTP endpoints for issuing coupons and viewing statuses  

## Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3.3.0  
- **Database**: MariaDB (JDBC driver)  
- **Caching & Locking**: Redis (Redisson), Caffeine  
- **Data Access**: Spring Data JPA, Spring Data Redis  
- **Build & Dependency**: Gradle  
- **Testing**: JUnit 5  
- **Serialization**: Jackson (JSR-310 support)  

## Prerequisites

- Java 17 SDK  
- Docker (for Redis)  
- MariaDB instance  

## Installation & Setup

1. **Clone the repository**  
   ```bash
   git clone https://github.com/your-user/your-repo.git
   cd your-repo
2. **Configure environment variables**<br>
Create application.properties from application.properties.template and set:
   ```bash
    spring.datasource.url=jdbc:mariadb://<HOST>:<PORT>/<DB_NAME>
    spring.datasource.username=<DB_USER>
    spring.datasource.password=<DB_PASS>
    spring.redis.host=<REDIS_HOST>
    spring.redis.port=<REDIS_PORT>
3. **Start Redis**
   ```bash
    docker-compose up -d
4. **Run the app**
   ```bash
   ./gradlew bootRun

   
## Usage
- Issue a coupon
   ```bash
    curl -X POST "http://localhost:8080/api/coupons/{couponId}/issues?userId={userId}"
- Get issuance details
   ```bash
    curl "http://localhost:8080/api/coupons/{couponId}/issues/{issueId}"

## Project Structure

      build.gradle            # Gradle build config
      docker-compose.yml      # Redis service for local dev
      src/
      ├─ main/
      │  ├─ java/com/my/tbd/   # application source code
      │  └─ resources/         # config files
      └─ test/java/            # unit tests
      README.md                # this file
   

## Testing
     ./gradlew test
