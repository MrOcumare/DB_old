CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  fullname text,
  nickname  CITEXT COLLATE ucs_basic NOT NULL UNIQUE,
  email CITEXT NOT NULL UNIQUE,
  about text
);