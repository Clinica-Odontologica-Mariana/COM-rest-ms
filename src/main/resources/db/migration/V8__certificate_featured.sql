-- Featured certificates are the ones shown publicly on the home page.
-- Admin selects up to 3 (enforced in the service layer).
ALTER TABLE certificate
    ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
