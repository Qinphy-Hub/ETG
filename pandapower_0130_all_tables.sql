-- MySQL dump 10.13  Distrib 5.7.25, for Win64 (x86_64)
--
-- Host: localhost    Database: pandapower_0130
-- ------------------------------------------------------
-- Server version	5.7.25

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
-- Table structure for table `asymmetricload`
--

DROP TABLE IF EXISTS `asymmetricload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `asymmetricload` (
  `bus` int(11) NOT NULL,
  `p_a_mw` float NOT NULL COMMENT 'A相有功功率(MW)',
  `p_b_mw` float NOT NULL COMMENT 'B相有功功率(MW)',
  `p_c_mw` float NOT NULL COMMENT 'C相有功功率(MW)',
  `q_a_mvar` float NOT NULL COMMENT 'A相无功功率(MVar)',
  `q_b_mvar` float NOT NULL COMMENT 'B相无功功率(MVar)',
  `q_c_mvar` float NOT NULL COMMENT 'C相无功功率(MVar)',
  `sn_a_mva` float DEFAULT NULL COMMENT 'A相额定容量(MVA)',
  `sn_b_mva` float DEFAULT NULL COMMENT 'B相额定容量(MVA)',
  `sn_c_mva` float DEFAULT NULL COMMENT 'C相额定容量(MVA)',
  `sn_mva` float DEFAULT NULL COMMENT '总额定容量(MVA)',
  `name` varchar(100) DEFAULT NULL,
  `scaling` float DEFAULT '1',
  `type` enum('wye','delta') DEFAULT 'wye',
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asymmetricload`
--

LOCK TABLES `asymmetricload` WRITE;
/*!40000 ALTER TABLE `asymmetricload` DISABLE KEYS */;
/*!40000 ALTER TABLE `asymmetricload` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asymmetricstaticgenerator`
--

DROP TABLE IF EXISTS `asymmetricstaticgenerator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `asymmetricstaticgenerator` (
  `bus` int(11) NOT NULL,
  `p_a_mw` float NOT NULL COMMENT 'A相有功功率(MW)',
  `p_b_mw` float NOT NULL COMMENT 'B相有功功率(MW)',
  `p_c_mw` float NOT NULL COMMENT 'C相有功功率(MW)',
  `q_a_mvar` float NOT NULL COMMENT 'A相无功功率(MVar)',
  `q_b_mvar` float NOT NULL COMMENT 'B相无功功率(MVar)',
  `q_c_mvar` float NOT NULL COMMENT 'C相无功功率(MVar)',
  `sn_a_mva` float DEFAULT NULL COMMENT 'A相额定容量(MVA)',
  `sn_b_mva` float DEFAULT NULL COMMENT 'B相额定容量(MVA)',
  `sn_c_mva` float DEFAULT NULL COMMENT 'C相额定容量(MVA)',
  `sn_mva` float DEFAULT NULL COMMENT '总额定容量(MVA)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `scaling` float DEFAULT '1' COMMENT '三相缩放因子',
  `type` enum('wye','delta') DEFAULT 'wye' COMMENT '连接类型：星型/三角型',
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asymmetricstaticgenerator`
--

LOCK TABLES `asymmetricstaticgenerator` WRITE;
/*!40000 ALTER TABLE `asymmetricstaticgenerator` DISABLE KEYS */;
/*!40000 ALTER TABLE `asymmetricstaticgenerator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `backtobackvoltagesourceconverter`
--

DROP TABLE IF EXISTS `backtobackvoltagesourceconverter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `backtobackvoltagesourceconverter` (
  `bus` int(11) NOT NULL COMMENT '交流侧连接母线',
  `bus_dc_plus` int(11) NOT NULL COMMENT '直流正侧连接母线',
  `bus_dc_minus` int(11) NOT NULL COMMENT '直流负侧连接母线',
  `r_ohm` float NOT NULL COMMENT '耦合变压器电阻(Ω)',
  `x_ohm` float NOT NULL COMMENT '耦合变压器电抗(Ω)',
  `r_dc_ohm` float NOT NULL COMMENT '直流侧内阻(Ω)',
  `pl_dc_mw` float NOT NULL COMMENT '直流侧空载损耗(MW)',
  `control_mode_ac` varchar(10) NOT NULL COMMENT '交流侧控制模式：vm_pu/q_mvar/slack',
  `control_value_ac` float NOT NULL COMMENT '交流侧控制值',
  `control_mode_dc` varchar(10) NOT NULL COMMENT '直流侧控制模式：vm_pu/p_mw',
  `control_value_dc` float NOT NULL COMMENT '直流侧控制值',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控',
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `backtobackvoltagesourceconverter`
--

LOCK TABLES `backtobackvoltagesourceconverter` WRITE;
/*!40000 ALTER TABLE `backtobackvoltagesourceconverter` DISABLE KEYS */;
/*!40000 ALTER TABLE `backtobackvoltagesourceconverter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bus`
--

DROP TABLE IF EXISTS `bus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bus` (
  `vn_kv` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `geodata` point DEFAULT NULL,
  `type` enum('n','b','m') DEFAULT 'n',
  `zone` varchar(100) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `max_vm_pu` float DEFAULT NULL,
  `min_vm_pu` float DEFAULT NULL,
  `x` float DEFAULT NULL,
  `y` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bus`
--

LOCK TABLES `bus` WRITE;
/*!40000 ALTER TABLE `bus` DISABLE KEYS */;
/*!40000 ALTER TABLE `bus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `busdc`
--

DROP TABLE IF EXISTS `busdc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `busdc` (
  `vn_kv` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `geodata` point DEFAULT NULL,
  `type` enum('n','b','m') NOT NULL DEFAULT 'n',
  `zone` varchar(100) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `max_vm_pu` float DEFAULT NULL,
  `min_vm_pu` float DEFAULT NULL,
  `x` float DEFAULT NULL,
  `y` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `busdc`
--

LOCK TABLES `busdc` WRITE;
/*!40000 ALTER TABLE `busdc` DISABLE KEYS */;
/*!40000 ALTER TABLE `busdc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emptynetwork`
--

DROP TABLE IF EXISTS `emptynetwork`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `emptynetwork` (
  `f_hz` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `sn_mva` float DEFAULT NULL,
  `add_stdtypes` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emptynetwork`
--

LOCK TABLES `emptynetwork` WRITE;
/*!40000 ALTER TABLE `emptynetwork` DISABLE KEYS */;
/*!40000 ALTER TABLE `emptynetwork` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extendedward`
--

DROP TABLE IF EXISTS `extendedward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extendedward` (
  `bus` int(11) NOT NULL,
  `ps_mw` float NOT NULL COMMENT 'PQ部分有功功率(MW)',
  `qs_mvar` float NOT NULL COMMENT 'PQ部分无功功率(MVar)',
  `pz_mw` float NOT NULL COMMENT '阻抗部分有功功率(MW)，1.0pu时',
  `qz_mvar` float NOT NULL COMMENT '阻抗部分无功功率(MVar)，1.0pu时',
  `r_ohm` float DEFAULT NULL COMMENT '电压源内阻(Ω)',
  `x_ohm` float DEFAULT NULL COMMENT '电压源内抗(Ω)',
  `vm_pu` float DEFAULT '1',
  `slack_weight` float DEFAULT '1' COMMENT '平衡节点权重',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extendedward`
--

LOCK TABLES `extendedward` WRITE;
/*!40000 ALTER TABLE `extendedward` DISABLE KEYS */;
/*!40000 ALTER TABLE `extendedward` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `externalgrid`
--

DROP TABLE IF EXISTS `externalgrid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `externalgrid` (
  `bus` int(11) NOT NULL,
  `vm_pu` float NOT NULL DEFAULT '1' COMMENT '电压标幺值',
  `va_degree` float DEFAULT '0' COMMENT '电压角度(度)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `s_sc_max_mva` float DEFAULT NULL COMMENT '最大短路容量(MVA)',
  `s_sc_min_mva` float DEFAULT NULL COMMENT '最小短路容量(MVA)',
  `rx_max` float DEFAULT NULL COMMENT '最大R/X比',
  `rx_min` float DEFAULT NULL COMMENT '最小R/X比',
  `r0x0_max` float DEFAULT NULL COMMENT '最大零序R/X比',
  `x0x_max` float DEFAULT NULL COMMENT '最大零序X/X比',
  `max_p_mw` float DEFAULT NULL COMMENT '最大有功功率(MW)',
  `min_p_mw` float DEFAULT NULL COMMENT '最小有功功率(MW)',
  `max_q_mvar` float DEFAULT NULL COMMENT '最大无功功率(MVar)',
  `min_q_mvar` float DEFAULT NULL COMMENT '最小无功功率(MVar)',
  `slack_weight` float DEFAULT '1' COMMENT '平衡节点权重',
  `controllable` tinyint(1) DEFAULT NULL COMMENT '是否可控'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `externalgrid`
--

LOCK TABLES `externalgrid` WRITE;
/*!40000 ALTER TABLE `externalgrid` DISABLE KEYS */;
/*!40000 ALTER TABLE `externalgrid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `generator`
--

DROP TABLE IF EXISTS `generator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `generator` (
  `bus` int(11) NOT NULL,
  `p_mw` float NOT NULL COMMENT '有功功率(MW)',
  `vm_pu` float DEFAULT '1',
  `sn_mva` float DEFAULT NULL COMMENT '额定容量(MVA)',
  `vn_kv` float DEFAULT NULL COMMENT '额定电压(kV)',
  `cos_phi` float DEFAULT NULL COMMENT '额定功率因数',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `scaling` float DEFAULT '1' COMMENT '有功功率缩放因子',
  `type` varchar(50) DEFAULT NULL COMMENT '发电机类型',
  `slack` tinyint(1) DEFAULT '0',
  `controllable` tinyint(1) DEFAULT '1',
  `slack_weight` float DEFAULT '1' COMMENT '分布式平衡权重',
  `reactive_capability_curve` tinyint(1) DEFAULT '0',
  `id_q_capability_characteristic` int(11) DEFAULT NULL COMMENT '无功能力特性曲线ID',
  `curve_style` varchar(50) DEFAULT NULL COMMENT '曲线样式',
  `xdss_pu` float DEFAULT NULL COMMENT '次暂态电抗(标幺值)',
  `rdss_ohm` float DEFAULT NULL COMMENT '次暂态电阻(Ω)',
  `pg_percent` float DEFAULT NULL COMMENT '电压控制范围百分比(%)',
  `power_station_trafo` int(11) DEFAULT NULL COMMENT '电厂变压器索引',
  `in_service` tinyint(1) DEFAULT '1',
  `max_p_mw` float DEFAULT NULL COMMENT '最大有功功率(MW)',
  `min_p_mw` float DEFAULT NULL COMMENT '最小有功功率(MW)',
  `max_q_mvar` float DEFAULT NULL COMMENT '最大无功功率(MVar)',
  `min_q_mvar` float DEFAULT NULL COMMENT '最小无功功率(MVar)',
  `min_vm_pu` float DEFAULT NULL COMMENT '最小电压(标幺值)',
  `max_vm_pu` float DEFAULT NULL COMMENT '最大电压(标幺值)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `generator`
--

LOCK TABLES `generator` WRITE;
/*!40000 ALTER TABLE `generator` DISABLE KEYS */;
/*!40000 ALTER TABLE `generator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hvdclink`
--

DROP TABLE IF EXISTS `hvdclink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hvdclink` (
  `from_bus` int(11) NOT NULL COMMENT '整流侧母线',
  `to_bus` int(11) NOT NULL COMMENT '逆变侧母线',
  `p_mw` float NOT NULL COMMENT '传输有功功率(MW)，from→to为正',
  `loss_percent` float DEFAULT NULL COMMENT '相对损耗百分比(%)',
  `loss_mw` float DEFAULT NULL COMMENT '总损耗功率(MW)',
  `vm_from_pu` float DEFAULT '1',
  `vm_to_pu` float DEFAULT '1',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `max_p_mw` float DEFAULT NULL COMMENT '最大传输功率(MW)',
  `min_q_from_mvar` float DEFAULT NULL COMMENT '整流侧最小无功(MVar)',
  `min_q_to_mvar` float DEFAULT NULL COMMENT '逆变侧最小无功(MVar)',
  `max_q_from_mvar` float DEFAULT NULL COMMENT '整流侧最大无功(MVar)',
  `max_q_to_mvar` float DEFAULT NULL COMMENT '逆变侧最大无功(MVar)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hvdclink`
--

LOCK TABLES `hvdclink` WRITE;
/*!40000 ALTER TABLE `hvdclink` DISABLE KEYS */;
/*!40000 ALTER TABLE `hvdclink` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impedance`
--

DROP TABLE IF EXISTS `impedance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `impedance` (
  `from_bus` int(11) NOT NULL,
  `to_bus` int(11) NOT NULL,
  `rft_pu` float NOT NULL COMMENT '正序电阻(标幺值)，from→to',
  `xft_pu` float NOT NULL COMMENT '正序电抗(标幺值)，from→to',
  `rtf_pu` float DEFAULT NULL COMMENT '反向电阻(标幺值)，to→from',
  `xtf_pu` float DEFAULT NULL COMMENT '反向电抗(标幺值)，to→from',
  `sn_mva` float DEFAULT NULL COMMENT '额定容量(MVA)',
  `rft0_pu` float DEFAULT NULL COMMENT '零序电阻(标幺值)',
  `xft0_pu` float DEFAULT NULL COMMENT '零序电抗(标幺值)',
  `rtf0_pu` float DEFAULT NULL COMMENT '反向零序电阻',
  `xtf0_pu` float DEFAULT NULL COMMENT '反向零序电抗',
  `gf_pu` float DEFAULT '0' COMMENT 'from侧电导',
  `bf_pu` float DEFAULT '0' COMMENT 'from侧电纳',
  `gt_pu` float DEFAULT '0' COMMENT 'to侧电导',
  `bt_pu` float DEFAULT '0' COMMENT 'to侧电纳',
  `gf0_pu` float DEFAULT NULL COMMENT 'from侧零序电导',
  `bf0_pu` float DEFAULT NULL COMMENT 'from侧零序电纳',
  `gt0_pu` float DEFAULT NULL COMMENT 'to侧零序电导',
  `bt0_pu` float DEFAULT NULL COMMENT 'to侧零序电纳',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impedance`
--

LOCK TABLES `impedance` WRITE;
/*!40000 ALTER TABLE `impedance` DISABLE KEYS */;
/*!40000 ALTER TABLE `impedance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `line`
--

DROP TABLE IF EXISTS `line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `line` (
  `from_bus` int(11) NOT NULL COMMENT '起点总线',
  `to_bus` int(11) NOT NULL COMMENT '终点总线',
  `length_km` float NOT NULL COMMENT '长度(km)',
  `std_type` varchar(100) NOT NULL COMMENT '标准类型',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `index` int(11) DEFAULT NULL COMMENT '索引',
  `geodata` point DEFAULT NULL COMMENT '地理坐标',
  `in_service` tinyint(1) DEFAULT '1',
  `df` float DEFAULT '1' COMMENT '降额因子',
  `parallel` int(11) DEFAULT '1' COMMENT '并行数',
  `max_loading_percent` float DEFAULT NULL COMMENT '最大负载%',
  `alpha` float DEFAULT '0.00403' COMMENT '温度系数',
  `temperature_degree_celsius` float DEFAULT '20' COMMENT '温度℃'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `line`
--

LOCK TABLES `line` WRITE;
/*!40000 ALTER TABLE `line` DISABLE KEYS */;
/*!40000 ALTER TABLE `line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `linedc`
--

DROP TABLE IF EXISTS `linedc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `linedc` (
  `from_bus_dc` int(11) NOT NULL,
  `to_bus_dc` int(11) NOT NULL,
  `length_km` float NOT NULL,
  `std_type` varchar(100) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `geodata` point DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `df` float DEFAULT '1',
  `parallel` int(11) DEFAULT '1',
  `max_loading_percent` float DEFAULT NULL,
  `alpha` float DEFAULT '0.00403',
  `temperature_degree_celsius` float DEFAULT '20'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linedc`
--

LOCK TABLES `linedc` WRITE;
/*!40000 ALTER TABLE `linedc` DISABLE KEYS */;
/*!40000 ALTER TABLE `linedc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `load`
--

DROP TABLE IF EXISTS `load`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load` (
  `bus` int(11) NOT NULL,
  `p_mw` float NOT NULL,
  `q_mvar` float NOT NULL,
  `const_z_p_percent` float DEFAULT '0',
  `const_i_p_percent` float DEFAULT '0',
  `const_z_q_percent` float DEFAULT '0',
  `const_i_q_percent` float DEFAULT '0',
  `sn_mva` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `scaling` float DEFAULT '1',
  `type` enum('wye','delta') DEFAULT 'wye',
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `max_p_mw` float DEFAULT NULL,
  `min_p_mw` float DEFAULT NULL,
  `max_q_mvar` float DEFAULT NULL,
  `min_q_mvar` float DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `load`
--

LOCK TABLES `load` WRITE;
/*!40000 ALTER TABLE `load` DISABLE KEYS */;
/*!40000 ALTER TABLE `load` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loaddc`
--

DROP TABLE IF EXISTS `loaddc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loaddc` (
  `bus_dc` int(11) NOT NULL,
  `p_dc_mw` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `scaling` float DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loaddc`
--

LOCK TABLES `loaddc` WRITE;
/*!40000 ALTER TABLE `loaddc` DISABLE KEYS */;
/*!40000 ALTER TABLE `loaddc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `measurement`
--

DROP TABLE IF EXISTS `measurement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measurement` (
  `meas_type` enum('v','p','q','i','va','ia') NOT NULL COMMENT '测量类型：v=电压,p=有功,q=无功,i=电流,va=电压角度,ia=电流角度',
  `element_type` enum('bus','line','trafo','trafow3','load','gen','sgen','shunt','ward','xward','ext_grid') NOT NULL COMMENT '被测元件类型',
  `value` float NOT NULL COMMENT '测量值',
  `value_unit` enum('MW','MVAr','p.u.','kA','degree') NOT NULL COMMENT '单位',
  `std_dev` float DEFAULT NULL COMMENT '标准偏差',
  `element` int(11) NOT NULL COMMENT '被测元件ID',
  `side` varchar(10) DEFAULT NULL COMMENT '测量侧：from/to/hv/mv/lv等',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `check_existing` tinyint(1) DEFAULT '1' COMMENT '是否检查重复测量',
  `measurement_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测量时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `measurement`
--

LOCK TABLES `measurement` WRITE;
/*!40000 ALTER TABLE `measurement` DISABLE KEYS */;
/*!40000 ALTER TABLE `measurement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `motor`
--

DROP TABLE IF EXISTS `motor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `motor` (
  `bus` int(11) NOT NULL,
  `pn_mech_mw` float NOT NULL COMMENT '额定机械功率(MW)',
  `cos_phi` float NOT NULL COMMENT '当前运行点功率因数',
  `efficiency_percent` float NOT NULL COMMENT '当前效率百分比(%)',
  `loading_percent` float NOT NULL COMMENT '机械负载率(%)',
  `scaling` float DEFAULT '1' COMMENT '有功功率缩放因子',
  `cos_phi_n` float DEFAULT NULL COMMENT '额定功率因数',
  `efficiency_n_percent` float DEFAULT NULL COMMENT '额定效率百分比(%)',
  `lrc_pu` float DEFAULT NULL COMMENT '堵转电流标幺值',
  `rx` float DEFAULT NULL COMMENT 'R/X比（短路计算用）',
  `vn_kv` float DEFAULT NULL COMMENT '额定电压(kV)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `motor`
--

LOCK TABLES `motor` WRITE;
/*!40000 ALTER TABLE `motor` DISABLE KEYS */;
/*!40000 ALTER TABLE `motor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shunt`
--

DROP TABLE IF EXISTS `shunt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shunt` (
  `bus` int(11) NOT NULL,
  `p_mw` float NOT NULL COMMENT '有功功率(MW)，v=1.0pu时',
  `q_mvar` float NOT NULL COMMENT '无功功率(MVar)，v=1.0pu时',
  `vn_kv` float DEFAULT NULL COMMENT '额定电压(kV)',
  `step` int(11) DEFAULT '1' COMMENT '当前级数',
  `max_step` int(11) DEFAULT '1' COMMENT '最大级数',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `step_dependency_table` tinyint(1) DEFAULT '0' COMMENT '参数是否依赖级数',
  `id_characteristic_table` int(11) DEFAULT NULL COMMENT '特性曲线ID',
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shunt`
--

LOCK TABLES `shunt` WRITE;
/*!40000 ALTER TABLE `shunt` DISABLE KEYS */;
/*!40000 ALTER TABLE `shunt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sourcedc`
--

DROP TABLE IF EXISTS `sourcedc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sourcedc` (
  `net` json DEFAULT NULL,
  `bus_dc` int(11) NOT NULL,
  `vm_pu` float DEFAULT '1',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1',
  `source_type` varchar(50) DEFAULT NULL COMMENT '电源类型'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sourcedc`
--

LOCK TABLES `sourcedc` WRITE;
/*!40000 ALTER TABLE `sourcedc` DISABLE KEYS */;
/*!40000 ALTER TABLE `sourcedc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staticgenerator`
--

DROP TABLE IF EXISTS `staticgenerator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `staticgenerator` (
  `bus` int(11) NOT NULL,
  `p_mw` float NOT NULL COMMENT '有功功率(MW)',
  `q_mvar` float NOT NULL COMMENT '无功功率(MVar)',
  `sn_mva` float DEFAULT NULL COMMENT '额定容量(MVA)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `scaling` float DEFAULT '1' COMMENT '缩放因子',
  `type` enum('wye','delta') DEFAULT 'wye' COMMENT '连接类型：星型/三角型',
  `in_service` tinyint(1) DEFAULT '1',
  `max_p_mw` float DEFAULT NULL COMMENT '最大有功功率(MW)',
  `min_p_mw` float DEFAULT NULL COMMENT '最小有功功率(MW)',
  `max_q_mvar` float DEFAULT NULL COMMENT '最大无功功率(MVar)',
  `min_q_mvar` float DEFAULT NULL COMMENT '最小无功功率(MVar)',
  `controllable` tinyint(1) DEFAULT NULL COMMENT '是否可控',
  `k` float DEFAULT NULL COMMENT '短路电流与额定电流比',
  `rx` float DEFAULT NULL COMMENT 'R/X比（短路阻抗）',
  `reactive_capability_curve` tinyint(1) DEFAULT '0',
  `id_q_capability_characteristic` int(11) DEFAULT NULL COMMENT '无功能力特性曲线ID',
  `curve_style` varchar(50) DEFAULT NULL COMMENT '曲线样式',
  `generator_type` enum('current_source','async','async_doubly_fed') DEFAULT NULL COMMENT '发电机类型',
  `lrc_pu` float DEFAULT NULL COMMENT '堵转电流标幺值（异步发电机用）',
  `max_ik_ka` float DEFAULT NULL COMMENT '最大瞬时短路电流(kA)（双馈异步用）',
  `kappa` float DEFAULT NULL COMMENT '峰值短路电流系数（双馈异步用）',
  `current_source` tinyint(1) DEFAULT '0' COMMENT '是否作为电流源进行短路计算'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staticgenerator`
--

LOCK TABLES `staticgenerator` WRITE;
/*!40000 ALTER TABLE `staticgenerator` DISABLE KEYS */;
/*!40000 ALTER TABLE `staticgenerator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staticsynchronouscompensator`
--

DROP TABLE IF EXISTS `staticsynchronouscompensator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `staticsynchronouscompensator` (
  `bus` int(11) NOT NULL COMMENT '连接母线',
  `r_ohm` float NOT NULL COMMENT '耦合变压器电阻(Ω)',
  `x_ohm` float NOT NULL COMMENT '耦合变压器电抗(Ω)',
  `set_vm_pu` float DEFAULT '1',
  `vm_internal_pu` float NOT NULL COMMENT 'VSC内部电压幅值(标幺值)',
  `va_internal_degree` float DEFAULT '1',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控',
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staticsynchronouscompensator`
--

LOCK TABLES `staticsynchronouscompensator` WRITE;
/*!40000 ALTER TABLE `staticsynchronouscompensator` DISABLE KEYS */;
/*!40000 ALTER TABLE `staticsynchronouscompensator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staticvarcompensator`
--

DROP TABLE IF EXISTS `staticvarcompensator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `staticvarcompensator` (
  `bus` int(11) NOT NULL COMMENT '连接母线',
  `x_l_ohm` float NOT NULL COMMENT '感性电抗(Ω)',
  `x_cvar_ohm` float NOT NULL COMMENT '容性电抗(Ω)',
  `set_vm_pu` float DEFAULT '1',
  `thyristor_firing_angle_degree` float NOT NULL COMMENT '晶闸管触发角(度)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控',
  `in_service` tinyint(1) DEFAULT '1',
  `min_angle_degree` float DEFAULT '90' COMMENT '最小触发角(度)',
  `max_angle_degree` float DEFAULT '180' COMMENT '最大触发角(度)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staticvarcompensator`
--

LOCK TABLES `staticvarcompensator` WRITE;
/*!40000 ALTER TABLE `staticvarcompensator` DISABLE KEYS */;
/*!40000 ALTER TABLE `staticvarcompensator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage`
--

DROP TABLE IF EXISTS `storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage` (
  `bus_id` int(11) NOT NULL,
  `p_mw` float NOT NULL COMMENT '瞬时功率(MW)，正为充电，负为放电',
  `q_mvar` float DEFAULT NULL COMMENT '无功功率(MVar)',
  `max_e_mwh` float NOT NULL COMMENT '最大储能容量(MWh)',
  `min_e_mwh` float DEFAULT '0' COMMENT '最小储能容量(MWh)',
  `soc_percent` float NOT NULL COMMENT '当前荷电状态(%)',
  `sn_mva` float DEFAULT NULL COMMENT '额定容量(MVA)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `scaling` float DEFAULT '1' COMMENT '功率缩放因子',
  `type` varchar(50) DEFAULT NULL COMMENT '储能类型',
  `in_service` tinyint(1) DEFAULT '1',
  `max_p_mw` float DEFAULT NULL COMMENT '最大充电功率(MW)',
  `min_p_mw` float DEFAULT NULL COMMENT '最大放电功率(MW，负值)',
  `max_q_mvar` float DEFAULT NULL COMMENT '最大无功功率(MVar)',
  `min_q_mvar` float DEFAULT NULL COMMENT '最小无功功率(MVar)',
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage`
--

LOCK TABLES `storage` WRITE;
/*!40000 ALTER TABLE `storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `switch`
--

DROP TABLE IF EXISTS `switch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `switch` (
  `bus` int(11) NOT NULL,
  `element` int(11) NOT NULL,
  `et` enum('b','l','t','t3') NOT NULL,
  `closed` tinyint(1) DEFAULT '1',
  `switch_type` enum('LS','CB','LBS','DS') NOT NULL,
  `z_ohm` float DEFAULT '0',
  `in_ka` float DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `switch`
--

LOCK TABLES `switch` WRITE;
/*!40000 ALTER TABLE `switch` DISABLE KEYS */;
/*!40000 ALTER TABLE `switch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `threewindingtransformer`
--

DROP TABLE IF EXISTS `threewindingtransformer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `threewindingtransformer` (
  `index` int(11) NOT NULL AUTO_INCREMENT COMMENT '三绕组变压器唯一索引',
  `name` varchar(255) DEFAULT NULL COMMENT '变压器名称',
  `std_type` varchar(255) DEFAULT NULL COMMENT '标准型号名称',
  `hv_bus` int(11) NOT NULL COMMENT '高压侧母线索引',
  `mv_bus` int(11) NOT NULL COMMENT '中压侧母线索引',
  `lv_bus` int(11) NOT NULL COMMENT '低压侧母线索引',
  `vn_hv_kv` float NOT NULL COMMENT '高压侧额定电压(kV)',
  `vn_mv_kv` float NOT NULL COMMENT '中压侧额定电压(kV)',
  `vn_lv_kv` float NOT NULL COMMENT '低压侧额定电压(kV)',
  `sn_hv_mva` float NOT NULL COMMENT '高压侧额定视在功率(MVA)',
  `sn_mv_mva` float NOT NULL COMMENT '中压侧额定视在功率(MVA)',
  `sn_lv_mva` float NOT NULL COMMENT '低压侧额定视在功率(MVA)',
  `vk_hv_percent` float NOT NULL COMMENT '高中侧短路电压(%)',
  `vk_mv_percent` float NOT NULL COMMENT '中低侧短路电压(%)',
  `vk_lv_percent` float NOT NULL COMMENT '高低侧短路电压(%)',
  `vkr_hv_percent` float NOT NULL COMMENT '高中侧短路电阻分量(%)',
  `vkr_mv_percent` float NOT NULL COMMENT '中低侧短路电阻分量(%)',
  `vkr_lv_percent` float NOT NULL COMMENT '高低侧短路电阻分量(%)',
  `pfe_kw` float NOT NULL COMMENT '铁损(kW)',
  `i0_percent` float NOT NULL COMMENT '空载电流百分比(%)',
  `shift_mv_degree` float DEFAULT '0' COMMENT '中压侧相移角度',
  `shift_lv_degree` float DEFAULT '0' COMMENT '低压侧相移角度',
  `tap_side` enum('hv','mv','lv') DEFAULT NULL COMMENT '分接头所在侧：hv=高压侧，mv=中压侧，lv=低压侧',
  `tap_step_percent` float DEFAULT NULL COMMENT '分接头电压步长百分比',
  `tap_step_degree` float DEFAULT NULL COMMENT '分接头角度步长',
  `tap_neutral` int(11) DEFAULT NULL COMMENT '分接头中间档位',
  `tap_min` int(11) DEFAULT NULL COMMENT '分接头最小档位',
  `tap_max` int(11) DEFAULT NULL COMMENT '分接头最大档位',
  `tap_pos` int(11) DEFAULT NULL COMMENT '分接头当前位置',
  `tap_changer_type` enum('Ratio','Symmetrical','Ideal','Tabular') DEFAULT NULL COMMENT '分接头类型：Ratio=标准调压，Symmetrical=对称调压，Ideal=理想调压，Tabular=表格调压',
  `tap_at_star_point` tinyint(1) DEFAULT '0' COMMENT '分接头是否连接在星点侧',
  `vk0_hv_percent` float DEFAULT NULL COMMENT '高中侧零序短路电压(%)',
  `vk0_mv_percent` float DEFAULT NULL COMMENT '中低侧零序短路电压(%)',
  `vk0_lv_percent` float DEFAULT NULL COMMENT '高低侧零序短路电压(%)',
  `vkr0_hv_percent` float DEFAULT NULL COMMENT '高中侧零序短路电阻分量(%)',
  `vkr0_mv_percent` float DEFAULT NULL COMMENT '中低侧零序短路电阻分量(%)',
  `vkr0_lv_percent` float DEFAULT NULL COMMENT '高低侧零序短路电阻分量(%)',
  `vector_group` varchar(10) DEFAULT NULL COMMENT '连接组别，如 "YNyn0d5"',
  `tap_dependency_table` tinyint(1) DEFAULT '0' COMMENT '是否使用特性表：True=使用，False=不使用',
  `id_characteristic_table` int(11) DEFAULT NULL COMMENT '特性表索引',
  `in_service` tinyint(1) DEFAULT '1' COMMENT '投运状态：True=投运，False=退出',
  `max_loading_percent` float DEFAULT NULL COMMENT '最大负载率(%)，OPF用',
  PRIMARY KEY (`index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `threewindingtransformer`
--

LOCK TABLES `threewindingtransformer` WRITE;
/*!40000 ALTER TABLE `threewindingtransformer` DISABLE KEYS */;
/*!40000 ALTER TABLE `threewindingtransformer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thyristorcontrolledseriescapacitor`
--

DROP TABLE IF EXISTS `thyristorcontrolledseriescapacitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thyristorcontrolledseriescapacitor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `net` json DEFAULT NULL,
  `from_bus` int(11) NOT NULL COMMENT '起始母线',
  `to_bus` int(11) NOT NULL COMMENT '终止母线',
  `x_l_ohm` float NOT NULL COMMENT '感性电抗(Ω)',
  `x_cvar_ohm` float NOT NULL COMMENT '容性电抗(Ω)',
  `set_p_to_mw` float NOT NULL COMMENT 'to侧有功功率设定点(MW)',
  `thyristor_firing_angle_degree` float NOT NULL COMMENT '晶闸管触发角(度)',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控',
  `in_service` tinyint(1) DEFAULT '1',
  `min_angle_degree` float DEFAULT '90' COMMENT '最小触发角(度)',
  `max_angle_degree` float DEFAULT '180' COMMENT '最大触发角(度)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thyristorcontrolledseriescapacitor`
--

LOCK TABLES `thyristorcontrolledseriescapacitor` WRITE;
/*!40000 ALTER TABLE `thyristorcontrolledseriescapacitor` DISABLE KEYS */;
/*!40000 ALTER TABLE `thyristorcontrolledseriescapacitor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transformer`
--

DROP TABLE IF EXISTS `transformer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transformer` (
  `index` int(11) NOT NULL COMMENT '变压器唯一索引',
  `name` varchar(255) DEFAULT NULL COMMENT '变压器名称',
  `std_type` varchar(255) DEFAULT NULL COMMENT '标准型号',
  `hv_bus` int(11) NOT NULL COMMENT '高压侧母线索引',
  `lv_bus` int(11) NOT NULL COMMENT '低压侧母线索引',
  `sn_mva` float DEFAULT NULL COMMENT '额定视在功率(MVA)',
  `vn_hv_kv` float DEFAULT NULL COMMENT '高压侧额定电压(kV)',
  `vn_lv_kv` float DEFAULT NULL COMMENT '低压侧额定电压(kV)',
  `vkr_percent` float DEFAULT NULL COMMENT '短路电压有功分量(%)',
  `vk_percent` float DEFAULT NULL COMMENT '短路电压(%)',
  `pfe_kw` float DEFAULT NULL COMMENT '铁损(kW)',
  `i0_percent` float DEFAULT NULL COMMENT '空载电流(%)',
  `shift_degree` float DEFAULT '0' COMMENT '相移角度',
  `vector_group` varchar(10) DEFAULT NULL COMMENT '连接组别，如 "Dyn5", "Yyn0"',
  `vk0_percent` float DEFAULT NULL COMMENT '零序短路电压(%)',
  `vkr0_percent` float DEFAULT NULL COMMENT '零序短路电压有功分量(%)',
  `mag0_percent` float DEFAULT NULL COMMENT '零序励磁阻抗百分比',
  `mag0_rx` float DEFAULT NULL COMMENT '零序励磁R/X比',
  `si0_hv_partial` float DEFAULT NULL COMMENT '零序漏抗高压侧分配',
  `tap_side` enum('hv','lv') DEFAULT NULL COMMENT '分接头所在侧：hv=高压侧，lv=低压侧',
  `tap_neutral` int(11) DEFAULT NULL COMMENT '分接头中间档位',
  `tap_min` int(11) DEFAULT NULL COMMENT '分接头最小档位',
  `tap_max` int(11) DEFAULT NULL COMMENT '分接头最大档位',
  `tap_step_percent` float DEFAULT NULL COMMENT '分接头电压步长百分比',
  `tap_step_degree` float DEFAULT NULL COMMENT '分接头角度步长',
  `tap_pos` int(11) DEFAULT NULL COMMENT '分接头当前位置',
  `tap_changer_type` enum('Ratio','Symmetrical','Ideal','Tabular','None') DEFAULT 'Ratio' COMMENT '分接头类型：Ratio=标准调压，Symmetrical=对称调压，Ideal=理想调压，Tabular=表格调压，None=无分接头',
  `oltc` tinyint(1) DEFAULT '0' COMMENT '是否有载调压：True=有载调压，False=无励磁调压',
  `tap2_side` enum('hv','lv') DEFAULT NULL COMMENT '第二分接头所在侧：hv=高压侧，lv=低压侧',
  `tap2_neutral` int(11) DEFAULT NULL COMMENT '第二分接头中间档位',
  `tap2_min` int(11) DEFAULT NULL COMMENT '第二分接头最小档位',
  `tap2_max` int(11) DEFAULT NULL COMMENT '第二分接头最大档位',
  `tap2_step_percent` float DEFAULT NULL COMMENT '第二分接头电压步长百分比',
  `tap2_step_degree` float DEFAULT NULL COMMENT '第二分接头角度步长',
  `tap2_pos` int(11) DEFAULT NULL COMMENT '第二分接头当前位置',
  `tap2_changer_type` enum('Ratio','Symmetrical','Ideal','Tabular','None') DEFAULT NULL COMMENT '第二分接头类型：Ratio=标准调压，Symmetrical=对称调压，Ideal=理想调压，Tabular=表格调压，None=无分接头',
  `in_service` tinyint(1) DEFAULT '1' COMMENT '投运状态：True=投运，False=退出',
  `parallel` int(11) DEFAULT '1' COMMENT '并联台数',
  `df` float DEFAULT '1' COMMENT '降额系数：0-1之间的浮点数',
  `max_loading_percent` float DEFAULT NULL COMMENT '最大负载率(%)，OPF用',
  `tap_dependency_table` tinyint(1) DEFAULT '0' COMMENT '是否使用特性表：True=使用，False=不使用',
  `id_characteristic_table` int(11) DEFAULT NULL COMMENT '特性表索引',
  `xn_ohm` float DEFAULT NULL COMMENT '接地电抗(Ω)',
  `pt_percent` float DEFAULT NULL COMMENT '短路计算参数',
  `leakage_resistance_ratio_hv` float DEFAULT '0.5' COMMENT '电阻高压侧分配比：0-1之间的浮点数',
  `leakage_reactance_ratio_hv` float DEFAULT '0.5' COMMENT '电抗高压侧分配比：0-1之间的浮点数',
  PRIMARY KEY (`index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transformer`
--

LOCK TABLES `transformer` WRITE;
/*!40000 ALTER TABLE `transformer` DISABLE KEYS */;
/*!40000 ALTER TABLE `transformer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voltagesourceconverter`
--

DROP TABLE IF EXISTS `voltagesourceconverter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voltagesourceconverter` (
  `bus` int(11) NOT NULL COMMENT '交流侧连接母线',
  `bus_dc` int(11) NOT NULL COMMENT '直流侧连接母线',
  `r_ohm` float NOT NULL COMMENT '交流侧耦合电阻(Ω)',
  `x_ohm` float NOT NULL COMMENT '交流侧耦合电抗(Ω)',
  `r_dc_ohm` float NOT NULL COMMENT '直流侧内阻(Ω)',
  `pl_dc_mw` float NOT NULL COMMENT '直流侧空载损耗(MW)',
  `control_mode_ac` enum('vm_pu','q_mvar','slack') NOT NULL COMMENT '交流侧控制模式',
  `control_value_ac` float NOT NULL COMMENT '交流侧控制值',
  `control_mode_dc` enum('vm_pu','p_mw') NOT NULL COMMENT '直流侧控制模式',
  `control_value_dc` float NOT NULL COMMENT '直流侧控制值',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `controllable` tinyint(1) DEFAULT '0' COMMENT '是否可控',
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voltagesourceconverter`
--

LOCK TABLES `voltagesourceconverter` WRITE;
/*!40000 ALTER TABLE `voltagesourceconverter` DISABLE KEYS */;
/*!40000 ALTER TABLE `voltagesourceconverter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ward`
--

DROP TABLE IF EXISTS `ward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ward` (
  `bus` int(11) NOT NULL,
  `ps_mw` float NOT NULL COMMENT 'PQ部分有功功率(MW)',
  `qs_mvar` float NOT NULL COMMENT 'PQ部分无功功率(MVar)',
  `pz_mw` float NOT NULL COMMENT '阻抗部分有功功率(MW)，1.0pu时',
  `qz_mvar` float NOT NULL COMMENT '阻抗部分无功功率(MVar)，1.0pu时',
  `name` varchar(100) DEFAULT NULL,
  `index` int(11) DEFAULT NULL,
  `in_service` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ward`
--

LOCK TABLES `ward` WRITE;
/*!40000 ALTER TABLE `ward` DISABLE KEYS */;
/*!40000 ALTER TABLE `ward` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 13:16:56
