CREATE TABLE IF NOT EXISTS bank (
    UserID              INTEGER       NOT NULL        REFERENCES users(UserID),
    InventoryBase64     TEXT          NOT NULL
);