# GreenLoop - Eco-Friendly Order & Delivery Management System

GreenLoop is a Java-based desktop portal designed to streamline sustainable order tracking, inventory control, and delivery scheduling. The application leverages a FlatLaf-styled Swing UI and links directly to a MongoDB database.

---

## Prerequisites
Before running the application, make sure you have the following installed:
* **Java Development Kit (JDK 17)** or higher.
* **Apache Maven** (tested with 3.x).
* **MongoDB Instance**: Connected via the properties file.

---

## Configuration
Database connection configurations are pre-defined in:
* [`src/main/resources/application.properties`](file:///d:/GreenLoop/src/main/resources/application.properties)

Ensure that the connection string `mongodb.uri` points to a reachable MongoDB database.

---

## How to Build the Project
Open your terminal inside the project root directory and run the following command to compile and build the package:
```bash
mvn clean compile
```

---

## How to Run the Application

### 1. Launch the Swing GUI (Graphical User Interface)
To run the main Logistics Portal UI console, execute:
```bash
mvn exec:java -Dexec.mainClass="org.example.OrderManagementExample"
```
*Alternatively, you can run the `org.example.OrderManagementGUI` class from your preferred IDE (IntelliJ, Eclipse, VS Code).*

### 2. Run the Console CLI Example Flow
To run a headless demo simulation of placing orders, assigning agents, completing deliveries, and refunding cancelled orders in the CLI:
```bash
mvn exec:java -Dexec.mainClass="org.example.OrderManagementExample" -Dexec.args="cli"
```

---

## Running Tests
To execute all JUnit unit and integration tests checking database integrity and business logic:
```bash
mvn test
```
