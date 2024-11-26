CREATE TABLE IF NOT EXISTS films (film_id integer);
CREATE TABLE IF NOT EXISTS users (user_id integer);
CREATE TABLE IF NOT EXISTS user_friends (user_id integer);
CREATE TABLE IF NOT EXISTS film_likes (film_id integer);
CREATE TABLE IF NOT EXISTS genres (genre_id integer);
CREATE TABLE IF NOT EXISTS film_genres (film_id integer);
CREATE TABLE IF NOT EXISTS mpa_dict (mpa_id integer);

DROP TABLE FILM_LIKES;
DROP TABLE FILM_GENRES;
DROP TABLE USER_FRIENDS;

DROP TABLE FILMS;
DROP TABLE USERS;
DROP TABLE GENRES;
DROP TABLE MPA_DICT;

CREATE TABLE IF NOT EXISTS mpa_dict (
    mpa_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name varchar(100),
    CONSTRAINT mpa_dict_pk PRIMARY KEY (mpa_id)
);

CREATE TABLE IF NOT EXISTS films (
    film_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name varchar(40) NOT NULL,
    description varchar(200),
    release_date date,
    duration integer,
    mpa_rating integer,
    CONSTRAINT films_pk PRIMARY KEY (film_id),
    FOREIGN KEY (mpa_rating) REFERENCES mpa_dict(mpa_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    email varchar(100) NOT NULL,
    login varchar(100) NOT NULL,
    name varchar(100),
    birthday date,
    CONSTRAINT users_pk PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_friends (
    user_id integer NOT NULL,
    friend_id integer NOT NULL,
    status integer NOT NULL DEFAULT 0,
    CONSTRAINT user_friends_pk PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (friend_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id integer NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT film_likes_pk PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name varchar(100),
    CONSTRAINT genres_pk PRIMARY KEY (genre_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id integer NOT NULL,
    genre_id integer NOT NULL,
    CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);
