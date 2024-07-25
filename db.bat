@echo off
REM Define Flyway properties
set FLYWAY_URL=jdbc:mysql://localhost:3306/spotify
set FLYWAY_USER=username
set FLYWAY_PASSWORD=password
set FLYWAY_LOCATIONS=filesystem:src/main/resources/db/migration

REM Check the first argument
if "%1"=="clean" (
    echo Running Flyway clean...
    mvn flyway:clean -D flyway.url=%FLYWAY_URL% -D flyway.user=%FLYWAY_USER% -D flyway.password=%FLYWAY_PASSWORD% -D flyway.cleanDisabled=false -D flyway.locations=%FLYWAY_LOCATIONS%
    goto end
)

REM Check if the first argument is "migrate"
if "%1"=="migrate" (
    REM Check if a version is specified
    if "%2"=="" (
        echo Running Flyway migrate...
        mvn flyway:migrate -D flyway.url=%FLYWAY_URL% -D flyway.user=%FLYWAY_USER% -D flyway.password=%FLYWAY_PASSWORD% -D flyway.locations=%FLYWAY_LOCATIONS%
    ) else (
        echo Running Flyway migrate for version %2...
        mvn flyway:migrate -D flyway.url=%FLYWAY_URL% -D flyway.user=%FLYWAY_USER% -D flyway.password=%FLYWAY_PASSWORD% -D flyway.locations=%FLYWAY_LOCATIONS% -D flyway.target=%2
    )
    goto end
)

echo Invalid command. Usage:
echo db clean
echo db migrate [version]

:end
pause
