CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE transactions(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source_account VARCHAR NOT NULL,
  destination_account VARCHAR NOT NULL,
  amount INTEGER NOT NULL,
  status VARCHAR NOT NULL
);

CREATE UNIQUE INDEX ON transactions(account, idempotency_key);
