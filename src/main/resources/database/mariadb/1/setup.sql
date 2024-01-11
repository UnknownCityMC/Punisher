CREATE TABLE IF NOT EXISTS punishment
(
    player_uuid VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY,
    player_last_name VARCHAR(16) NOT NULL,
    punisher_uuid VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY,
    punisher_last_name VARCHAR(16) NOT NULL,
    punishment_type ENUM('BAN', 'MUTE', 'WARN', 'KICK') NOT NULL,
    punishment_infinite BOOL DEFAULT false NOT NULL,
    punishment_end_date DATETIME DEFAULT ('9999-12-31 23:59:59'),
    punishment_reason VARCHAR(255)
);