CREATE TABLE users (
                       UserID         INTEGER      PRIMARY KEY                 AUTOINCREMENT,
                       UserUUID       VARCHAR(36)  NOT NULL                    UNIQUE,
                       UserNickname   VARCHAR(100)                             DEFAULT NULL,
                       ChatColor      VARCHAR(50)                              DEFAULT NULL
);

CREATE TABLE homes (
                       UserID         INTEGER      NOT NULL,
                       HomeLocation   VARCHAR(255) NOT NULL,
                       HomeName       VARCHAR(10)  NOT NULL
);

CREATE TABLE warps (
                       WarpID         INTEGER      PRIMARY KEY                 AUTOINCREMENT,
                       WarpName       VARCHAR(50)  NOT NULL                    UNIQUE,
                       WarpLocation   VARCHAR(255) NOT NULL,
                       WarpPermission VARCHAR(50)                              DEFAULT NULL
);

CREATE TABLE economy (
                       UserID         INTEGER      UNIQUE,
                       Balance        INTEGER      NOT NULL                    DEFAULT 0
);