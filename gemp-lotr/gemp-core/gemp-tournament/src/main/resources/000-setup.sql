CREATE TABLE `tournament`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `tournament_id` varchar(255)  NOT NULL,
    `name`          varchar(255)           DEFAULT NULL,
    `start_date`    datetime      NOT NULL DEFAULT current_timestamp(),
    `type`          varchar(45)   NOT NULL DEFAULT 'CONSTRUCTED',
    `parameters`    varchar(5000) NOT NULL DEFAULT '{}',
    `stage`         varchar(45)            DEFAULT NULL,
    `round`         int(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UQ_tournament_id` (`tournament_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `tournament_match`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `tournament_id` varchar(255) NOT NULL,
    `round`         int(11) NOT NULL DEFAULT 0,
    `player_one`    varchar(45)  NOT NULL,
    `player_two`    varchar(45)  NOT NULL,
    `winner`        varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `tournament_player`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `tournament_id` varchar(255) NOT NULL,
    `player`        varchar(30) DEFAULT NULL,
    `dropped`       binary(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `tournament_deck`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `tournament_id` varchar(255)  NOT NULL,
    `player`        varchar(30)            DEFAULT NULL,
    `type`          varchar(30)            DEFAULT NULL,
    `name`          varchar(100)  NOT NULL,
    `target_format` varchar(50)   NOT NULL DEFAULT 'Anything Goes',
    `contents`      text          NOT NULL,
    `notes`         varchar(5000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `player_tournament_deck` (`tournament_id`,`player`,`type`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
