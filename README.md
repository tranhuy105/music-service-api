# Java Spring Spotify-Like Music Service API

## Overview

This project provides a comprehensive music streaming service API with robust functionalities, including CRUD operations, advanced query options, and Redis caching for performance optimization. The core focuses are user management, track playback, playlist creation, payment processing, and session management. Special attention is given to implementing a caching layer using Redis to ensure a smooth and efficient streaming experience.

For database operations, JDBC is used for its simplicity and efficiency in handling native SQL queries. JPA (Java Persistence API) is avoided due to a personal preference for the direct control and flexibility that JDBC offers.

## Technologies Used

- **Java & Spring Boot:** Core technologies for building the RESTful API.
- **Redis:** Utilized for caching frequently accessed data like playlists and track details, and for distributed locking to manage concurrent access to shared resources (e.g., streaming sessions).
- **MySQL:** Database for storing persistent data, including user profiles, track metadata, and playlists.
- **Flyway:** Database migration tool for managing schema version control, ensuring the database structure is up-to-date and consistent across environments.
- **Docker:** Used for containerizing the application.
- **Amazon S3:** Utilized for storing and serving music tracks, providing scalable and reliable storage for audio files.
- **Amazon CloudFront:** CDN service used to deliver music tracks with low latency and high transfer speeds, enhancing the streaming experience by reducing buffering and improving load times.
- **VNPay Sandbox:** Payment gateway used for processing transactions and managing user subscriptions, including a sandbox environment for testing payment integration.

## Database Migration

Database schema changes are managed using Flyway for consistency and version control. For detailed information on the database schema, migrations, and version history, please refer to the `db.migration` directory.

You can explore the `db.migration` directory to find:

- **Migration Scripts:** SQL scripts that define changes to the database schema, including table creation, modification, and data migrations.
- **Schema Definitions:** The current and historical structure of the database, detailing how different components of the database are organized and related.
- **Version History:** A record of schema changes and migration versions, providing insight into the evolution of the database schema.

For a comprehensive view of the database design and migration details, visit the [db.migration](src/main/resources/db/migration) directory.


## API Documentation

- For detailed API endpoint reference, view this project's complete [API documentation](https://spotify-clone-api-docs.vercel.app/docs/#)  
- The design of this project's endpoints is heavily inspired by Spotify’s API. Explore Spotify’s official API documentation at [Spotify for Developers](https://developer.spotify.com/documentation/web-api/).

## Features

### 1. **Player Service**

The `PlayerService` class manages streaming sessions for users, including:

- **Play Track:** Adds a specific track to the queue and processes advertisements based on user status and playback history.
- **Next/Previous Track:** Skips to the next or previous track in the session queue.
- **Pause/Resume Session:** Allows pausing and resuming the current streaming session.
- **Change Playback Mode:** Supports sequential, shuffle, and repeat modes, regenerating the queue accordingly.
- **Add to Queue:** Adds tracks to the current session's queue.
- **Session History:** Retrieves the user’s streaming history, stored in Redis for quick access.
- An event is produced when a track is streamed for at least 30 seconds.

### 2. **Redis Caching and Locking**

The `RedisService` class implements the `CacheService` interface:

- **Caching:** Stores session data, track details, and other frequently accessed information in Redis with configurable expiration times to enhance performance (TLS).
- **Distributed Locking:** Manages thread-safe operations across distributed service instances using Redisson’s locking mechanism.
- **Cache Management:** Provides methods to evict specific or all caches, ensuring efficient removal of stale data.

### 3. **User Subscriptions and Payment Processing**

The service differentiates between premium and non-premium users, impacting their streaming experience. It also handles user subscriptions and payments:

- **Premium Users:**
    - **Playback Modes:** Have access to all playback modes (shuffle, repeat, sequential).
    - **Advertisements:** Generally do not experience ads during playback.
    - **Playback Experience:** Enjoy an uninterrupted streaming experience with additional features.


- **Non-Premium Users:**
    - **Playback Mode Restriction:** Restricted to shuffle mode only.
    - **Advertisements:** Experience ads inserted into their playback queue at regular intervals (e.g., every 10 tracks).
    - **Playback Experience:** May have advertisements inserted to support the free-tier service.

    
- **Subscription Management:** Oversees the entire lifecycle of user subscriptions, including creation, renewal, and updates to subscription plans. This ensures users receive the appropriate features based on their subscription status.


- **Payment Processing:** Utilizes VNPay for secure payment transactions related to subscription services. This encompasses:
    - **Payment Requests:** Generating secure URLs for transactions and managing payment requests.
    - **Transaction Verification:** Confirming the results of transactions to update user subscription status accordingly.

- **VNPay Integration:** The `VNPAYConfig` class manages all necessary configurations for VNPay, including payment URLs, merchant codes, secret keys, and other essential settings for processing payments.
  

This is just a very basic implementation though as it is not this project main focus


### 4. **Basic CRUD Operations**

The API offers full CRUD (Create, Read, Update, Delete) operations for:

- **User Management:** Creating, updating, retrieving, and deleting user profiles.
- **Track, Album, Artist Profile Management:** Managing music tracks, including metadata updates and retrieval.
- **Playlist Management:** Creating and managing user playlists to organize favorite tracks.

## Future Plans

With the successful stream event already being produced in `PlayerService`, future improvements may include:

- **Trending Analysis:** Implement features to identify and display trending music based on current popularity.
- **Basic Recommendations:** Develop a collaborative filtering system to suggest tracks or playlists based on user listening habits.