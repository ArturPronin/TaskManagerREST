DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS user_tasks CASCADE;


CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS tasks
(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_user_id BIGINT
    );

CREATE TABLE IF NOT EXISTS user_tasks
(
    user_id BIGINT REFERENCES users(id),
    task_id BIGINT REFERENCES tasks(id),
    PRIMARY KEY(user_id, task_id)
    );

INSERT INTO users (name)
VALUES ('User1'),
       ('User2'),
       ('User3'),
       ('User4'),
       ('User5');

INSERT INTO tasks (title, description, assigned_user_id)
VALUES ('Task1', 'desc 1', 1),
       ('Task2', 'desc 2', 2),
       ('Task3', 'desc 3', 3),
       ('Task4', 'desc 4', 4),
       ('Task5', 'desc 5', 5),
       ('Task6', 'desc 6', 1);

INSERT INTO user_tasks (user_id, task_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 4),
       (5, 5),
       (1, 6);




