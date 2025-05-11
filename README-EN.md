<div align="center">
  <br>
  <img src="source/logo-dev.png" alt="">
  <h1>schedule university backend</h1>
</div>

[![eng readme](source/Yandex_Translate_icon.svg)](https://github.com/techstud-dev/schedule-university/blob/devel/add-readme/README-EN.md)

**Schedule University** is a project designed to simplify access to student (and not only) schedules for teachers and students.

### 🦾 Motivation of the project
Unfortunately, not all universities in the country have sufficient technical capacity to create and support their own schedule services, but somehow have their own websites with schedules.
We simply take the timetable and wrap it conveniently by adding integrations with popular calendars and timely notifications about changes and selected events.

### ⚙️ Technical Stack
The application uses the classic stack.
- The base is written in Java version 21
- Spring Boot version 3.3.0 is used as a framework
- The database for storage is PostgreSQL version 16

All applications are wrapped at the build stage in docker containers and deployed on virutal machines using them.
- CI/CD we use GitHub Actions

### 😎 Как запустить локально
Start by cloning the repository:
```bash
$ git clone https://github.com/techstud-dev/schedule-university.git
$ cd schedule-university
```

After that set environment variables, we use the default test variables:
```text
SPRING_PROFILES_ACTIVE=your_profile  
DB_URL=your_db_url  
DB_USER=your_user  
DB_PASSWORD=your_password
```

Run the backend using docker compose:
```bash
$ docker compose up
```
### 💣 How to report a bug
To report a bug create a new issue: [link](https://github.com/techstud-dev/schedule-university/issues/new)
👨‍💼 For users:
- try to describe in as much detail as possible what the bug relates to, attach detailed screens and a detailed step-by-step how to reproduce the bug
  🧑‍💻 For developers:
- to the first point additionally: be sure to attach a file with `log` extension so that our team can quickly check and understand what the problem is

### 📬 How to suggest a new feature
Write: *[@Funt1koff](https://github.com/Funt1koff)* , describe what you want to propose, how it will help the project.

### ❗Contributors
Пока в работе 🚧.

### 👥 About us
The team that writes the backend:
- *[@Funt1koff](https://github.com/Funt1koff)*
- *[@EldarKhalilov](https://github.com/EldarKhalilov)*
- *[@MrNikaMilon](https://github.com/MrNikaMilon)* 
