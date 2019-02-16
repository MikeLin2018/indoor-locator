/*
 Navicat Premium Data Transfer

 Source Server         : indoor_locator
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : localhost
 Source Database       : indoor_locator_db

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : utf-8

 Date: 02/16/2019 00:14:05 AM
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `ap_data`
-- ----------------------------
DROP TABLE IF EXISTS `ap_data`;
CREATE TABLE `ap_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `BSSID` varchar(11) DEFAULT NULL,
  `SSID` varchar(11) DEFAULT NULL,
  `quality` int(11) DEFAULT NULL,
  `scan_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `scan_id` (`scan_id`),
  CONSTRAINT `ap_data_ibfk_1` FOREIGN KEY (`scan_id`) REFERENCES `scan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `ap_data`
-- ----------------------------
BEGIN;
INSERT INTO `ap_data` VALUES ('22', '12345678', 'AP Name1', '99', '13'), ('23', '87654321', 'AP Name2', '11', '13');
COMMIT;

-- ----------------------------
--  Table structure for `building`
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `longitude` decimal(20,15) DEFAULT NULL,
  `latitude` decimal(20,15) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `trained_model` blob,
  `training_status` varchar(255) DEFAULT NULL,
  `training_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `building_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `building`
-- ----------------------------
BEGIN;
INSERT INTO `building` VALUES ('18', 'The Doric on Lane', '40.006519984374460', '-83.013460864702130', '11', null, 'Not Trained', null), ('20', 'Caldwell Lab', '40.002400000000000', '-83.015000000000000', '11', null, 'Not Trained', null);
COMMIT;

-- ----------------------------
--  Table structure for `normalization`
-- ----------------------------
DROP TABLE IF EXISTS `normalization`;
CREATE TABLE `normalization` (
  `user_id` int(11) NOT NULL,
  `building_id` int(11) NOT NULL,
  `normalization_factor` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`building_id`),
  KEY `building_id` (`building_id`),
  CONSTRAINT `normalization_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `normalization_ibfk_2` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `room`
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `floor` int(11) DEFAULT NULL,
  `building_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `building_id` (`building_id`),
  KEY `user_id` (`user_id`),
  KEY `building_id_2` (`building_id`),
  KEY `user_id_2` (`user_id`),
  CONSTRAINT `room_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `room_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `room`
-- ----------------------------
BEGIN;
INSERT INTO `room` VALUES ('10', 'Room 101', '1', '18', '11'), ('11', 'Room 101', '2', '18', '11');
COMMIT;

-- ----------------------------
--  Table structure for `scan`
-- ----------------------------
DROP TABLE IF EXISTS `scan`;
CREATE TABLE `scan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `add_time` datetime DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `room_id` int(11) DEFAULT NULL,
  `building_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `room_id` (`room_id`),
  KEY `building_id` (`building_id`),
  CONSTRAINT `scan_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `scan_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `scan_ibfk_3` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `scan`
-- ----------------------------
BEGIN;
INSERT INTO `scan` VALUES ('13', '2019-02-16 00:01:57', '11', '10', '18');
COMMIT;

-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO `user` VALUES ('11', 'userName', '$6$rounds=656000$3yQvUvBVE/vM8D5X$cdlT3OUOTCIsuI7J19N62CF4aG57sVdkU8VgHBA1jxOgxreq7VNuc0G4zI37UuGQrEdQXumcOkFDu2YanRBym0', 'lin.2453@osu.edu');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
