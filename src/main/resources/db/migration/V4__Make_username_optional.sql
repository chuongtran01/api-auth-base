-- Migration: Make username optional (nullable) and keep unique constraint
ALTER TABLE users MODIFY username VARCHAR(100) NULL UNIQUE; 