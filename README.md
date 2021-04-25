# covid-warriors-2021

CREATE TABLE `category_message` (
  `category` varchar(100) DEFAULT NULL,
  `message` varchar(4000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contact` (
  `id` int NOT NULL,
  `mobile` varchar(12) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `pin_code` varchar(45) DEFAULT NULL,
  `category` varchar(45) DEFAULT NULL,
  `last_message_sent_time` timestamp NULL DEFAULT NULL,
  `last_message_received_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `response` (
  `id` int NOT NULL,
  `mobile` varchar(12) DEFAULT NULL,
  `response_message` varchar(4000) DEFAULT NULL,
  `response_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
