<div align="center">
  <br>
  <img src="source/logo-dev.png" alt="">
  <h1>schedule university backend</h1>
</div>

[![eng readme](source/Yandex_Translate_icon.svg)](https://github.com/techstud-dev/schedule-university/blob/devel/add-readme/README-EN.md)


**Schedule University** - проект предназначенный для упрощения получения доступа к студенческому(и не только) расписанию для преподавателей и учащихся.

### 🦾 Мотивация проекта
К сожалению не все ВУЗы страны распологают достаточными техническими мощностями для создания и поддержки своих сервисов по расписанию, но так или иначе обладают собственными сайтами с расписанием.
Мы просто берем расписание и удобно оборачиваем его добавляя интеграции с популярными календарями и своевременные нотификации об изменении и выбранных событиях.

### ⚙️ Технический стек
Приложение использует классический стек.
- В базе написано на Java версии 21
- Как фреймворк используется Spring Boot версии 3.3.0
- БД для хранения PostgreSQL версии 16

Все приложения оборачиваются на этапе сборки в докер контейнеры и с их помощью деплоятся на вирутальных тачках.
- CI/CD мы используем GitHub Actions
### 😎 Как запустить локально
Для начала склонируйте репозиторий:
```bash
$ git clone https://github.com/techstud-dev/schedule-university.git
$ cd schedule-university
```

После задайте переменные окружения, мы используем дефолтные тестовые:
```text
SPRING_PROFILES_ACTIVE=your_profile  
DB_URL=your_db_url  
DB_USER=your_user  
DB_PASSWORD=your_password
```

Запустите бэкенд используя docker compose:
```bash
$ docker compose up
```
### 💣 Как зарепортить баг
Для того чтобы зарепортить баг создайте новое issues: [link](https://github.com/techstud-dev/schedule-university/issues/new)
👨‍💼 Для юзеров:
- постарайтесь максимально подробно описать к чему относится баг, приложите подробные скрины и подробный step-by-step как воспроизвести баг
  🧑‍💻 Для разработчиков:
-  к первому пункту дополнительно: обязательно приложите файл с расширением `log` чтобы наша команда могла оперативно проверить и понять в чем проблема

### 📬 Как предложить новую фичу
Напишите: *[@Funt1koff](https://github.com/Funt1koff)* , распишите что вы хотите предложить, как это поможет проекту.

### ❗️Контрибьютерам
Пока в работе 🚧.

### 👥 О нас
Команда которая пишет бэкенд:
- *[@Funt1koff](https://github.com/Funt1koff)*
- *[@EldarKhalilov](https://github.com/EldarKhalilov)*
- *[@MrNikaMilon](https://github.com/MrNikaMilon)* 
