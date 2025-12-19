INSERT INTO genres (id, genre)
SELECT 1, 'Комедия'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 1);

INSERT INTO genres (id, genre)
SELECT 2, 'Драма'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 2);

INSERT INTO genres (id, genre)
SELECT 3, 'Мультфильм'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 3);

INSERT INTO genres (id, genre)
SELECT 4, 'Триллер'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 4);

INSERT INTO genres (id, genre)
SELECT 5, 'Документальный'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 5);

INSERT INTO genres (id, genre)
SELECT 6, 'Боевик'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 6);

INSERT INTO mpaRatings (id, rating)
SELECT 1, 'G'
    WHERE NOT EXISTS (SELECT 1 FROM mpaRatings WHERE id = 1);

INSERT INTO mpaRatings (id, rating)
SELECT 2, 'PG'
    WHERE NOT EXISTS (SELECT 1 FROM mpaRatings WHERE id = 2);

INSERT INTO mpaRatings (id, rating)
SELECT 3, 'PG-13'
    WHERE NOT EXISTS (SELECT 1 FROM mpaRatings WHERE id = 3);

INSERT INTO mpaRatings (id, rating)
SELECT 4, 'R'
    WHERE NOT EXISTS (SELECT 1 FROM mpaRatings WHERE id = 4);

INSERT INTO mpaRatings (id, rating)
SELECT 5, 'NC-17'
    WHERE NOT EXISTS (SELECT 1 FROM mpaRatings WHERE id = 5);