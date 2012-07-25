-- phpMyAdmin SQL Dump
-- version 3.3.2deb1ubuntu1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jul 26, 2012 at 02:55 AM
-- Server version: 5.1.63
-- PHP Version: 5.3.2-1ubuntu4.15

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `sfs_xactions`
--
CREATE DATABASE `sfs_xactions` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `sfs_xactions`;

-- --------------------------------------------------------

--
-- Table structure for table `sfs_txlog`
--

CREATE TABLE IF NOT EXISTS `sfs_txlog` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `method` varchar(10) NOT NULL,
  `path` varchar(255) NOT NULL,
  `sfsop` blob NOT NULL,
  `timestamp` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `method_2` (`method`,`path`,`timestamp`),
  KEY `method` (`method`),
  KEY `path` (`path`),
  KEY `timestamp` (`timestamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;
