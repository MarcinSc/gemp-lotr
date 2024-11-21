CREATE TABLE `transfer`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `notify`        int(11) NOT NULL,
    `player`        varchar(45)    NOT NULL,
    `reason`        varchar(255)   NOT NULL,
    `name`          varchar(255)   NOT NULL,
    `collection`    text           NOT NULL,
    `transfer_date` decimal(20, 0) NOT NULL,
    `direction`     varchar(45)    NOT NULL,
    PRIMARY KEY (`id`),
    KEY             `player` (`player`,`notify`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
