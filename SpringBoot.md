# Spring Boot Backend Architecture & Requirements

This document outlines the required components, APIs, and dependencies needed to build the Spring Boot backend for the Remote File Explorer project.

## 1. Project Dependencies (pom.xml / build.gradle)
To build this application, we will need the following core Spring Boot starters and external libraries:
*   **Spring Web:** For building the RESTful APIs.
*   **Spring Data MongoDB:** For integrating with the MongoDB database to store metadata.
*   **Spring Security:** To secure the APIs (e.g., using JWT or API Keys).
*   **Firebase Admin SDK:** To send FCM (Firebase Cloud Messaging) data payloads to the Android device.
*   **Lombok (Optional but recommended):** To reduce boilerplate code (Getters, Setters, Constructors).

## 2. Domain Models (MongoDB Collections)
These classes represent the documents that will be stored in MongoDB.

*   **`Device`**: 
    *   `deviceId` (String, Primary Key)
    *   `deviceName` (String)
    *   `fcmToken` (String)
    *   `lastSeen` (Timestamp)
    *   `status` (Enum: ONLINE, IDLE, OFFLINE)
*   **`FileMetadata`**:
    *   `id` (String)
    *   `deviceId` (String, Indexed)
    *   `path` (String)
    *   `name` (String)
    *   `size` (Long)
    *   `modifiedDate` (Long)
    *   `isDirectory` (Boolean)
*   **`Job`**:
    *   `jobId` (String, Primary Key)
    *   `deviceId` (String, Indexed)
    *   `type` (Enum: DOWNLOAD, UPLOAD, etc.)
    *   `status` (Enum: PENDING, IN_PROGRESS, COMPLETED, FAILED)
    *   `payload` (String/JSON - e.g., the file path `"/Documents/Resume.pdf"`)
    *   `createdAt` (Timestamp)

## 3. Core Services
The business logic of the application.

*   **`DeviceService`**: Handles registering new devices, updating FCM tokens, and updating the `lastSeen` heartbeat.
*   **`MetadataService`**: Processes incoming file metadata payloads from the phone and updates the `files` collection in MongoDB.
*   **`JobCoordinatorService`**: 
    1. Creates new jobs when the Web Client requests a file.
    2. Returns pending jobs when the Android Phone requests them.
    3. Updates job statuses.
*   **`FcmNotificationService`**: Wraps the Firebase Admin SDK. Responsible for sending the `{"type": "CHECK_JOBS"}` data message to a specific device's FCM token.
*   **`FileTransferService`**: Manages the temporary storage of files. When the phone uploads a requested file, this service saves it to a temporary directory (or cloud bucket) until the web client downloads it.

## 4. REST API Endpoints (Controllers)

### Device & Sync APIs (Called by Android Phone)
*   `POST /api/v1/devices/register`: Registers a device ID and FCM token.
*   `POST /api/v1/devices/heartbeat`: Updates the `lastSeen` timestamp.
*   `POST /api/v1/sync/metadata`: Accepts a bulk payload of file metadata from the phone to update MongoDB.

### Job & Transfer APIs (Called by Android Phone)
*   `GET /api/v1/jobs/pending`: Phone fetches its list of pending jobs.
*   `POST /api/v1/jobs/claim`: Phone claims the next pending job.
*   `POST /api/v1/jobs/{jobId}/complete`: Marks a job as done.
*   `POST /api/v1/jobs/upload/{jobId}`: Phone uploads the actual binary file to the server temporarily.

### Web Client APIs (Called by PC/Browser)
*   `GET /api/v1/web/devices`: List available devices and their status.
*   `GET /api/v1/web/files?deviceID=...&path=...`: Browse the file directory (reads from MongoDB).
*   `POST /api/v1/web/request/downloads`: Web client requests a file. (This creates a Job and triggers the FCM notification).
*   `POST /api/v1/web/download/{jobId}`: Web client downloads the temporarily stored file once it's ready.
*   `POST /api/v1/web/jobs`: Create generic jobs (e.g., upload, delete).

## 5. Security & Authentication
*   Every request from the phone must include a unique identifier/token to ensure it only accesses and updates its own metadata.
*   The web client needs a login mechanism (Username/Password or OAuth) to access the specific user's registered devices.

## 6. Temporary Storage Strategy
When the phone uploads a file for the PC to download, Spring Boot needs to store it briefly.
*   **Option A (Local Disk):** Store in `/tmp/remote-explorer-uploads/`. Requires a cleanup cron job to delete stale files after 1 hour.
*   **Option B (Cloud Storage - e.g., AWS S3):** Upload directly to a bucket with a lifecycle policy that auto-deletes files after 1 day.

## Next Steps for Development
1. Initialize a Spring Boot project via Spring Initializr.
2. Setup MongoDB connection properties in `application.yml`.
3. Create the Domain Models and Repositories.
4. Integrate Firebase Admin SDK with the private key JSON file.
5. Build out the Controller endpoints.
