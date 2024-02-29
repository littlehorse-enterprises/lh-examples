CREATE TABLE transactions(
  id VARCHAR PRIMARY KEY,
  source_account VARCHAR NOT NULL,
  destination_account VARCHAR NOT NULL,
  amount INTEGER NOT NULL,
  status VARCHAR NOT NULL
);

