CREATE TABLE IF NOT EXISTS interactions
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    rating   FLOAT  NOT NULL,
    ts       TIMESTAMP WITHOUT TIME ZONE,
    UNIQUE (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS similarities
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id1     BIGINT NOT NULL,
    event_id2     BIGINT NOT NULL,
    similarity FLOAT  NOT NULL,
    ts         TIMESTAMP WITHOUT TIME ZONE,
    UNIQUE (event_id1, event_id2)
);
