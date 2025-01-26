CREATE TYPE expense_category_enum AS ENUM ('product', 'service');

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    account_from INT NOT NULL,
    account_to INT NOT NULL,
    currency_shortname VARCHAR(3) NOT NULL,
    sum NUMERIC(18,2) NOT NULL,
    expense_category expense_category_enum NOT NULL,
    datetime TIMESTAMPTZ NOT NULL,
    limit_exceeded BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE expense_limit (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    datetime TIMESTAMPTZ NOT NULL DEFAULT now(),
    sum NUMERIC(18,2) NOT NULL,
    expense_category expense_category_enum NOT NULL
);