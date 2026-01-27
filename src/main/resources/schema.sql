-- Editable schema used for first-run database creation.
CREATE TABLE "Meetings" (
    meeting_key INTEGER NOT NULL,
    country_name TEXT NOT NULL,
    country_code TEXT NOT NULL,
    location TEXT NOT NULL,
    meeting_name TEXT NOT NULL,
    "year" INTEGER NOT NULL,
    CONSTRAINT "pk_Meetings" PRIMARY KEY (meeting_key)
);

CREATE TABLE "Sessions" (
    session_key INTEGER NOT NULL,
    meeting_key INTEGER NOT NULL,
    session_name TEXT NOT NULL,
    session_type TEXT NOT NULL,
    CONSTRAINT "pk_Sessions" PRIMARY KEY (session_key),
    CONSTRAINT "fk_Sessions_pk_Meetings" FOREIGN KEY (meeting_key)
        REFERENCES "Meetings"(meeting_key)
);

CREATE TABLE "Drivers" (
    driver_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    team_name TEXT NOT NULL,
    CONSTRAINT "pk_Drivers" UNIQUE (driver_id)
);

CREATE TABLE "Driver_numbers" (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    driver_id INTEGER NOT NULL,
    start_number INTEGER NOT NULL,
    "year" INTEGER NOT NULL,
    meeting_key INTEGER,
    CONSTRAINT "pk_Driver_numbers" UNIQUE (id),
    CONSTRAINT "fk_Driver_numbers_pk_Drivers" FOREIGN KEY (driver_id)
        REFERENCES "Drivers"(driver_id)
);

CREATE TABLE "Laps" (
    driver_number INTEGER NOT NULL,
    lap_number INTEGER NOT NULL,
    session_key INTEGER NOT NULL,
    duration_sector_2 DOUBLE NOT NULL,
    duration_sector_3 DOUBLE NOT NULL,
    lap_duration DOUBLE NOT NULL,
    is_pit_out_lap INTEGER NOT NULL,
    duration_sector_1 DOUBLE NOT NULL,
    CONSTRAINT "pk_Laps" PRIMARY KEY (driver_number, lap_number, session_key),
    CONSTRAINT "fk_Laps_pk_Sessions" FOREIGN KEY (session_key)
        REFERENCES "Sessions"(session_key)
);

CREATE TABLE "Weather" (
    session_key INTEGER NOT NULL,
    meeting_key INTEGER NOT NULL,
    avg_air_temperature DOUBLE NOT NULL,
    avg_humidity DOUBLE NOT NULL,
    is_rainfall INTEGER NOT NULL,
    avg_track_temperature DOUBLE NOT NULL,
    avg_wind_direction DOUBLE NOT NULL,
    avg_wind_speed DOUBLE NOT NULL,
    CONSTRAINT "pk_WEATHER" PRIMARY KEY (session_key, meeting_key),
    CONSTRAINT "fk_WEATHER_pk_Sessions" FOREIGN KEY (session_key)
        REFERENCES "Sessions"(session_key),
    CONSTRAINT "fk_WEATHER_pk_Meetings" FOREIGN KEY (meeting_key)
        REFERENCES "Meetings"(meeting_key)
);
