CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE customers(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  firstname VARCHAR NOT NULL,
  lastname VARCHAR NOT NULL,
  email VARCHAR NOT NULL
);

CREATE UNIQUE INDEX ON customers(email);
