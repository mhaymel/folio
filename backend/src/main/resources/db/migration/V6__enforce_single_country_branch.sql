-- Each ISIN maps to exactly one country and one branch (1:1).
-- The import logic already enforces this via delete+insert; these constraints make it explicit at the DB level.
ALTER TABLE isin_country ADD CONSTRAINT uq_isin_country_isin UNIQUE (isin_id);
ALTER TABLE isin_branch  ADD CONSTRAINT uq_isin_branch_isin  UNIQUE (isin_id);