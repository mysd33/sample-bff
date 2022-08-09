/* 従業員テーブル（第３章で作成） */
CREATE TABLE IF NOT EXISTS employee (
    employee_id INT PRIMARY KEY,
    employee_name VARCHAR(50),
    age INT
);

/* ユーザーマスタ */
CREATE TABLE IF NOT EXISTS m_user (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100),
    user_name VARCHAR(50),
    birthday DATE,
    age INT,
    marriage BOOLEAN,
    role VARCHAR(50)
);