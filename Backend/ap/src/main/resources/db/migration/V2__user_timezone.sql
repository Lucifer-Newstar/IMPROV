-- V2 — the user's timezone (locked decision D2).
--
-- "Today" for quests and streaks is the user's local day, computed on the
-- server from this column. Without it, every streak bug is a timezone bug.
-- Existing rows get UTC; users can change it later (history is not rewritten).

ALTER TABLE users
    ADD COLUMN timezone VARCHAR(64) NOT NULL DEFAULT 'UTC';
