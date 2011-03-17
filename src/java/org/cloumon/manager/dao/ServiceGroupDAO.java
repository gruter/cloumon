package org.cloumon.manager.dao;

import java.io.IOException;
import java.util.List;

import org.cloumon.thrift.ServiceGroup;

public interface ServiceGroupDAO {
  public List<ServiceGroup> findAllServiceGroups() throws IOException;
  public void addServiceGroup(ServiceGroup serviceGroup) throws IOException;
  public void removeServiceGroup(String serviceGroupName) throws IOException;
  
  public void addHostToServiceGroup(String serviceGroupName, String hostName) throws IOException;
  public void removeHostToServiceGroup(String serviceGroupName, String hostName) throws IOException;
  public void removeServiceHosts(String serviceGroupName) throws IOException;
  public void deleteAll() throws IOException;
}
