-- Search engine indexing is opt-in; existing public share links remain unchanged.
ALTER TABLE profiles
    ADD COLUMN indexable BOOLEAN NOT NULL DEFAULT FALSE;
