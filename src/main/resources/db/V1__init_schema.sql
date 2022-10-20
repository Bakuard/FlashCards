CREATE TABLE users (
    user_id UUID NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    email VARCHAR(512) NOT NULL,
    salt VARCHAR(512) NOT NULL,
    PRIMARY KEY(user_id),
    UNIQUE(email),
    UNIQUE(salt)
);

CREATE TABLE roles (
    user_id UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    index INT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(user_id, name)
);

------------------------------------------------------------------------------------------

CREATE TABLE intervals (
    user_id UUID NOT NULL,
    number_days INT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(user_id, number_days)
);

CREATE TABLE words (
    user_id UUID NOT NULL,
    word_id UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(512),
    repeat_interval_from_english INT NOT NULL,
    last_date_of_repeat_from_english DATE NOT NULL,
    repeat_interval_from_native INT NOT NULL,
    last_date_of_repeat_from_native DATE NOT NULL,
    PRIMARY KEY(word_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(user_id, value)
);

CREATE TABLE words_interpretations (
    word_id UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    index INT NOT NULL,
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    UNIQUE(word_id, value)
);

CREATE TABLE words_transcriptions (
    word_id UUID NOT NULL,
    value VARCHAR(128) NOT NULL,
    note VARCHAR(128),
    index INT NOT NULL,
    UNIQUE(word_id, value),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE words_translations (
    word_id UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    index INT NOT NULL,
    UNIQUE(word_id, value),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE words_examples (
    word_id UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    index INT NOT NULL,
    UNIQUE(word_id, origin),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE
);

CREATE TABLE repeat_words_from_english_statistic (
    repeat_id UUID NOT NULL,
    user_id UUID NOT NULL,
    word_id UUID NOT NULL,
    repetition_date DATE NOT NULL,
    repetition_interval INT NOT NULL,
    is_remember BOOLEAN NOT NULL,
    PRIMARY KEY(repeat_id),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE repeat_words_from_native_statistic (
    repeat_id UUID NOT NULL,
    word_id UUID NOT NULL,
    user_id UUID NOT NULL,
    repetition_date DATE NOT NULL,
    repetition_interval INT NOT NULL,
    is_remember BOOLEAN NOT NULL,
    PRIMARY KEY(repeat_id),
    FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE expressions (
    user_id UUID NOT NULL,
    expression_id UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    note VARCHAR(256) NOT NULL,
    repeat_interval_from_english INT NOT NULL,
    last_date_of_repeat_from_english DATE NOT NULL,
    repeat_interval_from_native INT NOT NULL,
    last_date_of_repeat_from_native DATE NOT NULL,
    PRIMARY KEY(expression_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(user_id, value)
);

CREATE TABLE expressions_interpretations (
    expression_id UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    index INT NOT NULL,
    UNIQUE(expression_id, value),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);

CREATE TABLE expressions_translations (
    expression_id UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    index INT NOT NULL,
    UNIQUE(expression_id, value),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);

CREATE TABLE expressions_examples (
    expression_id UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    index INT NOT NULL,
    UNIQUE(origin, translate),
    FOREIGN KEY(expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE
);

CREATE TABLE repeat_expressions_from_english_statistic (
    repeat_id UUID NOT NULL,
    user_id UUID NOT NULL,
    expression_id UUID NOT NULL,
    repetition_date DATE NOT NULL,
    repetition_interval INT NOT NULL,
    is_remember BOOLEAN NOT NULL,
    PRIMARY KEY(repeat_id),
    FOREIGN KEY (expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE repeat_expressions_from_native_statistic (
    repeat_id UUID NOT NULL,
    user_id UUID NOT NULL,
    expression_id UUID NOT NULL,
    repetition_date DATE NOT NULL,
    repetition_interval INT NOT NULL,
    is_remember BOOLEAN NOT NULL,
    PRIMARY KEY(repeat_id),
    FOREIGN KEY (expression_id) REFERENCES expressions(expression_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE ALIAS distance FOR 'com.bakuard.flashcards.dal.impl.StoredProcedures.levenshteinDistance';