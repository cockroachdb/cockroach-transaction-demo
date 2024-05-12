-- drop tables
DROP TABLE IF EXISTS items;

-- re-create tables
CREATE TABLE items (item_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
                    name STRING,
                    description STRING,
                    price DECIMAL NOT NULL,
                    quantity INT DEFAULT 0);

INSERT INTO items (name, description, quantity, price) VALUES ('foo', 'fang', 200, 0.0);
