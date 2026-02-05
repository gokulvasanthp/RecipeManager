# Quick Start Guide

## What You Have

You now have a complete integrated application:
- **Spring Boot Backend** (Java 17, JPA, REST APIs)
- **Angular Frontend** (TypeScript, Responsive UI)
- **H2 Database** (Embedded, offline-ready)
- **Ethernet/IP Support** (Offline mode enabled by default)

## Fastest Path to Running the App

### Option 1: Single Command Build & Run (Recommended)

```bash
# Navigate to project root
cd c:\code\compactLogix

# Build everything (frontend + backend in one command)
mvn clean install

# Run the application
mvn spring-boot:run
```

Then open your browser to: **http://localhost:8080**

### Option 2: Build JAR and Run

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/recipe-management-1.0.0.jar
```

Then open: **http://localhost:8080**

## What to Expect When You First Run

1. **Dashboard Page**: Shows overview with PLC status and batch statistics
2. **PLC Status Card**: Shows "Offline Mode" enabled
3. **Navigation Menu**: Dashboard, Recipes, Batch Runs
4. **H2 Database**: Empty but ready to receive data

## First Steps in the App

1. **Create a Recipe**:
   - Click "Recipes" in menu
   - Click "+ New Recipe"
   - Fill in name (e.g., "Batch A")
   - Set batch size (e.g., 100)
   - Set unit (e.g., "kg")
   - Add ingredients by clicking "+ Add Ingredient"
   - Click "Save Recipe"

2. **Create a Batch Run**:
   - Click "Batch Runs" in menu
   - Click "+ New Batch Run" (if available)
   - Or go back to Batch Runs and create one
   - Set batch number, target quantity, operator name
   - Click "Save"

3. **Start a Batch** (Offline Mode):
   - Go to Batch Runs
   - Click "Start" on a pending batch
   - Watch status change to RUNNING
   - Observe the Dashboard updates

4. **Toggle PLC Mode**:
   - Go to Dashboard
   - See the "PLC Connection Status" card
   - Click "Go Online" to switch modes (stays in offline simulation)

## Key Files

- **Frontend**: `ui/src/app/` - Angular components, services, models
- **Backend**: `src/main/java/com/plc/recipe/` - Java services, controllers, entities
- **Database Config**: `src/main/resources/application.properties`
- **API Endpoints**: All routes start with `/api/`

## Project Structure

```
Backend serves at: http://localhost:8080
├── Angular UI at: /
├── REST APIs at: /api/recipes, /api/batch-runs, /api/plc
├── H2 Console at: /h2-console (for debugging)
└── Health Check at: /actuator/health

All served from single JAR file
```

## Stopping the Application

- Press `Ctrl+C` in the terminal running the app
- Or kill the Java process

## Troubleshooting

### Port 8080 is already in use
```bash
# Change port in application.properties
# Or kill the process using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Mac/Linux
```

### Build fails with "Node not found"
- The Maven plugin will download Node.js automatically on first build
- Ensure you have internet connectivity
- This only happens on the first build, subsequent builds are faster

### Can't access the app
- Verify the server started: check console output for "Started RecipeManagementApplication"
- Check firewall settings
- Ensure port 8080 is not blocked

## Important Notes

- **Offline Mode**: Enabled by default - you can use the app without a real PLC
- **Database**: In-memory H2 - data is lost when app stops (great for dev/testing)
- **Single Application**: Both frontend and backend are served from one JAR
- **Responsive Design**: Works on desktop, tablet, and mobile browsers

## Next Steps

1. Read [README.md](README.md) for detailed documentation
2. Check [BUILD.md](BUILD.md) for advanced build options
3. Explore the REST API using Postman or curl
4. Review the code and customize as needed

## API Examples

### Create a Recipe
```bash
curl -X POST http://localhost:8080/api/recipes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Batch",
    "description": "Test recipe",
    "batchSize": 100,
    "unit": "kg",
    "isActive": true
  }'
```

### Get All Recipes
```bash
curl http://localhost:8080/api/recipes
```

### Get PLC Status
```bash
curl http://localhost:8080/api/plc/status
```

## Need Help?

- Check the [README.md](README.md) for comprehensive documentation
- Review [BUILD.md](BUILD.md) for build troubleshooting
- Examine the code comments in the source files
- Check browser console (F12) for frontend errors
- Check console output for backend errors

Enjoy building your PLC Recipe Management System! 🚀
