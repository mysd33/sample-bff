/* 従業員テーブルのデータ（第３章で作成） */
INSERT INTO employee (employee_id, employee_name, age)
VALUES(1, '山田太郎', 30);

/* ユーザーマスタのデータ（ADMIN権限） */
INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('yamada@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '山田太郎', '1990-01-01', 28, false, 'ROLE_ADMIN');

/* ユーザーマスタのデータ（一般権限） */
INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村', '1986-11-05', 31, false, 'ROLE_GENERAL');

/* ページネーション用ダミーデータ */
INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura2@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村2', '1986-11-05', 31, false, 'ROLE_GENERAL');


INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura3@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村3', '1986-11-05', 31, false, 'ROLE_GENERAL');


INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura4@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村4', '1986-11-05', 31, false, 'ROLE_GENERAL');

INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura5@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村5', '1986-11-05', 31, false, 'ROLE_GENERAL');


INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura6@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村6', '1986-11-05', 31, false, 'ROLE_GENERAL');


INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura7@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村7', '1986-11-05', 31, false, 'ROLE_GENERAL');

INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura8@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村8', '1986-11-05', 31, false, 'ROLE_GENERAL');

INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura9@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村9', '1986-11-05', 31, false, 'ROLE_GENERAL');

INSERT INTO m_user (user_id, password, user_name, birthday, age, marriage, role)
VALUES('tamura10@xxx.co.jp', '$2a$10$xRTXvpMWly0oGiu65WZlm.3YL95LGVV2ASFjDhe6WF4.Qji1huIPa', '田村10', '1986-11-05', 31, false, 'ROLE_GENERAL');



