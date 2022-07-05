CREATE TABLE users (
    user_id UUID NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    email VARCHAR(512) NOT NULL,
    salt VARCHAR(512) NOT NULL,
    PRIMARY KEY(user_id),
    UNIQUE(email),
    UNIQUE(salt)
);

CREATE TABLE intervals (
    interval_id UUID NOT NULL,
    user_id UUID NOT NULL,
    number_days INT NOT NULL,
    PRIMARY KEY(interval_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(user_id, number_days)
);

CREATE TABLE words (
    user_id UUID NOT NULL,
    word_id UUID NOT NULL,
    word_value VARCHAR(64) NOT NULL,
    note VARCHAR(512),
    interval_id UUID NOT NULL,
    last_date_of_repeat DATE NOT NULL,
    PRIMARY KEY(word_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(interval_id) REFERENCES intervals(interval_id) ON DELETE NO ACTION ON UPDATE CASCADE,
    UNIQUE(user_id, word_value)
);

CREATE TABLE words_interpretations (
    word_id UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    UNIQUE(word_id, value)
);

CREATE TABLE words_transcriptions (
    word_id UUID NOT NULL,
    value VARCHAR(128) NOT NULL,
    note VARCHAR(128),
    UNIQUE(word_id, value),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE words_translations (
    word_id UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    UNIQUE(word_id, value),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE words_examples (
    word_id UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    UNIQUE(word_id, origin),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE expressions (
    user_id UUID NOT NULL,
    expression_id UUID NOT NULL,
    expression_value VARCHAR(512) NOT NULL,
    note VARCHAR(256) NOT NULL,
    interval_id UUID NOT NULL,
    last_date_of_repeat DATE NOT NULL,
    PRIMARY KEY(expression_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(interval_id) REFERENCES intervals(interval_id) ON DELETE NO ACTION ON UPDATE CASCADE,
    UNIQUE(user_id, expression_value)
);

CREATE TABLE expressions_interpretations (
    expression_id UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    UNIQUE(expression_id, value),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);

CREATE TABLE expressions_translations (
    expression_id UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    UNIQUE(expression_id, value),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);

CREATE TABLE expressions_examples (
    expression_id UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    UNIQUE(origin, translate),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);
