INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'super-admin@domain.com', '0270193c-80de-11eb-8447-58040f3e0a78', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER')),
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER')),
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER')),
((SELECT id FROM identity WHERE email = 'super-admin@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_DELETE'))
;
