INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'learning-manager@domain.com', '2afb9a64-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'organisation-manager@domain.com', '40e16ea8-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'profession-manager@domain.com', '441d35de-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'identity-delete@domain.com', '47270566-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'learning-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'learning-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'organisation-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'organisation-manager@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'profession-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'profession-manager@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'identity-delete@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'identity-delete@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_DELETE')),
((SELECT id FROM identity WHERE email = 'identity-delete@domain.com'), (SELECT id FROM role WHERE name = 'IDENTITY_MANAGER'))
;
