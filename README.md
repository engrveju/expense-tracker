# Expense Tracker 🏦

A comprehensive Spring Boot-based Expense Tracker application for personal finance management. This application provides robust features for tracking expenses, analyzing spending patterns, and managing budgets with a clean REST API architecture.

## 🚀 Features

### Core Functionality
- **User Management** - Complete user registration and profile management
- **Expense Tracking** - Full CRUD operations for financial transactions
- **Budget Management** - Create and track spending limits by category
- **Advanced Analytics** - Spending insights and category-wise analysis

### Advanced Capabilities
- **Category-wise Analysis** - Breakdown of expenses by category
- **Date Range Filtering** - Custom period expense analysis
- **Expense Summaries** - Total spending and top category identification
- **Pagination & Sorting** - Efficient data retrieval
- **Multiple Payment Methods** - Cash, Credit Card, Debit Card, Bank Transfer, Digital Wallet

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: PostgreeSQL
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Bean Validation API
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Utilities**: Lombok, MapStruct, Apache Commons Lang3
- **Build Tool**: Maven# expense-tracker

## 📁 Project Structure

`src/main/java/com/expensetracker/`

- `config/` \- Configuration classes (Spring beans, security, properties)
- `controller/` \- REST API endpoints (HTTP controllers)
- `dto/` \- Data Transfer Objects (request/response models)
- `entity/` \- JPA entities (database models)
- `exception/` \- Custom exception handling (controllers/handlers)
- `repository/` \- Data access layer (Spring Data JPA repositories)
- `service/` \- Business logic layer (service interfaces and implementations)
- `util/` \- Utilities and mappers (helpers, MapStruct mappers, converters)
