
INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'learner-organisation-1@domain.com', '4a1ac67c-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-organisation-2@domain.com', '4cee0b52-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-profession-1@domain.com', '57e01852-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-profession-2@domain.com', '5af24470-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-cshr@domain.com', '5e22cb92-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-booking-feed@domain.com', '615965a0-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-supplier@domain.com', '649d23e6-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-kpmg-supplier@domain.com', '67e0636a-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'learner-kornferry-supplier@domain.com', '6b0163be-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'learner-organisation-1@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-organisation-2@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-profession-1@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-profession-2@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-cshr@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-booking-feed@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-supplier@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-kpmg-supplier@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learner-kornferry-supplier@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER'))
;
