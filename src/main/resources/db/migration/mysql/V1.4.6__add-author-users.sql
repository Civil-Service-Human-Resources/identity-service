INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'csl-author@domain.com', '1731fa28-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'organisation-author@domain.com', '1a224134-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'profession-author@domain.com', '1d4689ba-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'kpmg-supplier-author@domain.com', '204d623c-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'kornferry-supplier-author@domain.com', '2355b7d6-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'knowledgepool-supplier-author@domain.com', '26292aec-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'csl-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'csl-author@domain.com'), (SELECT id FROM role WHERE name = 'CSL_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'organisation-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'organisation-author@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_AUTHOR')),
((SELECT id FROM identity WHERE email = 'organisation-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'profession-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'profession-author@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_AUTHOR')),
((SELECT id FROM identity WHERE email = 'profession-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'kpmg-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'KPMG_SUPPLIER_AUTHOR')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'kornferry-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'KORNFERRY_SUPPLIER_AUTHOR')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'knowledgepool-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'knowledgepool-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'KNOWLEDGEPOOL_SUPPLIER_AUTHOR')),
((SELECT id FROM identity WHERE email = 'knowledgepool-supplier-author@domain.com'), (SELECT id FROM role WHERE name = 'LEARNING_MANAGER'))
;
