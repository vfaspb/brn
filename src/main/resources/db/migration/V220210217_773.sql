ALTER TABLE user_account ALTER COLUMN email DROP NOT NULL;
ALTER TABLE user_account ALTER COLUMN full_name DROP NOT NULL;
ALTER TABLE user_account ALTER COLUMN password DROP NOT NULL;
ALTER TABLE user_account ADD COLUMN user_id VARCHAR (255);