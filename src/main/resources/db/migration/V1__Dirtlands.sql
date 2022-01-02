CREATE TABLE users (
                       UserID              INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       UserUUID            VARCHAR(36)   NOT NULL        UNIQUE,
                       UserNickname        VARCHAR(100)                  DEFAULT NULL,
                       ChatColor           VARCHAR(50)                   DEFAULT NULL
);

CREATE TABLE homes (
                       UserID              INTEGER       NOT NULL,
                       HomeLocation        VARCHAR(255)  NOT NULL,
                       HomeName            VARCHAR(10)   NOT NULL
);

CREATE TABLE warps (
                       WarpID              INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       WarpName            VARCHAR(50)   NOT NULL        UNIQUE,
                       WarpLocation        VARCHAR(255)  NOT NULL,
                       WarpPermission      VARCHAR(50)                   DEFAULT NULL
);

CREATE TABLE economy (
                       UserID              INTEGER       UNIQUE,
                       Balance             INTEGER       NOT NULL        DEFAULT 0
);

CREATE TABLE punishments (
                       UserID              INTEGER       NOT NULL, -- UserID of the person being punished
                       IPAddress           VARCHAR(50)                  DEFAULT NULL, -- IP address for ip bans
                       PunisherID          INTEGER,                -- UserID of the punisher, null if console
                       PunishmentType      VARCHAR(50)   NOT NULL, -- Ban, Mute, Kick, Warn
                       PunishmentReason    VARCHAR(255),           -- Reason for the punishment
                       PunishmentStart     DATETIME                      DEFAULT NULL, --null if warn
                       PunishmentEnd       DATETIME                      DEFAULT NULL  -- null if permanent ban/warn
);

CREATE TABLE shopkeepers (
                       ShopkeeperID        INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       InventoryBase64     TEXT          NOT NULL
);


