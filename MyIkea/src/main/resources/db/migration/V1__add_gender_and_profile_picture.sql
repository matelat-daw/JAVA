-- Migration: Add gender and profile_picture columns to users table
-- This migration adds the gender field with default value 'female' 
-- and ensures profile_picture column exists

-- Check and add gender column if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS gender VARCHAR(20) DEFAULT 'female' AFTER password;

-- Check and add/update profile_picture column if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(255) DEFAULT NULL AFTER gender;

-- Update existing users with default profile pictures based on gender
UPDATE users 
SET profile_picture = CASE 
    WHEN gender = 'male' THEN '/images/male.png'
    WHEN gender = 'female' THEN '/images/female.png'
    ELSE '/images/other.png'
END
WHERE profile_picture IS NULL;
