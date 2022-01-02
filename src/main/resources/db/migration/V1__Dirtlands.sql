CREATE TABLE users (
                       UserID              INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       UserUUID            VARCHAR(36)   NOT NULL        UNIQUE
);

CREATE TABLE economy (
                       UserID              INTEGER       UNIQUE,
                       Balance             INTEGER       NOT NULL        DEFAULT 0
);

CREATE TABLE shopkeepers (
                       ShopkeeperID        INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       InventoryBase64     TEXT          NOT NULL
);


