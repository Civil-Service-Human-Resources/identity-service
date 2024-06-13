INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'course-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'identity-manager@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'manage-po@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'organisation-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'organisation-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'organisation-reporter@domain.com'), (SELECT id FROM role WHERE name = 'ORGANISATION_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'profession-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'profession-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'profession-reporter@domain.com'), (SELECT id FROM role WHERE name = 'PROFESSION_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'cshr-reporter@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'cshr-reporter@domain.com'), (SELECT id FROM role WHERE name = 'DOWNLOAD_BOOKING_FEED')),
((SELECT id FROM identity WHERE email = 'cshr-reporter@domain.com'), (SELECT id FROM role WHERE name = 'CSL_AUTHOR'))
;

INSERT INTO `identity_role` (identity_id, role_id) VALUES
((SELECT id FROM identity WHERE email = 'booking-feed@domain.com'), (SELECT id FROM role WHERE name = 'LEARNER')),
((SELECT id FROM identity WHERE email = 'booking-feed@domain.com'), (SELECT id FROM role WHERE name = 'CSHR_REPORTER'))
;
