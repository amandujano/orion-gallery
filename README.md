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

## Vercel Configuration

The application uses Vercel Blob for storage:
- **Images**: Stored at `photos/<uuid>.<ext>` with random UUIDs for unique URLs
- **Gallery metadata**: Stored at `gallery/photos.json` (overwritten on each save)

### Required Environment Variable

Set the `BLOB_READ_WRITE_TOKEN` environment variable with your Vercel Blob token:

```bash
export BLOB_READ_WRITE_TOKEN=vercel_blob_rw_...
```

To obtain the token:
1. Go to Vercel Dashboard → Your Project → Storage → Blob
2. Navigate to the `.env.local` tab
3. Copy the token (starts with `vercel_blob_rw_...`)

The token is configured in `src/main/resources/application.properties` as:
```
vercel.blob.token=${BLOB_READ_WRITE_TOKEN}
```

## Development

For frontend development, you can run the Angular dev server separately in the `frontend/` directory:

```bash
cd frontend
npm install
npm start
```



docker build -t orions-gallery-backend .
docker run -d -p 8080:8080 --name orion-gallery orions-gallery-backend:latest
