INSERT INTO `identity` (active, locked, email, uid, password) VALUES
(true, false, 'organisation-reporter-2@domain.com', 'fae13830-800c-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'profession-reporter-2@domain.com', 'ff71013c-800c-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'supplier-reporter@domain.com', '04bdcf4e-800d-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'kpmg-supplier-reporter@domain.com', '08d111fe-800d-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36'),
(true, false, 'kornferry-supplier-reporter@domain.com', '13486d98-800e-11eb-b34b-33dfc641f403', '$2a$10$sGfnyPnJ8a0b9R.vqIphKu5vjetS3.Bvi6ISv39bOphq5On0U2m36')
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'organisation-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'organisation-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'organisation-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_REPORTER')),
((SELECT id FROM identity WHERE email = 'organisation-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'profession-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'profession-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'profession-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_REPORTER')),
((SELECT id FROM identity WHERE email = 'profession-reporter-2@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'kpmg-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'KPMG_SUPPLIER_REPORTER')),
((SELECT id FROM identity WHERE email = 'kpmg-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'KPMG_SUPPLIER_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'kornferry-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'KORNFERRY_SUPPLIER_REPORTER')),
((SELECT id FROM identity WHERE email = 'kornferry-supplier-reporter@domain.com'), (SELECT id FROM role WHERE name = 'KORNFERRY_SUPPLIER_AUTHOR'))
;
