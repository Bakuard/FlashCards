CREATE TABLE Users (
    userId UUID NOT NULL,
    passwordHash VARCHAR(512) NOT NULL,
    email VARCHAR(512) NOT NULL,
    salt VARCHAR(512) NOT NULL,
    PRIMARY KEY(userId),
    UNIQUE(email),
    UNIQUE(salt)
);

CREATE TABLE RepeatData (
    repeatDataId UUID NOT NULL,
    userId UUID NOT NULL,
    numberDays INT NOT NULL,
    lastDateOfRepeat DATE NOT NULL,
    PRIMARY KEY(repeatDataId),
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(userId, numberDays)
);

CREATE TABLE Words (
    userId UUID NOT NULL,
    wordId UUID NOT NULL,
    wordValue VARCHAR(64) NOT NULL,
    note VARCHAR(512),
    repeatDataId UUID NOT NULL,
    PRIMARY KEY(wordId),
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(repeatDataId) REFERENCES RepeatData(repeatDataId) ON DELETE NO ACTION ON UPDATE CASCADE,
    UNIQUE(userId, wordValue)
);

CREATE TABLE WordsInterpretations (
    wordId UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    FOREIGN KEY (wordId) REFERENCES Words(wordId) ON DELETE CASCADE,
    UNIQUE(wordId, value)
);

CREATE TABLE WordsTranscriptions (
    wordId UUID NOT NULL,
    value VARCHAR(128) NOT NULL,
    note VARCHAR(128),
    UNIQUE(wordId, value),
    FOREIGN KEY (wordId) REFERENCES Words(wordId) ON DELETE CASCADE
);

CREATE TABLE WordsTranslations (
    wordId UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    UNIQUE(wordId, value),
    FOREIGN KEY (wordId) REFERENCES Words(wordId) ON DELETE CASCADE
);

CREATE TABLE WordsExamples (
    wordId UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    UNIQUE(wordId, origin),
    FOREIGN KEY (wordId) REFERENCES Words(wordId) ON DELETE CASCADE
);

CREATE TABLE Expressions (
    userId UUID NOT NULL,
    expressionId UUID NOT NULL,
    expressionValue VARCHAR(512) NOT NULL,
    note VARCHAR(256) NOT NULL,
    repeatDataId UUID NOT NULL,
    PRIMARY KEY(expressionId),
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(repeatDataId) REFERENCES RepeatData(repeatDataId) ON DELETE NO ACTION ON UPDATE CASCADE,
    UNIQUE(userId, expressionValue)
);

CREATE TABLE ExpressionsInterpretations (
    expressionId UUID NOT NULL,
    value VARCHAR(512) NOT NULL,
    UNIQUE(expressionId, value),
    FOREIGN KEY(expressionId) REFERENCES Expressions(expressionId) ON DELETE CASCADE
);

CREATE TABLE ExpressionsTranslations (
    expressionId UUID NOT NULL,
    value VARCHAR(64) NOT NULL,
    note VARCHAR(128),
    UNIQUE(expressionId, value),
    FOREIGN KEY(expressionId) REFERENCES Expressions(expressionId) ON DELETE CASCADE
);

CREATE TABLE ExpressionsExamples (
    expressionId UUID NOT NULL,
    origin VARCHAR(512) NOT NULL,
    translate VARCHAR(512),
    note VARCHAR(128),
    UNIQUE(origin, translate),
    FOREIGN KEY(expressionId) REFERENCES Expressions(expressionId) ON DELETE CASCADE
);
