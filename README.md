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
