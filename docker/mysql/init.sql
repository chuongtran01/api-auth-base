-- Initialize the authbase database
-- This script runs when the MySQL container starts for the first time

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS authbase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE authbase;

-- Create a dedicated user for the application (optional)
-- GRANT ALL PRIVILEGES ON authbase.* TO 'authbase_user'@'%';
-- FLUSH PRIVILEGES;

-- Note: Tables will be created automatically by Hibernate/JPA
-- when the Spring Boot application starts with ddl-auto=create-drop 