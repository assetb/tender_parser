-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: 10.1.2.11    Database: com.altaik.db.altatender
-- ------------------------------------------------------
-- Server version	5.6.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `lots`
--

DROP TABLE IF EXISTS `lots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lots` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `source` varchar(11) DEFAULT NULL,
  `negnumber` varchar(45) DEFAULT NULL,
  `number` varchar(45) DEFAULT NULL,
  `kzname` mediumtext,
  `runame` mediumtext,
  `rudescription` mediumtext,
  `kzdescription` mediumtext,
  `quantity` varchar(255) DEFAULT NULL,
  `price` varchar(255) DEFAULT NULL,
  `sum` varchar(255) DEFAULT NULL,
  `deliveryplace` mediumtext,
  `deliveryschedule` mediumtext,
  `deliveryterms` mediumtext,
  `ktru` varchar(45) DEFAULT NULL,
  `unit` varchar(45) DEFAULT NULL,
  `kind` varchar(45) DEFAULT NULL,
  `kato` varchar(45) DEFAULT NULL,
  `lotscol` varchar(45) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `isum` decimal(15,2) DEFAULT NULL,
  `purchaseid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `negnumber_idx` (`negnumber`),
  KEY `sum_idx` (`sum`)
) ENGINE=InnoDB AUTO_INCREMENT=1019121 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-12 21:18:48
