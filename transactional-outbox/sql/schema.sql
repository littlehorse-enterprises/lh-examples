CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE transactions(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  account VARCHAR NOT NULL,
  amount NUMERIC NOT NULL DEFAULT 0,
  idempotency_key VARCHAR NOT NULL
);

CREATE UNIQUE INDEX ON transactions(account, idempotency_key);

CREATE MATERIALIZED VIEW balances(account, balance)
  AS
    SELECT
      account,
      COALESCE(sum(amount), 0.0)
    FROM
      transactions
      GROUP BY account;

CREATE UNIQUE INDEX ON balances(account);

CREATE FUNCTION update_balances() RETURNS TRIGGER AS $$
BEGIN
  REFRESH MATERIALIZED VIEW balances;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_balances
AFTER INSERT
OR UPDATE OF amount
ON transactions
FOR EACH STATEMENT
EXECUTE PROCEDURE update_balances();

CREATE FUNCTION check_balance() RETURNS TRIGGER AS $$
DECLARE
  _current_balance NUMERIC;
  _future_balance NUMERIC;
BEGIN
  SELECT COALESCE((SELECT balance FROM balances WHERE account = NEW.account), 0) INTO _current_balance;

  IF (_current_balance + NEW.amount) < 0 THEN
    RAISE EXCEPTION 'Not enough balance, tried % has %', NEW.amount, _current_balance;
  END IF;
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_balance
  BEFORE INSERT OR UPDATE ON transactions
  FOR EACH ROW
EXECUTE PROCEDURE check_balance();
