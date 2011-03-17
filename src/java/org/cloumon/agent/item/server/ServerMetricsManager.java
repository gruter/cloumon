package org.cloumon.agent.item.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.SystemCpuInfo;
import org.cloumon.thrift.SystemFileSystemInfo;
import org.cloumon.thrift.SystemMachineInfo;
import org.cloumon.thrift.SystemMemInfo;
import org.cloumon.thrift.SystemNetworkInterfaceInfo;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NfsFileSystem;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.ProcStat;
import org.hyperic.sigar.ResourceLimit;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.SysInfo;
import org.hyperic.sigar.Tcp;
import org.hyperic.sigar.Uptime;

public class ServerMetricsManager {
  static Log LOG = LogFactory.getLog(ServerMetricsManager.class);
  
  //http://support.hyperic.com/display/SIGAR/PTQL
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  public static Map<String, NetworkMetrics> previousNetworkMetrics = new HashMap<String, NetworkMetrics>(); 
  
  private static CpuPercMetrics oldCpuPerc;
  private static Integer lock = new Integer(0);
  
  private Sigar sigar;
  
  public ServerMetricsManager() {
    sigar = new Sigar();
  }
  
  public void close() {
    sigar.close();
  }

  public HostInfo getHostInfo() throws IOException {
    HostInfo hostInfo = new HostInfo();
    hostInfo.setCpuInfo(getSystemCpuInfo());
    hostInfo.setFileSystemInfo(getSystemFileSystemInfo());
    hostInfo.setMemInfo(getSystemMemInfo());
    hostInfo.setNetworkInfo(getSystemNetworkInterfaceInfo());
    hostInfo.setMachineInfo(getSystemMachineInfo());

    hostInfo.setHostIps(getSystemIps());
    return hostInfo;
  }
  
  public List<String> getSystemIps() throws IOException {
    List<String> ips = new ArrayList<String>();
    try {
      for (String eachNetInerface: sigar.getNetInterfaceList()) {
        try {
          NetInterfaceConfig netIfConfig = sigar.getNetInterfaceConfig(eachNetInerface);
          if (netIfConfig == null) {
            continue;
          }
          if ("ETHERNET".equals(netIfConfig.getType().toUpperCase())) {
            ips.add(netIfConfig.getAddress());
          }
        } catch (SigarException e) {
          LOG.warn(e.getMessage(), e);
        }    
      }
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      //throw new IOException(e.getMessage(), e);
    }    
    
    return ips;
  }
  
  /**
   * 서버의 버전, 벤더 등과 같은 기본 정보를 반환한다.
   * @return
   */
  public SystemMachineInfo getSystemMachineInfo() {
    OperatingSystem sys = OperatingSystem.getInstance();
    SystemMachineInfo systemMachineInfo = new SystemMachineInfo();
    
    systemMachineInfo.setDescription(sys.getDescription());
    systemMachineInfo.setName(sys.getName());
    systemMachineInfo.setArch(sys.getArch());
    systemMachineInfo.setMachine(sys.getMachine());
    systemMachineInfo.setVersion(sys.getVersion());
    systemMachineInfo.setPatchLevel(sys.getPatchLevel());
    systemMachineInfo.setVendor(sys.getVendor());
    systemMachineInfo.setVendorVersion(sys.getVendorVersion());
    systemMachineInfo.setVendorCodeName(sys.getVendorCodeName());
    systemMachineInfo.setDataModel(sys.getDataModel());
    systemMachineInfo.setCpuEndian(sys.getCpuEndian());

    systemMachineInfo.setJvmVersion(System.getProperty("java.vm.version"));
    systemMachineInfo.setJvmVendor(System.getProperty("java.vm.vendor"));
    systemMachineInfo.setJvmHome(System.getProperty("java.home"));
    
    return systemMachineInfo;
  }
  
  /**
   * 서버의 uptime 정보를 반환한다.
   * @return
   * @throws IOException
   */
  public UptimeMetrics getServerUptime() throws IOException {
    UptimeMetrics serverUptime = new UptimeMetrics();
    
    double[] loadAverages;
    try {
      loadAverages = sigar.getLoadAverage();
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
    
    serverUptime.setLoadAverage1(loadAverages[0]);
    serverUptime.setLoadAverage2(loadAverages[1]);
    serverUptime.setLoadAverage3(loadAverages[2]);

    try {
      double uptime = 0;
      uptime = sigar.getUptime().getUptime();
      serverUptime.setUptime(uptime);
      serverUptime.setUptimeString(parseUptime(uptime));
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }

    return serverUptime;
  }
  
  /**
   * 서버의 CPU 갯수, 코어 등에 대한 기본 정보를 반환한다.
   * @return
   * @throws IOException   
   */
  public SystemCpuInfo getSystemCpuInfo() throws IOException {
    CpuInfo[] cpuInfos;
    try {
      cpuInfos = sigar.getCpuInfoList();
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }

    CpuInfo info = cpuInfos[0];
    
    SystemCpuInfo systemCpuInfo = new SystemCpuInfo();
    
    systemCpuInfo.setCacheSize(info.getCacheSize());
    systemCpuInfo.setVendor(info.getVendor());
    systemCpuInfo.setModel(info.getModel());
    systemCpuInfo.setMhz(info.getMhz());
    systemCpuInfo.setTotalCores(info.getTotalCores());
    systemCpuInfo.setTotalSockets(info.getTotalSockets());
    systemCpuInfo.setCoresPerSocket(info.getCoresPerSocket());
    
    return systemCpuInfo;
  }
  
  /**
   * 서버의 메모리 정보를 반환한다.
   * @return
   * @throws IOException
   */
  public SystemMemInfo getSystemMemInfo() throws IOException {
    try {
      Mem mem   = sigar.getMem();
      Swap swap = sigar.getSwap();
  
      SystemMemInfo systemMemInfo = new SystemMemInfo();
      systemMemInfo.setMemory(mem.getRam());
      systemMemInfo.setTotal(mem.getTotal());
      systemMemInfo.setUsed(mem.getUsed());
      systemMemInfo.setFree(mem.getFree());
      systemMemInfo.setActualUsed(mem.getActualUsed());
      systemMemInfo.setActualFree(mem.getActualFree());
      systemMemInfo.setSwapTotal(swap.getTotal());
      systemMemInfo.setSwapUsed(swap.getUsed());
      systemMemInfo.setSwapUsed(swap.getFree());
      systemMemInfo.setSwapPageIn(swap.getPageIn());
      systemMemInfo.setSwapPageOut(swap.getPageOut());
      
      return systemMemInfo;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }
  
  /**
   * 서버의 파일 시스템 목록을 보여준다.
   * @return
   * @throws IOException
   */
  public SystemFileSystemInfo getSystemFileSystemInfo() throws IOException {
    try {
      SystemFileSystemInfo systemFileSystemInfo = new SystemFileSystemInfo();
      
      FileSystem[] fileSystems = sigar.getFileSystemList();
      
      List<String> fileSystemInfos = new ArrayList<String>();
      for (int i = 0; i < fileSystems.length; i++) {
        fileSystemInfos.add(fileSystems[i].getDirName());
      }
      systemFileSystemInfo.setFileSystems(fileSystemInfos);
      return systemFileSystemInfo;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }

  public SystemNetworkInterfaceInfo getSystemNetworkInterfaceInfo() throws IOException {
    try {
      SystemNetworkInterfaceInfo systemNetworkInterfaceInfo = new SystemNetworkInterfaceInfo();
      
      systemNetworkInterfaceInfo.setNetworkInterfaces(Arrays.asList(sigar.getNetInterfaceList()));
      return systemNetworkInterfaceInfo;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }

  public MemoryMetrics getMemoryMetrics() throws IOException {
    try {
      MemoryMetrics memoryMetrics = new MemoryMetrics();

      Mem mem   = sigar.getMem();
      Swap swap = sigar.getSwap();
  
      memoryMetrics.setRam(mem.getRam());
      memoryMetrics.setTotal(mem.getTotal());
      memoryMetrics.setUsed(mem.getUsed());
      memoryMetrics.setFree(mem.getFree());
      memoryMetrics.setActualUsed(mem.getActualUsed());
      memoryMetrics.setActualFree(mem.getActualFree());
      memoryMetrics.setSwapTotal(swap.getTotal());
      memoryMetrics.setSwapUsed(swap.getUsed());
      memoryMetrics.setSwapUsed(swap.getFree());
      memoryMetrics.setSwapPageIn(swap.getPageIn());
      memoryMetrics.setSwapPageOut(swap.getPageOut());

      return memoryMetrics;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }
  
  /**
   * CPU 상태 정보를 반환
   * @return
   * @throws IOException
   */
  public CpuPercMetrics getCpuPercMetrics() throws IOException {
    try {
      CpuPerc cpuPerc = sigar.getCpuPerc();
      
      CpuPercMetrics cpuPercMetrics = new CpuPercMetrics();

      cpuPercMetrics.setUser(cpuPerc.getUser());
      cpuPercMetrics.setSys(cpuPerc.getSys());
      cpuPercMetrics.setNice(cpuPerc.getNice());
      cpuPercMetrics.setIdle(cpuPerc.getIdle());
      cpuPercMetrics.setWait(cpuPerc.getWait());
      cpuPercMetrics.setIrq(cpuPerc.getIrq());
      cpuPercMetrics.setSoftIrq(cpuPerc.getSoftIrq());
      cpuPercMetrics.setStolen(cpuPerc.getStolen());
      cpuPercMetrics.setCombined(cpuPerc.getCombined());
      
      if (new Double(cpuPercMetrics.getUser()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getSys()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getNice()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getIdle()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getWait()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getIrq()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getSoftIrq()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getStolen()).equals(Double.NaN) ||
          new Double(cpuPercMetrics.getCombined()).equals(Double.NaN)) {
        synchronized(lock) {
          if(oldCpuPerc == null) {
            return null;
          }
          return oldCpuPerc;
        }
      }
      double[] loadAverages = sigar.getLoadAverage();
      cpuPercMetrics.setLoad(loadAverages[0]);
      
      synchronized(lock) {
        oldCpuPerc = cpuPercMetrics;
      }
      return cpuPercMetrics;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * DiskUsage 정보를 반환
   * @return
   * @throws IOException
   */
  public List<DiskUsageMetrics> getDiskUsageMetrics() throws IOException {
    List<DiskUsageMetrics> diskUsages = new ArrayList<DiskUsageMetrics>();
    try {
      FileSystem[] fileSystems = sigar.getFileSystemList();
      for (FileSystem eachFileSystem: fileSystems) {
        if (eachFileSystem instanceof NfsFileSystem) {
          continue;
        }
        if (eachFileSystem.getSysTypeName().equals("vboxsf")) {
          continue;
        }
        try {
          FileSystemUsage usage = sigar.getFileSystemUsage(eachFileSystem.getDirName());
          if (usage.getTotal() == 0) {
            continue;
          }
          DiskUsageMetrics diskUsage = new DiskUsageMetrics();
          diskUsage.setResourceName(eachFileSystem.getDevName() + "," + eachFileSystem.getSysTypeName() + "/" + eachFileSystem.getTypeName());
          diskUsage.setUsed(usage.getTotal() - usage.getFree());
          diskUsage.setAvail(usage.getAvail());
          diskUsage.setSize(usage.getTotal());
          diskUsage.setUsePercent(usage.getUsePercent() * 100);
          diskUsage.setMountedOn(eachFileSystem.getDirName());
          diskUsage.setType(eachFileSystem.getSysTypeName() + "/" + eachFileSystem.getTypeName());
          diskUsages.add(diskUsage);
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
      }
      
      return diskUsages;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      return diskUsages;
    }
  }
  
  /**
   * Nwtwork 트래픽 정보를 반환
   * @return
   * @throws IOException
   */
  public List<NetworkMetrics> getNetworkMetrics() throws IOException {
    List<NetworkMetrics> networkMetrics = new ArrayList<NetworkMetrics>();
    try {
      for (String netIf: sigar.getNetInterfaceList()) {
        try {
          NetInterfaceStat netStat = sigar.getNetInterfaceStat(netIf);
          if (netStat == null) {
            continue;
          }
          
          NetworkMetrics networkInfo = new NetworkMetrics();
          networkInfo.setResourceName(netIf);
          networkInfo.setRxBytes(netStat.getRxBytes());
          networkInfo.setRxPackets(netStat.getRxPackets());
          networkInfo.setTxBytes(netStat.getTxBytes());
          networkInfo.setRxPackets(netStat.getTxPackets());
          
          synchronized(previousNetworkMetrics) {
            if (previousNetworkMetrics.containsKey(netIf)) {
              NetworkMetrics previous = previousNetworkMetrics.get(netIf);
              
              NetworkMetrics sendNetworkInfo = new NetworkMetrics();
              sendNetworkInfo.setResourceName(netIf);
              sendNetworkInfo.setRxBytes(netStat.getRxBytes() - previous.getRxBytes());
              sendNetworkInfo.setRxPackets(netStat.getRxPackets() - previous.getRxPackets());
              sendNetworkInfo.setTxBytes(netStat.getTxBytes() - previous.getTxBytes());
              sendNetworkInfo.setRxPackets(netStat.getTxPackets() - previous.getTxPackets());
              
              networkMetrics.add(sendNetworkInfo);
            }
            previousNetworkMetrics.put(netIf, networkInfo);
          }
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
          continue;
        }
      }
      return networkMetrics;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      return networkMetrics;
    }
  }
  
  /**
   * 특정 포트를 사용하고 있는 프로세스가 존재하는지 확인한다.
   * @param protocol tpc 또는 udp
   * @param port
   * @return
   * @throws IOException
   */
  public boolean existsProcess(String protocol, int port)  throws IOException {
    try {
      long pid = sigar.getProcPort(protocol, String.valueOf(port));
      if (pid > 0) {
        return true;
      } else {
        return false;
      }
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      return false;
    }
  }

  public int getProcessCount() throws IOException {
    try {
      long[] processes = sigar.getProcList();
      return processes.length;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      return -1;
    }
  }
  
  /**
   * 특정 이름을 가진 프로세스가 존재하는지 확인한다.
   * @param programName
   * @return
   * @throws IOException
   */
  public boolean existsProcess(String programName)  throws IOException {
    try {
      long[] processes = sigar.getProcList();
      for (long pid: processes) {
        for (String eachParam: sigar.getProcArgs(pid)) {
          if(eachParam.equals(programName)) {
            return true;
          }
        }
      }
      
      return false;
    } catch (SigarException e) {
      LOG.error(e.getMessage(), e);
      return false;
    }
  }

  /**
   * 시스템의 uptime 정보는 sec 단위인데 이 단위를 일, 시, 분으로 변환한다.
   * @param uptime
   * @return
   */
  public static String parseUptime(double uptime) {
    String retval = " ";

    int days = (int)uptime / (60*60*24);
    int minutes, hours;

    if (days != 0) {
        retval += days + " " + ((days > 1) ? "days" : "day") + ", ";
    }

    minutes = (int)uptime / 60;
    hours = minutes / 60;
    hours %= 24;
    minutes %= 60;

    if (hours != 0) {
        retval += hours + ":" + minutes;
    }
    else {
        retval += minutes + " min";
    }
    
    return dateFormat.format(new Date()) + retval;  
  }

  private static void printMap(String type, Map<Object, Object> map) {
    printMap(type, map, "");
  }
  
  private static void printMap(String type, Map<Object, Object> map, String prefix) {
    System.out.println("===================== " + type + " ========================");
    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      System.out.println(prefix + entry.getKey() + ": " + entry.getValue());
    }
  }

  private static void printObject(String type, Object obj) {
    System.out.println("===================== " + type + " ========================");
    
    for (Field eachField : obj.getClass().getDeclaredFields()) {
      try {
        String fieldName = eachField.getName();
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if(fieldName.startsWith("SerialVersion")) {
          continue;
        }
        Method method = obj.getClass().getMethod("get" + fieldName);
        System.out.println(eachField.getName() + ": " + method.invoke(obj));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void main(String[] args) throws Exception {
//    ServerMetricsManager manager = new ServerMetricsManager();
//    manager.getDiskUsageMetrics();
    testSigar();
  }
  
  public static void testSigar() throws Exception {
    
    ServerMetricsManager serverMetricsManager = new ServerMetricsManager();
    
//    long pid = serverMetricsManager.sigar.getProcPort("tcp", "9999");
//    System.out.println(">>>pid>" + pid);
//    for(Object obj: serverMetricsManager.sigar.getProcModules(pid)) {
//      System.out.println(">>>>" + obj);
//    }

    long[] processIds = serverMetricsManager.sigar.getProcList();
    for(long pid: processIds) {
      for(Object obj: serverMetricsManager.sigar.getProcModules(pid)) {
        System.out.println(">>>>" + obj);
      }
    }
    
    if(true) {
      return;
    }
    
    SysInfo sysInfo = new SysInfo();
    sysInfo.gather(serverMetricsManager.sigar);
    printMap("SysInfo", sysInfo.toMap());
    
    Cpu cpu = serverMetricsManager.sigar.getCpu();
    printMap("Cpu", cpu.toMap());

    Cpu[] cpus = serverMetricsManager.sigar.getCpuList();
    for (int i = 0; i < cpus.length; i++) {
      printMap("Cpu_" + (i + 1), cpus[i].toMap());
    }

    CpuInfo[] cpuInfos = serverMetricsManager.sigar.getCpuInfoList();
    for (int i = 0; i < cpuInfos.length; i++) {
      printMap("CpuInfo_" + (i + 1), cpuInfos[i].toMap());
    }

    CpuPerc[] cpuPercs = serverMetricsManager.sigar.getCpuPercList();
    for (int i = 0; i < cpuPercs.length; i++) {
      printObject("CpuPerc_" + (i + 1), cpuPercs[i]);
    }
    
    CpuPerc cpuPerc = serverMetricsManager.sigar.getCpuPerc();
    printObject("CpuPerc", cpuPerc);
    
    FileSystemMap fileSystemMap = serverMetricsManager.sigar.getFileSystemMap();
    printMap("FileSystemMap", fileSystemMap);
    
    FileSystem[] fileSystems = serverMetricsManager.sigar.getFileSystemList();
    FileSystemUsage[] fileSystemUsages = new FileSystemUsage[fileSystems.length];
    DiskUsage[] diskUsages = new DiskUsage[fileSystems.length];
    for (int i = 0; i < fileSystems.length; i++) {
      printMap("FileSystems" + (i + 1), fileSystems[i].toMap());
      if(fileSystems[i].getSysTypeName().equals("vboxsf")) {
        continue;
      }
      fileSystemUsages[i] = serverMetricsManager.sigar.getFileSystemUsage(fileSystems[i].getDirName());
      switch(fileSystems[i].getType()){
        case FileSystem.TYPE_LOCAL_DISK:
          diskUsages[i] = serverMetricsManager.sigar.getDiskUsage(fileSystems[i].getDirName());
      }
    }    

//    for (int i = 0; i < fileSystems.length; i++) {
//      printMap("FileSystemUsage:" + fileSystems[i].getDirName(), fileSystemUsages[i].toMap());
//    }
    
    for (int i = 0; i < diskUsages.length; i++) {
      if (diskUsages[i] != null) {
        printMap("DiskUsage:" + fileSystems[i].getDirName(), diskUsages[i].toMap());
      }
    }
    
    Map<Object, Object> map = new HashMap<Object, Object>();
    map.put("FQDN", serverMetricsManager.sigar.getFQDN());
    printMap("FQDN", map);

    map.clear();
    double[] loadAverages = serverMetricsManager.sigar.getLoadAverage();
    for(int i = 0 ; i < loadAverages.length; i++) {
      map.put("LoadAverge_" + (i + 1), loadAverages[i]);
    }
    printMap("LoadAverge", map);
    
    Mem mem = serverMetricsManager.sigar.getMem();
    printMap("Mem", mem.toMap());
    
    MultiProcCpu multiProcCpu = serverMetricsManager.sigar.getMultiProcCpu("State.Name.eq=sshd");
    printMap("MultiProcCpu", multiProcCpu.toMap());
    
    NetInfo netInfo = serverMetricsManager.sigar.getNetInfo();
    printMap("NetInfo", netInfo.toMap());
    
    map.clear();
    String[] netInterfaces = serverMetricsManager.sigar.getNetInterfaceList();
    for(int i = 0; i < netInterfaces.length; i++) {
      map.put("NetInterfaces_" + (i + 1), netInterfaces[i]);
    }
    printMap("NetInterfaces", map);
    
    try {
      NetInterfaceConfig netInterfaceConfig = serverMetricsManager.sigar.getNetInterfaceConfig();
      printMap("NetInterfaceConfig", netInterfaceConfig.toMap());
    } catch (Exception e) {
      System.out.println("Error:" + e.getMessage());
    }
    
//    int flag = NetFlags.CONN_SERVER | NetFlags.CONN_TCP | NetFlags.CONN_UDP | NetFlags.CONN_UDP;
//    NetConnection[] netConnections = serverMetricsManager.sigar.getNetConnectionList(flag);
//    for (int i = 0; i < netConnections.length; i++) {
//      printMap("NetConnections_" + (i + 1), netConnections[i].toMap());
//    }    
    
//    NetRoute[] netRoutes =  serverMetricsManager.sigar.getNetRouteList();
//    for (int i = 0; i < netRoutes.length; i++) {
//      printMap("NetRoute", netRoutes[i].toMap());
//    }

    ProcStat procStat = serverMetricsManager.sigar.getProcStat();
    printMap("ProcStat", procStat.toMap());

    
    ResourceLimit resourceLimit = serverMetricsManager.sigar.getResourceLimit();
    printMap("ResourceLimit", resourceLimit.toMap());
    
    Swap swap = serverMetricsManager.sigar.getSwap();
    printMap("Swap", swap.toMap());
    
    Tcp tcp = serverMetricsManager.sigar.getTcp();
    printMap("Tcp", tcp.toMap());
    
    Uptime uptime = serverMetricsManager.sigar.getUptime();
    printMap("Uptime", uptime.toMap());
    
    long[] pids = serverMetricsManager.sigar.getProcList();
  }

}
