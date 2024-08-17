# Java Spring Spotify-Like Music Service API

## Overview

This project offers a REST API which was inspired by Spotify, allowing you to manage and stream music. It features robust search capabilities and fast performance, enhanced by Redis caching.

JDBC is used for database operations due to its straightforwardness and effectiveness in managing native SQL queries, especially when complex joins make JPA a bit frustrating (personal opinion).

## Technologies Used

1. **Java & Spring Boot:** Frameworks for building and managing the RESTful API.
2. **Redis:** Enhances performance with caching and supports distributed locking.
3. **MySQL:** Relational database for storing user data, track info, and playlists.
4. **Flyway:** Handles database schema migrations and version control.
5. **Docker:** Containerizing the application.
6. **Amazon S3:** Scalable storage for audio files.
7. **Amazon CloudFront:** Accelerates music delivery with low-latency CDN.
8. **VNPay Sandbox:** Manages and tests payment processing and subscriptions.
9. **FFmpeg:** Provides audio processing capabilities, including track duration extraction and quality reduction.


## Database Migration

Database schema changes are managed using Flyway for consistency and version control. For detailed information on the database schema, migrations, and version history, please refer to the [`db.migration`](src/main/resources/db/migration)directory.

## API Documentation

- For detailed API endpoint reference, view this project's complete [API documentation](https://spotify-clone-api-docs.vercel.app/docs/#)  
- The design of this project's endpoints is heavily inspired by Spotify’s API. Explore Spotify’s official API documentation at [Spotify for Developers](https://developer.spotify.com/documentation/web-api/).

## Features

### 1. Playback Controller

The `PlayerService` class handles streaming sessions for users with the following features:

- **Play Track:** 
  - Adds a specific track to the queue.
  - Processes advertisements based on user status and playback history. (non-premium user)

- **Next/Previous Track:** 
  - Skips to the next or previous track in the queue.

- **Pause/Resume Session:** 
  - Allows pausing and resuming the current streaming session.

- **Change Playback Mode:** 
  - Supports different playback modes, including:
    - Shuffle
    - Sequential (premium only)
    - Repeat (premium only)
  - Regenerates the queue according to the selected mode.

- **Add to Queue:** 
  - Adds new tracks to the current session’s queue.

- **Session History:** 
  - Retrieves the user’s streaming history.
  - Utilizes Redis for quick access and efficient retrieval.

- **Streaming Event:** 
  - Produces an event when a track is streamed for at least 30 seconds.

---

### 2. Audio Preprocessing

Audio preprocessing tasks are handled using FFmpeg and include:

- **Track Duration Extraction:** 
  - FFmpeg is used to determine the length of audio tracks, ensuring accurate playback information.

- **Audio Quality Reduction:** 
  - FFmpeg processes audio files to produce lower-quality versions for non-premium users, while premium users receive high-quality audio. This approach balances performance and quality for different user tiers.

- **Future Plans with AWS Lambda:** 
  - **Cloud Function:**: Looking to move audio processing tasks to AWS Lambda for better scalability and performance. This would allow processing to happen in the cloud, handling large volumes of audio files more efficiently. Currently, audio processing is handled within `FileUtil` using Java's `Process` class to execute FFmpeg commands.

---

### 3. **Premium Subscriptions and Payment Processing**

The system supports both premium and non-premium user experiences, including managing subscriptions and handling payments:

- **Premium Users:**
    - **Playback Modes:** Have access to all playback modes (shuffle, repeat, sequential).
    - **Advertisements:** Generally do not experience ads during playback.
    - **Playback Experience:** Enjoy an uninterrupted streaming experience with additional features.
    - **Audio Quality:** Can access and stream high-quality audio tracks.

- **Non-Premium Users:**
    - **Playback Mode Restriction:** Restricted to shuffle mode only.
    - **Advertisements:** Experience ads inserted into their playback queue at regular intervals (e.g., every 10 tracks).
    - **Playback Experience:** May have advertisements inserted to support the free-tier service.
    - **Audio Quality:** Streams lower-quality audio tracks compared to premium users.

- **Subscription Management:** 
    - Handles everything related to user subscriptions, like setting them up, renewing them, and changing plans. This makes sure users get the features they’re supposed to based on their subscription.

- **VNPay Integration:**
    - VNPay handles payment transactions, making sure that payments are processed securely and smoothly between users and the app. 

This is just a very basic implementation though as it is not this project main focus

---

### 4. **Redis Caching and Locking**

The `RedisService` class implements the `CacheService` interface:

- **Caching:** 
    - Stores session data, track details, and other frequently accessed information in Redis with configurable expiration times to enhance performance (TLS).

- **Distributed Locking:** 
    - Manages thread-safe operations when working with `StreamingSession` using Redisson’s locking mechanism.

- **Cache Management:** 
    - Provides methods to evict specific or all caches, ensuring efficient removal of stale data.

---

### 5. **Basic CRUD Operations**

The API offers full CRUD (Create, Read, Update, Delete) operations for:

- **User Management:** 
    - Creating, updating, retrieving, and deleting user profiles.

- **Track, Album, Artist Profile Management:** 
    - Managing music tracks, including metadata updates and retrieval.

- **Playlist Management:** 
    - Creating and managing user playlists to organize favorite tracks.

---

## Future Plans

With the successful stream event already being produced in `PlayerService`, future improvements may include:

- **Trending Analysis:** Implement features to identify and display trending music based on current popularity.
- **Basic Recommendations:** Develop a collaborative filtering system to suggest tracks or playlists based on user listening habits.
