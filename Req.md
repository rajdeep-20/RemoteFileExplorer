Remote File Explorer – Complete System Design
Goal

Extend your existing Android File Explorer into a personal remote file explorer.

The phone remains the source of truth. The server never permanently stores the user's files (except optionally during a requested transfer). MongoDB stores only metadata and job information.

Design goals:

Remote browsing
Remote file download
Phone may be offline
No permanent background service
No persistent notification
Battery friendly
Android-compliant
Overall Architecture
                     ┌─────────────────────────┐
                     │        Web Client       │
                     │   (PC / Browser)        │
                     └────────────┬────────────┘
                                  │
                           HTTPS REST API
                                  │
                     ┌────────────▼────────────┐
                     │      Spring Boot        │
                     │                         │
                     │ Authentication          │
                     │ Metadata API            │
                     │ Job Queue               │
                     │ Upload API              │
                     │ Download API            │
                     │ FCM Sender              │
                     └────────────┬────────────┘
                                  │
                     ┌────────────▼────────────┐
                     │        MongoDB          │
                     │                         │
                     │ devices                │
                     │ files                  │
                     │ recentFiles            │
                     │ jobs                   │
                     └────────────┬────────────┘
                                  │
                    Firebase Cloud Messaging
                                  │
                     ┌────────────▼────────────┐
                     │     Android Phone       │
                     │                         │
                     │ File Explorer           │
                     │ File Scanner            │
                     │ Sync Manager            │
                     │ Job Processor           │
                     │ WorkManager             │
                     │ FirebaseMessagingService│
                     └─────────────────────────┘
Android Application

Your existing application remains the core.

Current modules

File Explorer

↓

Files.walkFileTree()

↓

RecyclerView

↓

File Operations

New modules

SyncManager

JobProcessor

FirebaseMessagingService

WorkManager
First Launch

The first time the app starts

Android

↓

Generate Device ID

↓

Request Firebase Token

↓

POST /api/v1/devices/register

Spring Boot stores

deviceId

deviceName

firebaseToken

lastSeen

appVersion
Metadata Synchronization

The explorer scans

Documents

Pictures

Movies

Downloads

Produces

{
    "path":"/Documents/Resume.pdf",
    "size":25123,
    "modified":1751721200,
    "directory":false
}

Only metadata is uploaded.

No file contents.

MongoDB
devices
deviceId

firebaseToken

lastSeen

status
files
deviceId

path

name

size

modified

directory
recentFiles
deviceId

path

openedTime
jobs
jobId

deviceId

type

payload

status

createdTime

Example

DOWNLOAD

/Documents/Resume.pdf
Remote Browsing

User opens

https://yourserver.com

Server returns

Documents

Pictures

Downloads

Movies

Everything comes from MongoDB.

The phone can even be offline.

Download Flow

Suppose you click

Resume.pdf

Server

↓

Creates

DOWNLOAD JOB

MongoDB

status = PENDING

Immediately afterwards

Spring Boot sends

FCM DATA MESSAGE

{
    type:"CHECK_JOBS"
}
Phone receives FCM

Android starts

FirebaseMessagingService

No Activity opens.

No UI appears.

The app executes

GET /api/v1/jobs/pending

Server replies

[
    {
        "type":"DOWNLOAD",
        "path":"/Documents/Resume.pdf"
    }
]

Phone

↓

Reads file

↓

POST /api/v1/jobs/upload/{jobID}

↓

Server

↓

Stores temporarily

↓

Marks job completed

↓

Phone exits

PC

User refreshes

↓

Download Ready
Recent Files

Whenever your explorer opens

Resume.pdf
POST /recent

MongoDB keeps

Last 100 files
WorkManager

Acts as a safety net.

Every few hours

Wake

↓

Sync metadata

↓

Check pending jobs

↓

Process remaining jobs

↓

Sleep

Even if an FCM message was delayed, jobs are eventually completed.

Notifications
Data-only FCM
{
    data:{
        type:"CHECK_JOBS"
    }
}

Expected behavior:

No notification shown.
Android wakes your app briefly.
Work is performed.
Process exits.
Notification FCM
{
    notification:{
        title:"Hello"
    }
}

Android automatically displays a notification.

You will not use this type.

Device States

Instead of just Online/Offline

ONLINE

Seen recently.

IDLE

Hasn't synced in several hours.

OFFLINE

No contact for a long period.

The web UI always displays the device state.

Swipe Away

User

Recent Apps

↓

Swipe App Away

On most Android devices

FCM

↓

Android starts FirebaseMessagingService

↓

Jobs execute

↓

Process exits

No persistent notification required.

Force Stop
Settings

↓

Apps

↓

Force Stop

Everything stops.

No FCM.

No WorkManager.

No background execution.

User must manually open the app again.

Security

Every API request includes authentication (for example, a JWT).

The phone never trusts the FCM payload.

FCM only says

CHECK_JOBS

The actual work is fetched securely from your backend over HTTPS.

Advantages
Reuses your existing file explorer.
No continuous background process.
No permanent notification.
Battery efficient.
Works when the phone is offline.
Remote browsing remains available from cached metadata.
Remote downloads occur when the phone checks in or is awakened by FCM.
Easily extensible to multiple devices.
Future Enhancements

Once the core system is stable, you can add features without redesigning the architecture:

Thumbnail generation for images and videos.
Full-text search over indexed files.
Multi-device support under one account.
Folder sharing with expiring links.
Streaming media instead of downloading first.
Delta synchronization using FileObserver.
Optional end-to-end encryption for uploaded files.
Progressive uploads for very large files.
Desktop client built with Electron or JavaFX.
Final Summary

The project becomes a personal remote storage index rather than a cloud drive.

Android app: Owns and manages the actual files.
Spring Boot: Coordinates requests, authentication, and job scheduling.
MongoDB: Stores metadata, device information, recent files, and pending jobs.
FCM: Wakes the app for immediate work when possible.
WorkManager: Guarantees eventual synchronization if FCM is delayed or unavailable.

This architecture stays within Android's background execution model while providing a responsive and scalable remote file browsing experience.