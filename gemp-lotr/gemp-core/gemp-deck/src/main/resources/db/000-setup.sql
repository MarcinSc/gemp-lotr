CREATE TABLE `deck`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `player`        varchar(30) NOT NULL,
    `name`          varchar(100)  NOT NULL,
    `target_format` varchar(50)   NOT NULL DEFAULT 'Anything Goes',
    `contents`      text          NOT NULL,
    `notes`         varchar(5000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `player_deck` (`player`,`name`),
    KEY             `player` (`player`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
