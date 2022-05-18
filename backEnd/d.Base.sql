SHOW DATABASES;
CREATE DATABASE `company`;
SHOW DATABASES;
USE company;

CREATE TABLE `customer` (
                            `id` varchar(30) NOT NULL,
                            `name` varchar(40) DEFAULT NULL,
                            `address` varchar(100) DEFAULT NULL,
                            `salary` int DEFAULT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
                        `code` varchar(255) NOT NULL,
                        `description` varchar(255) DEFAULT NULL,
                        `qtyOnHand` int DEFAULT NULL,
                        `unitPrice` decimal(10,2) DEFAULT NULL,
                        PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
                          `oid` varchar(255) NOT NULL,
                          `date` date DEFAULT NULL,
                          `customerID` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`oid`),
                          KEY `customerID` (`customerID`),
                          CONSTRAINT `Orders_ibfk_1` FOREIGN KEY (`customerID`) REFERENCES `customer` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `orderdetails`;
CREATE TABLE `orderdetails` (
                                `oid` varchar(255) NOT NULL,
                                `itemCode` varchar(255) NOT NULL,
                                `qty` int DEFAULT NULL,
                                `unitPrice` decimal(10,2) DEFAULT NULL,
                                PRIMARY KEY (`oid`,`itemCode`),
                                KEY `itemCode` (`itemCode`),
                                CONSTRAINT `OrderDetails_ibfk_1` FOREIGN KEY (`oid`) REFERENCES `orders` (`oid`) ON DELETE CASCADE ON UPDATE CASCADE,
                                CONSTRAINT `OrderDetails_ibfk_2` FOREIGN KEY (`itemCode`) REFERENCES `item` (`code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

SHOW DATABASES;
SHOW TABLES;