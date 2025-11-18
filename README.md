# Базовые SQL запросы для Filmorate (PostgreSQL)

## ПОЛЬЗОВАТЕЛИ

<details><summary>Получить всех пользователей</summary>

```sql
SELECT userId, email, login, name, birthday 
FROM users;
```
</details>

<details><summary>Получить пользователя по ID</summary>

```sql
SELECT userId, email, login, name, birthday 
FROM users 
WHERE userId = 1;
```
</details>

<details><summary>Получить пользователя по email</summary>

```sql
SELECT userId, email, login, name, birthday 
FROM users 
WHERE email = 'user@example.com';
```
</details>

<details><summary>Добавить нового пользователя</summary>

```sql
INSERT INTO users (email, login, name, birthday)
VALUES ('ivan@mail.ru', 'ivan123', 'Иван Иванов', '1990-05-16');
```
</details>

<details><summary>Обновить данные пользователя</summary>

```sql
UPDATE users 
SET name = 'Иван Петров', email = 'new_email@mail.ru'
WHERE userId = 1;
```
</details>

<details><summary>Удалить пользователя</summary>

```sql
DELETE FROM users WHERE userId = 1;
```
</details>

---

## ДРУЖБА

<details><summary>Получить всех друзей пользователя (подтверждённые)</summary>

```sql
SELECT u.userId, u.email, u.login, u.name, u.birthday 
FROM users u
JOIN friendships f ON u.userId = f.friendTo
WHERE f.friendFrom = 1 AND f.acceptanceStatus = true;
```
</details>

<details><summary>Получить входящие заявки в друзья (неподтверждённые)</summary>

```sql
SELECT u.userId, u.email, u.login, u.name, u.birthday 
FROM users u
JOIN friendships f ON u.userId = f.friendFrom
WHERE f.friendTo = 1 AND f.acceptanceStatus = false;
```
</details>

<details><summary>Отправить заявку в друзья</summary>

```sql
INSERT INTO friendships (friendFrom, friendTo, acceptanceStatus)
VALUES (1, 2, false);
```
</details>

<details><summary>Принять заявку в друзья</summary>

```sql
UPDATE friendships 
SET acceptanceStatus = true
WHERE friendFrom = 2 AND friendTo = 1;
```
</details>

<details><summary>Удалить из друзей</summary>

```sql
DELETE FROM friendships 
WHERE friendFrom = 1 AND friendTo = 2;
```
</details>

<details><summary>Получить общих друзей двух пользователей</summary>

```sql
SELECT DISTINCT u.userId, u.email, u.login, u.name, u.birthday
FROM users u
JOIN friendships f1 ON u.userId = f1.friendTo
JOIN friendships f2 ON u.userId = f2.friendTo
WHERE f1.friendFrom = 1 AND f2.friendFrom = 2
  AND f1.acceptanceStatus = true AND f2.acceptanceStatus = true;
```
</details>

---

## ФИЛЬМЫ

<details><summary>Получить все фильмы</summary>

```sql
SELECT filmId, name, description, created_at, duration 
FROM films;
```
</details>

<details><summary>Получить фильм по ID</summary>

```sql
SELECT filmId, name, description, created_at, duration 
FROM films 
WHERE filmId = 1;
```
</details>

<details><summary>Получить фильмы с жанрами и рейтингом</summary>

```sql
SELECT 
    f.filmId,
    f.name,
    f.description,
    f.created_at,
    f.duration,
    g.genre,
    m.rating as mpa_rating
FROM films f
LEFT JOIN filmsGenres fg ON f.filmId = fg.filmId
LEFT JOIN genres g ON fg.genreId = g.genreId
LEFT JOIN filmsMpaRatings fmr ON f.filmId = fmr.filmId
LEFT JOIN mpaRatings m ON fmr.ratingId = m.ratingId;
```
</details>

<details><summary>Добавить новый фильм</summary>

```sql
INSERT INTO films (name, description, created_at, duration)
VALUES ('Матрица', 'Научная фантастика о виртуальной реальности', '1999-03-31', 136);
```
</details>

<details><summary>Добавить жанр к фильму</summary>

```sql
INSERT INTO filmsGenres (filmId, genreId)
VALUES (1, 2);
```
</details>

<details><summary>Добавить MPA рейтинг к фильму</summary>

```sql
INSERT INTO filmsMpaRatings (filmId, ratingId)
VALUES (1, 3);
```
</details>

<details><summary>Обновить данные фильма</summary>

```sql
UPDATE films 
SET name = 'Новое название', duration = 120
WHERE filmId = 1;
```
</details>

<details><summary>Удалить фильм</summary>

```sql
DELETE FROM films WHERE filmId = 1;
```
</details>

---

## ЛАЙКИ

<details><summary>Получить все лайки пользователя</summary>

```sql
SELECT f.filmId, f.name, f.description, f.created_at, f.duration 
FROM films f
JOIN likes l ON f.filmId = l.filmId
WHERE l.userId = 1;
```
</details>

<details><summary>Получить количество лайков для фильма</summary>

```sql
SELECT COUNT(*) as likes_count
FROM likes
WHERE filmId = 1;
```
</details>

<details><summary>Поставить лайк фильму</summary>

```sql
INSERT INTO likes (userId, filmId)
VALUES (1, 5);
```
</details>

<details><summary>Убрать лайк</summary>

```sql
DELETE FROM likes 
WHERE userId = 1 AND filmId = 5;
```
</details>

<details><summary>Получить самые популярные фильмы (по лайкам)</summary>

```sql
SELECT f.filmId, f.name, f.description, f.created_at, f.duration, COUNT(l.userId) as likes_count
FROM films f
LEFT JOIN likes l ON f.filmId = l.filmId
GROUP BY f.filmId, f.name, f.description, f.created_at, f.duration
ORDER BY likes_count DESC
LIMIT 10;
```
</details>

---

## ЖАНРЫ

<details><summary>Получить все жанры</summary>

```sql
SELECT genreId, genre 
FROM genres;
```
</details>

<details><summary>Получить фильмы определённого жанра</summary>

```sql
SELECT f.filmId, f.name, f.description, f.created_at, f.duration
FROM films f
JOIN filmsGenres fg ON f.filmId = fg.filmId
WHERE fg.genreId = 1;
```
</details>

<details><summary>Добавить новый жанр</summary>

```sql
INSERT INTO genres (genre)
VALUES ('Комедия');
```
</details>

---

## MPA РЕЙТИНГИ

<details><summary>Получить все рейтинги</summary>

```sql
SELECT ratingId, rating 
FROM mpaRatings;
```
</details>

<details><summary>Получить фильмы с определённым рейтингом</summary>

```sql
SELECT f.filmId, f.name, f.description, f.created_at, f.duration
FROM films f
JOIN filmsMpaRatings fmr ON f.filmId = fmr.filmId
WHERE fmr.ratingId = 2;
```
</details>

<details><summary>Добавить новый рейтинг</summary>

```sql
INSERT INTO mpaRatings (rating)
VALUES ('PG-13');
```
</details>

---

## СЛОЖНЫЕ ЗАПРОСЫ

<details><summary>Рекомендации: фильмы, которые понравились друзьям, но не просмотрены пользователем</summary>

```sql
SELECT DISTINCT f.filmId, f.name, f.description, f.created_at, f.duration
FROM films f
JOIN likes l ON f.filmId = l.filmId
JOIN friendships fr ON l.userId = fr.friendTo
WHERE fr.friendFrom = 1 
  AND fr.acceptanceStatus = true
  AND f.filmId NOT IN (
    SELECT filmId FROM likes WHERE userId = 1
  );
```
</details>

<details><summary>Топ-10 фильмов по лайкам с жанрами</summary>

```sql
SELECT 
    f.filmId,
    f.name,
    COUNT(DISTINCT l.userId) as likes_count,
    STRING_AGG(DISTINCT g.genre, ', ') as genres
FROM films f
LEFT JOIN likes l ON f.filmId = l.filmId
LEFT JOIN filmsGenres fg ON f.filmId = fg.filmId
LEFT JOIN genres g ON fg.genreId = g.genreId
GROUP BY f.filmId, f.name
ORDER BY likes_count DESC
LIMIT 10;
```
</details>

<details><summary>Пользователи с наибольшим количеством друзей</summary>

```sql
SELECT 
    u.userId,
    u.name,
    COUNT(f.friendTo) as friends_count
FROM users u
LEFT JOIN friendships f ON u.userId = f.friendFrom AND f.acceptanceStatus = true
GROUP BY u.userId, u.name
ORDER BY friends_count DESC
LIMIT 10;
```
</details>