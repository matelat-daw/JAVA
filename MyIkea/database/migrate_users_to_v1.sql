-- Direct SQL Migration Script for MyIkea Database
-- Execute this script if you need to manually update your existing database
-- This adds the gender field (if not exists) and sets up profile pictures

-- Step 1: Add gender column if it doesn't exist
ALTER TABLE users 
ADD COLUMN gender VARCHAR(20) DEFAULT 'female' AFTER password;

-- Step 2: Add profile_picture column if it doesn't exist  
ALTER TABLE users 
ADD COLUMN profile_picture VARCHAR(255) DEFAULT NULL AFTER gender;

-- Step 3: Update seed users with their respective profile pictures
UPDATE users SET profile_picture = '/images/female.png' WHERE username = 'user' AND gender = 'female';
UPDATE users SET profile_picture = '/images/male.png' WHERE username = 'manager' AND gender = 'male';
UPDATE users SET profile_picture = '/images/male.png' WHERE username = 'admin1' AND gender = 'male';
UPDATE users SET profile_picture = '/images/other.png' WHERE username = 'admin2' AND gender = 'other';

-- Verification: Check the users table structure
DESCRIBE users;

-- Verification: Check the updated users data
SELECT id, username, email, gender, profile_picture FROM users;
