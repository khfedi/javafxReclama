-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : jeu. 08 mai 2025 à 14:33
-- Version du serveur : 9.1.0
-- Version de PHP : 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `gestion_events`
--

-- --------------------------------------------------------

--
-- Structure de la table `reclamations`
--

DROP TABLE IF EXISTS `reclamations`;
CREATE TABLE IF NOT EXISTS `reclamations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `reclamations`
--

INSERT INTO `reclamations` (`id`, `email`, `description`, `date`) VALUES
(12, 'emj', 'ojg', '2025-05-06'),
(15, 'az', 'ilfjoe', '2025-05-06'),
(19, 'fff', 'lvhsofjofhfoqk', '2025-05-07'),
(16, 'aaa', 'eogjpoej', '2025-05-06'),
(17, 'a', 'a', '2025-05-06'),
(18, 'aaa', 'ezihgeoripgh', '2025-05-06'),
(21, 'fedi@gmail.com', 'piehgàzehg$zeàghozàçi', '2025-05-07'),
(22, 'ali@gmail.com', 'eeeeeeeeeeeeeeeeeeliioeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeepoioh', '2025-05-07'),
(23, 'ali@gmail.com', 'zefezfezfzegzege', '2025-05-07'),
(24, 'gmekj@egi.gse', 'oqêfhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh', '2025-05-07'),
(25, 'k@gmail.com', 'dkzljfoifjoekjf^qekpfjojkdnviupdsvpuishvpohvu_ph', '2025-05-07'),
(26, 'benibrahimsamer@gmail.com', 'saokoeovrrt', '2025-05-07'),
(27, 'benibrahimsamer@gmail.com', 'saokoeovrrtijefe', '2025-05-07'),
(29, 'benibrahimsamer@gmail.com', 'zegùlkhgosdôgijzĝoiezjùglk', '2025-05-07'),
(30, 'benibrahimsamer@gmail.com', 'fmzejfozeiĵfze^lkjôij', '2025-05-07'),
(31, 'benibrahimsamer@gmail.com', 'zepoueupojjfoŝhfodsfhoi', '2025-05-07'),
(32, 'benibrahimsamer@gmail.com', 'egegmlgjezĝjelg,sglp,gpo$jzpg$pg$kzopgkezopg', '2025-05-07'),
(33, 'ben@gmail.com', 'isgdyuuuuuuuuuugdsgdgdggd', '2025-05-07'),
(34, 'fedi@gmail.com', 'ghfjkdsslqmpazoieueryrtyfgdskl', '2025-05-07'),
(35, 'ousama@gmail.com', 'prob avec ali bouain', '2025-05-08');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
