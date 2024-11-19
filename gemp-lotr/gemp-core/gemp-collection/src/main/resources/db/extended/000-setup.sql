CREATE TABLE `collection`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `player`     varchar(30) NOT NULL,
    `type`       varchar(45) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_collection_player_type` (`player`,`type`),
    KEY          `player_collection` (`player`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `collection_entries`
(
    `collection_id` int(11) NOT NULL,
    `quantity` int(2) DEFAULT 0,
    `product` varchar(50) NOT NULL,
    `source` varchar(100) NOT NULL,
    `created_date` datetime DEFAULT current_timestamp(),
    `modified_date` datetime DEFAULT NULL ON UPDATE current_timestamp(),
    PRIMARY KEY (`collection_id`,`product`),
    CONSTRAINT `collection_entries_ibfk_1` FOREIGN KEY (`collection_id`) REFERENCES `collection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
