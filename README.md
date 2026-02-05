# PLC Batching Recipe Management System

A Spring Boot application for managing PLC batch recipes with Ethernet/IP communication support in offline mode.

## Features

- **Complete Single Application**: Angular frontend integrated with Spring Boot backend
- **Recipe Management**: Create, read, update, and delete batch recipes
- **Ingredient Management**: Define ingredients and their quantities for each recipe
- **Batch Execution Tracking**: Monitor and track batch runs with status updates
- **Ethernet/IP Support**: Offline mode support for PLC communication via com.digitalpetri.enip library
- **H2 Database**: Embedded database for offline operation
- **Responsive UI**: Modern Angular dashboard and management interface
- **RESTful API**: Complete REST API for all operations
- **Input Validation**: Comprehensive validation for all inputs
- **Error Handling**: Global exception handling with detailed error responses

## Technology Stack

### Backend
- **Java**: JDK 17
- **Spring Boot**: 3.2.1
- **Database**: H2 (Embedded)
- **ORM**: JPA/Hibernate
- **PLC Communication**: com.digitalpetri.enip
- **Build**: Maven

### Frontend
- **Angular**: 17.x
- **TypeScript**: 5.2+
- **Node.js**: 18+
- **npm**: 9+
- **SCSS**: For styling
- **RxJS**: For reactive programming

## Project Structure

```
compactLogix/
├── src/main/java/com/plc/recipe/
│   ├── RecipeManagementApplication.java    # Main application entry point
│   ├── config/
│   │   └── WebConfig.java                  # Web configuration for serving Angular
│   ├── controller/
│   │   ├── RecipeController.java           # Recipe REST endpoints
│   │   ├── BatchRunController.java         # Batch execution endpoints
│   │   └── PLCController.java              # PLC status and mode endpoints
│   ├── service/
│   │   ├── RecipeService.java              # Recipe business logic
│   │   ├── BatchRunService.java            # Batch execution logic
│   │   └── EthernetIPService.java          # PLC communication
│   ├── entity/
│   │   ├── Recipe.java                     # Recipe entity
│   │   ├── Ingredient.java                 # Ingredient entity
│   │   └── BatchRun.java                   # Batch run tracking entity
│   ├── dto/
│   │   ├── RecipeDTO.java                  # Recipe data transfer object
│   │   ├── IngredientDTO.java              # Ingredient DTO
│   │   └── BatchRunDTO.java                # Batch run DTO
│   ├── repository/
│   │   ├── RecipeRepository.java           # Recipe data access
│   │   └── BatchRunRepository.java         # Batch run data access
│   └── exception/
│       ├── ErrorResponse.java              # Error response format
│       └── GlobalExceptionHandler.java     # Global exception handling
├── ui/                                     # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/                 # Angular components
│   │   │   ├── models/                     # TypeScript models
│   │   │   ├── services/                   # Angular services
│   │   │   └── app-routing.module.ts       # Routing configuration
│   │   ├── index.html                      # Angular app HTML
│   │   └── styles.scss                     # Global styles
│   ├── package.json                        # Frontend dependencies
│   ├── tsconfig.json                       # TypeScript configuration
│   └── angular.json                        # Angular CLI configuration
├── pom.xml                                 # Maven configuration with frontend build
└── README.md                               # This file
```

## API Endpoints

### Recipe Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/recipes` | Create a new recipe |
| GET | `/api/recipes` | Get all recipes |
| GET | `/api/recipes/active` | Get active recipes only |
| GET | `/api/recipes/{id}` | Get recipe by ID |
| PUT | `/api/recipes/{id}` | Update recipe |
| DELETE | `/api/recipes/{id}` | Delete recipe |
| PUT | `/api/recipes/{id}/activate` | Activate recipe |
| PUT | `/api/recipes/{id}/deactivate` | Deactivate recipe |

### Batch Execution

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/batch-runs` | Create batch run |
| GET | `/api/batch-runs` | Get all batch runs |
| GET | `/api/batch-runs/{id}` | Get batch run by ID |
| GET | `/api/batch-runs/recipe/{recipeId}` | Get batch runs for a recipe |
| PUT | `/api/batch-runs/{id}/status` | Update batch status |
| PUT | `/api/batch-runs/{id}/complete` | Complete batch with actual quantity |
| POST | `/api/batch-runs/{id}/start` | Start batch execution |
| POST | `/api/batch-runs/{id}/stop` | Stop batch execution |
| GET | `/api/batch-runs/{id}/progress` | Get current batch progress |
| DELETE | `/api/batch-runs/{id}` | Delete batch run |

### PLC Communication

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/plc/status` | Get PLC connection status |
| POST | `/api/plc/mode/offline` | Enable offline mode |
| POST | `/api/plc/mode/online` | Enable online mode (connect to PLC) |

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ (for frontend development, optional for production build)
- npm 9+ (for frontend development, optional for production build)

### Installation & Build

1. Clone the repository:
```bash
git clone <repository-url>
cd compactLogix
```

2. Build the project (includes Angular build):
```bash
mvn clean install
```

This command will:
- Download Node.js and npm if not available
- Install Angular dependencies
- Build the Angular frontend
- Copy built Angular files to `src/main/resources/static`
- Compile the Java backend
- Package everything into a single JAR

3. Run the application:
```bash
mvn spring-boot:run
```

Or run the generated JAR:
```bash
java -jar target/recipe-management-1.0.0.jar
```

The application will be available at: **http://localhost:8080**

### Development Mode

#### Frontend Development Only

If you want to run just the Angular frontend with live reload:
```bash
cd ui
npm install
npm start
```

Configure the Angular app to proxy API calls by creating `ui/src/proxy.conf.json`:
```json
{
  "/api/*": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

Then run with:
```bash
ng serve --proxy-config proxy.conf.json
```

#### Backend Development Only

```bash
mvn spring-boot:run
```

## Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# Database configuration
spring.datasource.url=jdbc:h2:mem:recipedb

# Logging level
logging.level.com.plc.recipe=DEBUG

# H2 Console (for debugging)
spring.h2.console.enabled=true
```

### Frontend Configuration

The frontend uses the same base URL as the backend for API calls. The API base is set to `/api` in `ui/src/app/services/recipe.service.ts`.

## API Examples

### Create a Recipe

```bash
curl -X POST http://localhost:8080/api/recipes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Batch A",
    "description": "First batch recipe",
    "batchSize": 100.0,
    "unit": "kg",
    "isActive": true,
    "ingredients": [
      {
        "name": "Ingredient 1",
        "quantity": 50.0,
        "unit": "kg",
        "sequenceOrder": 1
      }
    ]
  }'
```

### Start a Batch Run

```bash
curl -X POST http://localhost:8080/api/batch-runs \
  -H "Content-Type: application/json" \
  -d '{
    "recipeId": 1,
    "batchNumber": "BATCH-001",
    "targetQuantity": 100.0,
    "operatorName": "John Doe"
  }'
```

### Get PLC Status

```bash
curl http://localhost:8080/api/plc/status
```

## Offline Mode

The system runs in **offline mode** by default, allowing you to develop and test without a physical PLC connection. 

To toggle offline/online mode from the UI:
1. Navigate to the Dashboard
2. Look at the "PLC Connection Status" card
3. Click "Go Online" or "Go Offline" button

Or via API:

```bash
# Enable offline mode
curl -X POST http://localhost:8080/api/plc/mode/offline

# Enable online mode (attempt PLC connection)
curl -X POST http://localhost:8080/api/plc/mode/online
```

## User Interface

The Angular application provides:

- **Dashboard**: Overview of recipes, running batches, and PLC status
- **Recipes**: List, create, edit, and manage recipes with ingredients
- **Batch Runs**: Track batch execution with start, stop, and status controls
- **PLC Status**: Monitor PLC connection and toggle offline/online mode
- **Responsive Design**: Works on desktop and tablet devices

## Ethernet/IP Integration

The `EthernetIPService` provides a foundation for Ethernet/IP communication. To implement actual PLC communication:

1. Update the `EthernetIPService` class in `src/main/java/com/plc/recipe/service/EthernetIPService.java`
2. Use `com.digitalpetri.enip` library methods to establish connections
3. Implement message marshaling/unmarshaling
4. Handle session management

Example TODO in service:
```java
// TODO: Implement actual Ethernet/IP communication using com.digitalpetri.enip
// Steps:
// 1. Connect to PLC
// 2. Create EtherNet/IP session
// 3. Send batch start message
// 4. Read response
```

## Database Access

Access H2 Console at: `http://localhost:8080/h2-console`

Default credentials:
- URL: `jdbc:h2:mem:recipedb`
- User: `sa`
- Password: (empty)

## Monitoring & Actuator

Health check: `http://localhost:8080/actuator/health`
Metrics: `http://localhost:8080/actuator/metrics`

## Development Notes

- All entities use optimistic locking (`@Version`) for concurrent access
- Timestamps are automatically managed (createdAt, updatedAt)
- Service layer handles all business logic and validation
- Controllers are thin and delegate to services
- Global exception handler provides consistent error responses

## Future Enhancements

- [ ] Implement actual Ethernet/IP communication
- [ ] Add authentication and authorization (JWT)
- [ ] Implement batch history and analytics charts
- [ ] Add WebSocket support for real-time batch updates
- [ ] Implement audit logging
- [ ] Add export functionality (CSV, PDF)
- [ ] Mobile-optimized layout
- [ ] Batch reordering and drag-drop UI
- [ ] Advanced search and filtering
- [ ] Multi-language support

## License

Proprietary - Internal Use Only

## Support

For issues or questions, contact the development team.

