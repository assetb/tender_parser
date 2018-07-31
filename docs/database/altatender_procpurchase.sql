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
-- Table structure for table `procpurchase`
--

DROP TABLE IF EXISTS `procpurchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `procpurchase` (
  `id` int(11) NOT NULL,
  `source` int(11) NOT NULL,
  `number` varchar(45) DEFAULT NULL,
  `runame` mediumtext,
  `customer` mediumtext,
  `venue` int(11) DEFAULT NULL,
  `method` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `publishday` varchar(254) DEFAULT NULL,
  `startday` varchar(254) DEFAULT NULL,
  `endday` varchar(254) DEFAULT NULL,
  `attribute` varchar(254) DEFAULT NULL,
  `pricesuggestion` varchar(254) DEFAULT NULL,
  `type` varchar(45) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `istatus` int(11) DEFAULT NULL,
  `kzname` mediumtext,
  `dendday` date DEFAULT NULL,
  `timeinserted` timestamp(6) NULL DEFAULT NULL,
  `dstartday` date DEFAULT NULL,
  `loadday` datetime DEFAULT NULL,
  `dpublishday` date DEFAULT NULL,
  `isdocs` int(11) DEFAULT NULL,
  `docszip` mediumtext,
  `isum` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `startday_idx` (`startday`),
  KEY `number_idx` (`number`),
  KEY `endday_idx` (`endday`),
  KEY `method_idx` (`method`),
  KEY `venue_idx` (`venue`),
  KEY `source_idx` (`source`),
  KEY `istatus_idx` (`istatus`),
  KEY `dendday_idx` (`dendday`),
  KEY `dstartday_idx` (`dstartday`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-12 21:18:56
