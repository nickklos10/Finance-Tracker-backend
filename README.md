# AI Finance Tracker

A modern finance tracking application with AI capabilities to help analyze spending patterns and provide insights.

## Project Structure

The project is organized into two main components:

### Backend (Spring Boot)

- Located in the `backend` directory
- RESTful API built with Spring Boot
- Uses H2 in-memory database (for development)
- Provides endpoints for managing transactions and categories

### Frontend (To be implemented)

- Will be located in the `frontend` directory
- Will be implemented with a modern frontend framework

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.8+

### Running the Backend

1. Navigate to the backend directory:

   ```
   cd backend
   ```

2. Build the project:

   ```
   mvn clean install
   ```

3. Run the application:

   ```
   mvn spring-boot:run
   ```

4. The API will be available at http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - API Base URL: http://localhost:8080/api

## API Endpoints

### Transactions

- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `POST /api/transactions` - Create a new transaction
- `PUT /api/transactions/{id}` - Update a transaction
- `DELETE /api/transactions/{id}` - Delete a transaction
- `GET /api/transactions/type/{type}` - Get transactions by type (INCOME, EXPENSE, TRANSFER)
- `GET /api/transactions/date-range?startDate=...&endDate=...` - Get transactions between dates
- `GET /api/transactions/category/{categoryId}` - Get transactions by category

### Categories

- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/name/{name}` - Get category by name
- `POST /api/categories` - Create a new category
- `PUT /api/categories/{id}` - Update a category
- `DELETE /api/categories/{id}` - Delete a category

## Future Enhancements

- User authentication and authorization
- Frontend implementation
- AI-powered spending analysis
- Budget planning and recommendations
- Data visualization
- Mobile application
