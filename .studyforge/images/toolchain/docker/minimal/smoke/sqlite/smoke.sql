-- An in-memory database; the CHECK constraint is the assertion, and `-bail`
-- turns its failure into a non-zero exit.
CREATE TABLE answer (value INTEGER CHECK (value = 2));
INSERT INTO answer VALUES (1 + 1);
SELECT 'smoke: sqlite ok';
