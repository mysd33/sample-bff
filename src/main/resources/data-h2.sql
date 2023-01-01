/* ユーザーマスタのデータ（ADMIN権限） */
INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'yamada@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '山田太郎', '1990-01-01', 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'yamada@xxx.co.jp');

/* ユーザーマスタのデータ（一般権限） */
INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura@xxx.co.jp');

/* ページネーション用ダミーデータ */
INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura2@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村2', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura2@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura3@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村3', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura3@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura4@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村4', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura4@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura5@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村5', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura5@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura6@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村6', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura6@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura7@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村7', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura7@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura8@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村8', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura8@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura9@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村9', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura9@xxx.co.jp');

INSERT INTO m_user (user_id, password, user_name, birthday, role)
SELECT 'tamura10@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村10', '1986-11-05', 'ROLE_GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM m_user WHERE user_id = 'tamura10@xxx.co.jp');
