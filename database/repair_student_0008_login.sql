-- EduTrack: repair login for an already-registered student
-- Student username: STU-2026-0008
-- Password after running this script: EduTrack123
-- The password is stored as the same PBKDF2 format used by PasswordUtil.java.
-- This UPDATE is compatible with MySQL and SQL Server.

UPDATE users
SET password_hash = 'pbkdf2$210000$00000000000000000000000000000008$061faaccda6e0803c4c3fa11f4672e2e28e342e698e5560ce6235c5926b92470',
    active = 1
WHERE username = 'STU-2026-0008'
  AND user_type = 'STUDENT';

-- Optional verification:
-- SELECT id, username, user_type, active, student_id FROM users
-- WHERE username = 'STU-2026-0008';
