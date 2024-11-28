# java-filmorate
Template repository for Filmorate project.
![Диаграмма таблицы проекта](https://github.com/Sergei-Lukashenko/java-filmorate/blob/main/Схема%20БД%20filmorate.png)

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
##### _Запрос на получение топ 10 наиболее популярных фильмов_
```sql
SELECT *
FROM films AS f
JOIN
  (SELECT film_id,
          count(user_id) AS likes_count
   FROM film_likes
   GROUP BY film_id
   ORDER BY likes_count DESC
   LIMIT (10)) AS t ON f.film_id = t.film_id
ORDER BY t.likes_count;
```
#####  Запрос на получение общих друзей для пользователей с идентификаторами 1 и 2
```sql
SELECT *
FROM users
WHERE user_id IN
    (SELECT friend_id
     FROM user_friends
     WHERE user_id = 1 INTERSECT
       SELECT friend_id
       FROM user_friends WHERE user_id = 2);
```