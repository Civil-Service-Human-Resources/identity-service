ALTER TABLE `identity` ADD `last_logged_in` datetime NOT NULL;
ALTER TABLE `identity` ADD `deletion_notification_sent` bit(1) DEFAULT FALSE;

UPDATE `identity` SET `last_logged_in` = GETDATE() WHERE `last_logged_in` = null;

