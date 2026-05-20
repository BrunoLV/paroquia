-- Migration to refactor event categories from a table to an Enum
-- This adds the new 'categoria' column and removes the now obsolete 'categorias' table.

-- 1. Add the new column to 'eventos'
ALTER TABLE calendario.eventos ADD COLUMN categoria VARCHAR(32);

-- 2. Drop the existing 'categorias' table
DROP TABLE IF EXISTS calendario.categorias;
