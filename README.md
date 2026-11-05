../../outputs/README.md
<div align="center">
  <img src="src/main/webapp/res/SOMTODAY_LOGO.png" alt="Somtoday Homework Planner" width="220">

  # Somtoday Homework Planner

  A role-based homework planning web app for students and teachers.

  [![Java](https://img.shields.io/badge/Java-11-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
  [![Jersey](https://img.shields.io/badge/Jersey-3.1-5B8C5A)](https://eclipse-ee4j.github.io/jersey/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
</div>

## About the project

Somtoday Homework Planner brings classes, lessons, assignments, goals, and notifications into one responsive web application. Teachers can organize their classes and publish homework, while students can see upcoming work and request that larger assignments be split into smaller tasks.

The application uses a plain HTML/CSS/JavaScript frontend, a Java REST API built with Jersey, and PostgreSQL for persistence. It is packaged as a WAR and can run on Tomcat 10.1 or in Docker.

## Features

### For students

- View classes, lessons, and assignments from one dashboard
- See assignment descriptions, goals, due dates, and time estimates
- Request that splittable homework be divided into smaller tasks
- Review notifications and upcoming work
- View and update personal profile details

### For teachers

- Create and manage classes and class pictures
- Add or remove students from classes
- Create, edit, and delete lessons and assignments
- Add learning goals and time estimates to assignments
- Split homework into smaller tasks for an entire class
- Review student split requests
- Notify students when new assignments are published

### Shared experience

- Student, teacher, and admin-aware navigation
- Responsive layout for desktop and mobile browsers
- Sign-up and login flow
- In-app help and FAQ page

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | HTML5, CSS3, vanilla JavaScript |
| REST API | Java 11, Jakarta REST, Jersey 3 |
| Data | PostgreSQL, JDBC |
| JSON | Jackson and `org.json` |
| Server | Apache Tomcat 10.1 |
| Build | Maven, WAR packaging |
| Deployment | Multi-stage Docker build |

## Architecture

```text
Browser
  ├── HTML pages
  ├── CSS styles
  └── JavaScript modules
           │
           │ fetch("api/...")
           ▼
Jersey REST API (/api)
           │
           │ JDBC
           ▼
PostgreSQL
```

The browser keeps the signed-in user's `personId`, `personName`, and `role` in session storage. REST resources live under `/api`, and all database connections are configured through environment variables.

## Getting started

### Prerequisites

- Docker, **or** Java 11 plus Tomcat 10.1
- A PostgreSQL database with SSL enabled
- The Somtoday database schema and any required seed data

> [!IMPORTANT]
> The database schema and migrations are not included in this repository. You need an existing compatible database before the application can be used. The current connection code always adds `sslmode=require`.

### 1. Clone the repository

```bash
git clone https://github.com/akdev47/SomtodayHomeworkPlanner.git
cd SomtodayHomeworkPlanner
```
### 2. Configure the database

The application expects these environment variables:

| Variable | Description | Example |
| --- | --- | --- |
| `DB_HOST` | PostgreSQL hostname | `db.example.com` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `somtoday` |
| `DB_USER` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `change-me` |

Never commit real credentials to the repository.

### 3. Run with Docker

Build the image:

```bash
docker build -t somtoday-homework-planner .
```

Start the app with your database configuration:

```bash
docker run --rm -p 8080:8080 \
  -e DB_HOST=db.example.com \
  -e DB_PORT=5432 \
  -e DB_NAME=somtoday \
  -e DB_USER=postgres \
  -e DB_PASSWORD=change-me \
  somtoday-homework-planner
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

### Run with Maven and Tomcat

Build the WAR with the included Maven wrapper:

```bash
./mvnw clean package
```

On Windows, use `mvnw.cmd clean package` instead. The generated file is:

```text
target/jaxrs-tomcat-example.war
```

Set the database environment variables for your Tomcat process, copy the WAR into Tomcat's `webapps` directory, and start Tomcat. The URL depends on the deployed context name; renaming the file to `ROOT.war` serves the app at the server root.

## API overview

All endpoints are served below `/api`.

| Area | Example endpoints |
| --- | --- |
| Authentication | `POST /login`, `POST /signup` |
| Profiles | `GET /profile`, `PUT /profile` |
| Classes | `GET /fetchClasses`, `POST /addToClass`, `DELETE /deleteClass` |
| Students | `GET /fetchStudents`, `POST /editStudentsInClass` |
| Lessons | `GET /fetchLessonsPage`, `POST /addLesson`, `DELETE /deleteLesson` |
| Assignments | `GET /fetchAssignments`, `POST /addAssignment`, `POST /editAssignment` |
| Homework splitting | `POST /splitHomeworkStudent`, `POST /splitHomeworkTeacher` |
| Notifications | `GET /fetchNotifications` |

See the resource classes in [`src/main/java/jaxRS`](src/main/java/jaxRS) for request parameters and response formats.

## Project structure

```text
.
├── src/main/java
│   ├── jaxRS/              # REST resources and API configuration
│   └── somtoday/
│       ├── db/             # PostgreSQL connection helper
│       └── model/          # Domain models
├── src/main/webapp
│   ├── javascript/         # Page behavior and API calls
│   ├── style/              # Page and component styles
│   ├── res/                # Logos, icons, and images
│   └── WEB-INF/web.xml     # Web application configuration
├── Dockerfile
├── pom.xml
└── mvnw / mvnw.cmd
```

## Development notes

- The frontend calls relative `api/...` URLs, so it should be served by the same Tomcat application as the REST API.
- Authentication state is currently stored in browser session storage.
- Passwords are hashed with SHA-256 before being stored. For a production deployment, use a password-specific adaptive hashing algorithm such as Argon2id or bcrypt and add server-managed sessions.
- Add migrations or a schema export before expecting new contributors to run the project from a clean database.

## Contributing

Contributions are welcome. To propose a change:

1. Fork the repository.
2. Create a branch: `git checkout -b feature/your-feature`.
3. Make and test your changes.
4. Commit with a clear message.
5. Open a pull request describing what changed and why.

For bugs, include the steps to reproduce the problem, the expected behavior, and screenshots or logs when useful.

---

<div align="center">
  Built to make homework planning clearer for students and teachers.
</div>
