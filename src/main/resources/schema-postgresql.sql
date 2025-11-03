/* ユーザーマスタ */
CREATE TABLE IF NOT EXISTS m_user (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100),
    user_name VARCHAR(50),
    birthday DATE,
    role VARCHAR(50)
);

/* トランザクショントークン */
CREATE TABLE IF NOT EXISTS transaction_token (
    token_name varchar(256) not null,
    token_key varchar(32) not null,
    token_value varchar(32) not null,
    session_id  varchar(256) not null,
    sequence bigint,
    constraint pk_transaction_token primary key (token_name, token_key, session_id)
);

CREATE INDEX IF NOT EXISTS transaction_token_index_delete_older on transaction_token(token_name, session_id);
CREATE INDEX IF NOT EXISTS transaction_token_index_delete_older_sequence on transaction_token(sequence);
CREATE INDEX IF NOT EXISTS transaction_token_index_clean on transaction_token(session_id);

CREATE SEQUENCE IF NOT EXISTS transaction_token_sequence;


/* ユーザーの一時テーブル */
CREATE TABLE IF NOT EXISTS m_user_temp (
    user_id VARCHAR(50) PRIMARY KEY,
    user_name VARCHAR(50),
    age INT
);