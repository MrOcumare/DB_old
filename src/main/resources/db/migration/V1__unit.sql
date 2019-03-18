CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  fullname text,
  nickname  CITEXT COLLATE ucs_basic NOT NULL UNIQUE,
  email CITEXT NOT NULL UNIQUE,
  about text
);
CREATE TABLE forum (
  id          SERIAL PRIMARY KEY,
  slug        CITEXT UNIQUE NOT NULL,
  title       TEXT          NOT NULL,
  owner       CITEXT REFERENCES users (nickname)
);

CREATE TABLE thread (
  tid     SERIAL PRIMARY KEY,
  slug    CITEXT UNIQUE,
  owner   CITEXT REFERENCES users (nickname),
  forum   CITEXT REFERENCES forum (slug),
  forumid INTEGER,
  created TIMESTAMP WITH TIME ZONE,
  message TEXT NOT NULL,
  title   TEXT NOT NULL,
  votes   BIGINT
);
