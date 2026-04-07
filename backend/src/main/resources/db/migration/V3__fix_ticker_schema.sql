-- Remove isin_id from ticker_symbol (the isin link belongs in isin_ticker only)
ALTER TABLE ticker_symbol DROP COLUMN isin_id;

-- Add UNIQUE constraint on symbol (each ticker string is stored once)
ALTER TABLE ticker_symbol ADD CONSTRAINT uq_ticker_symbol UNIQUE (symbol);

-- Rebuild isin_ticker with a surrogate PK (preserve existing rows)
CREATE TABLE isin_ticker_new (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isin_id INTEGER NOT NULL REFERENCES isin(id),
    ticker_symbol_id INTEGER NOT NULL REFERENCES ticker_symbol(id),
    UNIQUE (isin_id, ticker_symbol_id)
);
INSERT INTO isin_ticker_new (isin_id, ticker_symbol_id)
SELECT isin_id, ticker_symbol_id FROM isin_ticker;
DROP TABLE isin_ticker;
ALTER TABLE isin_ticker_new RENAME TO isin_ticker;
