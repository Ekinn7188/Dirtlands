CREATE TABLE IF NOT EXISTS users (
                       UserID              INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       UserUUID            VARCHAR(36)   NOT NULL        UNIQUE
);

CREATE TABLE IF NOT EXISTS shopkeepers (
                       ShopkeeperID        INTEGER       PRIMARY KEY     AUTOINCREMENT,
                       InventoryBase64     TEXT          NOT NULL
);


