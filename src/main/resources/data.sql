DELETE FROM FILM_LIKES;
DELETE FROM FILM_GENRES;
DELETE FROM USER_FRIENDS;

DELETE FROM FILMS;
DELETE FROM USERS;
DELETE FROM GENRES;
DELETE FROM MPA_DICT;

INSERT INTO MPA_DICT
(MPA_ID, NAME)
VALUES (1, 'G');
INSERT INTO MPA_DICT
(MPA_ID, NAME)
VALUES (2, 'PG');
INSERT INTO MPA_DICT
(MPA_ID, NAME)
VALUES (3, 'PG-13');
INSERT INTO MPA_DICT
(MPA_ID, NAME)
VALUES (4, 'R');
INSERT INTO MPA_DICT
(MPA_ID, NAME)
VALUES (5, 'NC-17');

INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(1, 'Комедия');
INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(2, 'Драма');
INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(3, 'Мультфильм');
INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(4, 'Триллер');
INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(5, 'Документальный');
INSERT INTO GENRES(GENRE_ID, NAME)
VALUES(6, 'Боевик');
