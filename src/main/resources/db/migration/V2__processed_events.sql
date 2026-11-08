CREATE TABLE processed_events (
    event_id     VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
