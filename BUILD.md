# Build Guide

## Complete Build (Backend + Frontend)

The easiest way to build the entire application:

```bash
mvn clean install
```

This single command will:
1. Download Node.js and npm (if using frontend-maven-plugin)
2. Install npm dependencies for the Angular app
3. Build the Angular application
4. Copy Angular dist files to Spring Boot's static resources
5. Compile the Java backend
6. Package everything into a single JAR file

## Run the Application

### Using Maven
```bash
mvn spring-boot:run
```

### Using JAR
```bash
java -jar target/recipe-management-1.0.0.jar
```

### Access the Application
Open your browser and navigate to: http://localhost:8080

## Frontend-Only Development

If you want to develop the Angular frontend with live reload:

```bash
cd ui
npm install
npm start
```

This starts the Angular development server on `http://localhost:4200` with hot module reloading.

## Backend-Only Development

To run just the Spring Boot backend:

```bash
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8080/api/*`

## Production Build

For production deployment:

```bash
# Build with production optimizations
mvn clean package -DskipTests
```

This creates an optimized JAR in `target/recipe-management-1.0.0.jar` that:
- Includes minified Angular assets
- Optimizes Java compilation
- Strips unnecessary files

## Building with Docker (Optional)

Create a `Dockerfile` in the project root:

```dockerfile
FROM openjdk:17-slim
COPY target/recipe-management-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
mvn clean package -DskipTests
docker build -t recipe-manager:1.0.0 .
docker run -p 8080:8080 recipe-manager:1.0.0
```

## Troubleshooting Build Issues

### Node/npm not found
- The `frontend-maven-plugin` will automatically download and use Node.js
- Ensure you have internet connectivity for the first build

### Angular build fails
- Check that `ui/package.json` exists
- Verify Node.js compatibility (v18+)
- Clear npm cache: `rm -rf ui/node_modules && npm install`

### Spring Boot build fails
- Ensure JDK 17 is installed
- Check Java home: `echo $JAVA_HOME`
- Clear Maven cache: `rm -rf ~/.m2/repository`

### Port 8080 already in use
- Change the port in `application.properties`:
  ```properties
  server.port=8081
  ```

## Development Workflow

### Quick Start
```bash
# Initial setup
mvn clean install

# Regular development
mvn spring-boot:run
```

### Frontend Changes
If you modify Angular files:
```bash
cd ui
npm run build:prod
cd ..
mvn clean install
```

### Backend Changes
Simply rebuild and restart:
```bash
mvn clean package -DskipTests
java -jar target/recipe-management-1.0.0.jar
```

## CI/CD Integration

The project is ready for CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Build with Maven
  run: mvn clean install -DskipTests

- name: Run Tests
  run: mvn test

- name: Package Application
  run: mvn package -DskipTests
```

## Useful Commands

```bash
# Clean everything
mvn clean

# Compile only
mvn compile

# Run tests
mvn test

# Skip tests during build
mvn install -DskipTests

# Check for dependency updates
mvn versions:display-dependency-updates

# Build Angular in development mode
cd ui && npm run watch

# Build Angular in production mode
cd ui && npm run build:prod
```
