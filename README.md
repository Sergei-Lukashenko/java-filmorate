# java-filmorate
Template repository for Filmorate project.
![Диаграмма таблицы проекта](/"Схема БД filmorate.png")

##### _Запрос на получение списка всех пользователей:_
```sql
SELECT *
FROM users;
```

##### _Запрос на получение списка всех фильмов:_
```sql
SELECT *
FROM films;
```
##### _Запрос на получение топ N наиболее популярных фильмов_
```sql
SELECT *
FROM films
WHERE film_id IN (SELECT film_id, count(user_id) AS likes_count
                  FROM film_likes
                  GROUP BY film_id
                  ORDER BY likes_count DESC
                  LIMIT (10));
```
