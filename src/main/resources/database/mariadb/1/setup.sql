CREATE TABLE IF NOT EXISTS punishment
(
    punishment_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    player_uuid VARCHAR(36) DEFAULT (UUID()),
    player_last_name VARCHAR(16) NOT NULL,
    punisher_uuid VARCHAR(36) DEFAULT (UUID()),
    punisher_last_name VARCHAR(16) NOT NULL,
    active BOOL DEFAULT true NOT NULL,
    punishment_type ENUM('BAN', 'MUTE', 'WARN', 'KICK') NOT NULL,
    punishment_infinite BOOL DEFAULT false NOT NULL,
    punishment_duration INT DEFAULT (0),
    punishment_date DATETIME DEFAULT (CURRENT_DATE + ' ' + CURRENT_TIME),
    punishment_reason VARCHAR(255)
);