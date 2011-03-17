CREATE DATABASE monitor;
GRANT ALL PRIVILEGES ON monitor.* TO monitor@localhost IDENTIFIED BY 'monitor';
GRANT ALL PRIVILEGES ON monitor.* TO monitor@"%" IDENTIFIED BY 'monitor';

CREATE TABLE Host (
  HostName              VARCHAR(100) NOT NULL,
  HostIP                VARCHAR(255) NOT NULL,
  HostType              VARCHAR( 10) NOT NULL,
  CpuCores              INT(3),
  CpuModel              VARCHAR(255),
  CpuVendor             VARCHAR(255),
  CpuMhz                INT(5),
  MachineName           VARCHAR(100),
  MachineVersion        VARCHAR(100),
  MachineArch           VARCHAR(100),
  Machine               VARCHAR(100),
  MachineDescription    VARCHAR(100),
  MachinePatchLevel     VARCHAR(100),
  MachineVendor         VARCHAR(100),
  MachineVendorVersion  VARCHAR(100),
  MachineVendorName     VARCHAR(100),
  MachineVendorCodeName VARCHAR(100),
  MachineDataModel      VARCHAR(100),
  MachineCpuEndian      VARCHAR(100),
  MachineJvmVersion     VARCHAR(100),
  MachineJvmVendor      VARCHAR(100),
  MachineJvmHome        VARCHAR(100),
  MemotyTotal           BIGINT,
  SwapTotal             BIGINT,
  FileSystem            VARCHAR(255),
  NetworkInterfaces     VARCHAR(255),
  LiveStatus            VARCHAR(  1),   /* Y or N */
  AlarmOn               VARCHAR(  1),   /* Y or N */
  HostAlarm             VARCHAR(255),
  PRIMARY KEY (HostName)
 ) DEFAULT CHARSET='utf8' ;
  
CREATE TABLE MonitorItem (
  ItemID        VARCHAR( 20) NOT NULL,
  ItemName      VARCHAR( 50) NOT NULL,
  GroupName     VARCHAR( 50) ,
  AdaptorClass  VARCHAR(255) NOT NULL,
  defaultItem   VARCHAR(  1) ,
  Period        INT    (  6) DEFAULT 60,
  Params        TEXT,
  Description   TEXT,
  ALARM         TEXT         ,
  PRIMARY KEY(ItemID)
 ) DEFAULT CHARSET='utf8' ;

CREATE TABLE HostMonitorItem (
  ItemID        VARCHAR( 20) NOT NULL,
  HostName      VARCHAR(100) NOT NULL,
  ALARM         TEXT         ,
  PRIMARY KEY(ItemID, HostName)
 ) DEFAULT CHARSET='utf8' ;


CREATE TABLE MetricRecord (
  MetricRecordID  BIGINT       NOT NULL,
  ItemID          VARCHAR( 20),
  ItemName        VARCHAR( 50) NOT NULL,
  GroupName       VARCHAR( 50) ,
  HostName        VARCHAR(255) NOT NULL,
  HostIp          VARCHAR(255) NOT NULL,
  ResourceName    VARCHAR(255) , 
  MetricData      TEXT,
  CollectTime     DATETIME NOT NULL,
  PRIMARY KEY(MetricRecordID)
) DEFAULT CHARSET='utf8' ;


CREATE INDEX MetricRecord_IX01
    ON MetricRecord (ItemID);
    
CREATE INDEX MetricRecord_IX02
    ON MetricRecord (ItemName);

CREATE INDEX MetricRecord_IX03
    ON MetricRecord (GroupName);    
    
CREATE INDEX MetricRecord_IX04
    ON MetricRecord (HostName);
    
CREATE INDEX MetricRecord_IX05
    ON MetricRecord (CollectTime);    
        
CREATE TABLE CurrentMetricRecord (
  MetricRecordID  BIGINT       NOT NULL,
  HostName        VARCHAR(100) NOT NULL,
  ItemID          VARCHAR( 20),
  ItemName        VARCHAR( 50) NOT NULL,
  GroupName       VARCHAR( 50) ,
  HostIp          VARCHAR(255) NOT NULL,
  ResourceName    VARCHAR(255) , 
  MetricData      TEXT,
  CollectTime     DATETIME NOT NULL,
  PRIMARY KEY(MetricRecordID)
) DEFAULT CHARSET='utf8' ;

CREATE TABLE SummaryMetricRecord (
  MetricRecordID  BIGINT       NOT NULL,
  SummaryType     INT(1) NOT NULL,   /* 1: Monthly, 2:weekly */
  Year            INT(4) NOT NULL,
  Month           INT(2) NOT NULL,
  WeekOfYear      INT(2) ,
  FromDate        VARCHAR(8),
  EndDate          VARCHAR(8),
  HostName        VARCHAR(100) NOT NULL,
  ItemID          VARCHAR( 20),
  ItemName        VARCHAR( 50) NOT NULL,
  GroupName       VARCHAR( 50) ,
  HostIp          VARCHAR(255) NOT NULL,
  ResourceName    VARCHAR(255) , 
  MetricData      TEXT,
  PRIMARY KEY(MetricRecordID)
) DEFAULT CHARSET='utf8' ;

CREATE TABLE ServiceGroup (
  ServiceGroupName  VARCHAR(100) NOT NULL,
  PRIMARY KEY(ServiceGroupName)
) DEFAULT CHARSET='utf8' ;

CREATE TABLE ServiceHosts (
  ServiceGroupName  VARCHAR(100) NOT NULL,
  HostName          VARCHAR(100) NOT NULL,
  PRIMARY KEY(ServiceGroupName, HostName)   
) DEFAULT CHARSET='utf8' ;
 
 