CREATE TABLE IF NOT EXISTS punishment
(
    player_uuid VARCHAR(36) DEFAULT (UUID()),
    player_last_name VARCHAR(16) NOT NULL,
    punisher_uuid VARCHAR(36) DEFAULT (UUID()),
    punisher_last_name VARCHAR(16) NOT NULL,
    active BOOL DEFAULT true NOT NULL,
    punishment_type ENUM('TEMP_BAN', 'BAN', 'MUTE', 'WARN', 'KICK') NOT NULL,
    punishment_infinite BOOL DEFAULT false NOT NULL,
    punishment_end_date DATETIME DEFAULT ('9999-12-31 23:59:59'),
    punishment_date DATETIME DEFAULT (CURRENT_DATE + ' ' + CURRENT_TIME),
    punishment_reason VARCHAR(255)
);