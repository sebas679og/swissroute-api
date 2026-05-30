CREATE TABLE users
(
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    id         UUID                     NOT NULL,
    base_city  VARCHAR(255),
    email      VARCHAR(255)             NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    password   VARCHAR(255)             NOT NULL,

    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email)
);

CREATE TABLE favorite_routes
(
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    id             UUID                     NOT NULL,
    user_id        UUID                     NOT NULL,
    destination    VARCHAR(255)             NOT NULL,
    name           VARCHAR(255)             NOT NULL,
    origin         VARCHAR(255)             NOT NULL,
    transport_type VARCHAR(255),

    CONSTRAINT favorite_routes_pkey PRIMARY KEY (id),
    CONSTRAINT uk_favorite_routes_user_name UNIQUE (user_id, name),
    CONSTRAINT favorite_routes_transport_type_check
        CHECK (
            transport_type IN (
                               'TRAIN',
                               'TRAM',
                               'SHIP',
                               'BUS',
                               'CABLEWAY'
                )
            )
);

CREATE TABLE favorite_stations
(
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    id                  UUID                     NOT NULL,
    user_id             UUID                     NOT NULL,
    external_station_id VARCHAR(255)             NOT NULL,
    station_name        VARCHAR(255)             NOT NULL,

    CONSTRAINT favorite_stations_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_external_station UNIQUE (user_id, external_station_id)
);

CREATE TABLE search_history
(
    result_count INTEGER,
    searched_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    id           UUID                     NOT NULL,
    user_id      UUID                     NOT NULL,
    destination  VARCHAR(255)             NOT NULL,
    origin       VARCHAR(255)             NOT NULL,

    CONSTRAINT search_history_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_user_route_created
    ON favorite_routes (user_id, name, created_at DESC);

CREATE INDEX idx_user_station_created
    ON favorite_stations (user_id, external_station_id, created_at DESC);

CREATE INDEX idx_history_user_searchedat
    ON search_history (user_id, searched_at DESC);

ALTER TABLE favorite_routes
    ADD CONSTRAINT fk_favorite_routes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE favorite_stations
    ADD CONSTRAINT fk_favorite_stations_user
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE search_history
    ADD CONSTRAINT fk_search_history_user
        FOREIGN KEY (user_id)
            REFERENCES users (id);