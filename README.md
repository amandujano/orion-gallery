# Orion's Gallery

A cat picture gallery built as a Spring Boot + Angular monolith.

## Tech Stack

- **Backend**: Spring Boot 3.4.1
- **Frontend**: Angular
- **Java**: 21
- **Node**: v22.13.1
- **npm**: 10.9.2

## Building the Project

The project uses Maven to manage both the Spring Boot backend and Angular frontend. The build process automatically:

1. Installs Node.js and npm
2. Installs frontend dependencies
3. Builds the Angular frontend
4. Copies the frontend build output to Spring Boot's static resources

To build the entire project:

```bash
mvn clean install
```

## Running the Application

After building, run the Spring Boot application:

```bash
mvn spring-boot:run
```

The application will start and serve both the backend API and the frontend Angular application.

## Project Structure

- `src/` - Spring Boot backend source code
- `frontend/` - Angular frontend application
- `pom.xml` - Maven build configuration

## Development

For frontend development, you can run the Angular dev server separately in the `frontend/` directory:

```bash
cd frontend
npm install
npm start
```
