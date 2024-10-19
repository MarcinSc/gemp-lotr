-- Support for password reset and email update
ALTER TABLE gemp_db.player
    ADD COLUMN `password_reset_token` varchar(20) COLLATE utf8_bin DEFAULT NULL;
ALTER TABLE gemp_db.player
    ADD COLUMN `new_email` varchar(128) COLLATE utf8_bin DEFAULT NULL;
ALTER TABLE gemp_db.player
    ADD COLUMN `change_email_token` varchar(20) COLLATE utf8_bin DEFAULT NULL;
