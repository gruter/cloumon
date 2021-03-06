/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.cloumon.thrift;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemNetworkInterfaceInfo implements org.apache.thrift.TBase<SystemNetworkInterfaceInfo, SystemNetworkInterfaceInfo._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SystemNetworkInterfaceInfo");

  private static final org.apache.thrift.protocol.TField NETWORK_INTERFACES_FIELD_DESC = new org.apache.thrift.protocol.TField("networkInterfaces", org.apache.thrift.protocol.TType.LIST, (short)1);

  public List<String> networkInterfaces;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NETWORK_INTERFACES((short)1, "networkInterfaces");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // NETWORK_INTERFACES
          return NETWORK_INTERFACES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NETWORK_INTERFACES, new org.apache.thrift.meta_data.FieldMetaData("networkInterfaces", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SystemNetworkInterfaceInfo.class, metaDataMap);
  }

  public SystemNetworkInterfaceInfo() {
  }

  public SystemNetworkInterfaceInfo(
    List<String> networkInterfaces)
  {
    this();
    this.networkInterfaces = networkInterfaces;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public SystemNetworkInterfaceInfo(SystemNetworkInterfaceInfo other) {
    if (other.isSetNetworkInterfaces()) {
      List<String> __this__networkInterfaces = new ArrayList<String>();
      for (String other_element : other.networkInterfaces) {
        __this__networkInterfaces.add(other_element);
      }
      this.networkInterfaces = __this__networkInterfaces;
    }
  }

  public SystemNetworkInterfaceInfo deepCopy() {
    return new SystemNetworkInterfaceInfo(this);
  }

  @Override
  public void clear() {
    this.networkInterfaces = null;
  }

  public int getNetworkInterfacesSize() {
    return (this.networkInterfaces == null) ? 0 : this.networkInterfaces.size();
  }

  public java.util.Iterator<String> getNetworkInterfacesIterator() {
    return (this.networkInterfaces == null) ? null : this.networkInterfaces.iterator();
  }

  public void addToNetworkInterfaces(String elem) {
    if (this.networkInterfaces == null) {
      this.networkInterfaces = new ArrayList<String>();
    }
    this.networkInterfaces.add(elem);
  }

  public List<String> getNetworkInterfaces() {
    return this.networkInterfaces;
  }

  public SystemNetworkInterfaceInfo setNetworkInterfaces(List<String> networkInterfaces) {
    this.networkInterfaces = networkInterfaces;
    return this;
  }

  public void unsetNetworkInterfaces() {
    this.networkInterfaces = null;
  }

  /** Returns true if field networkInterfaces is set (has been assigned a value) and false otherwise */
  public boolean isSetNetworkInterfaces() {
    return this.networkInterfaces != null;
  }

  public void setNetworkInterfacesIsSet(boolean value) {
    if (!value) {
      this.networkInterfaces = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case NETWORK_INTERFACES:
      if (value == null) {
        unsetNetworkInterfaces();
      } else {
        setNetworkInterfaces((List<String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NETWORK_INTERFACES:
      return getNetworkInterfaces();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case NETWORK_INTERFACES:
      return isSetNetworkInterfaces();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SystemNetworkInterfaceInfo)
      return this.equals((SystemNetworkInterfaceInfo)that);
    return false;
  }

  public boolean equals(SystemNetworkInterfaceInfo that) {
    if (that == null)
      return false;

    boolean this_present_networkInterfaces = true && this.isSetNetworkInterfaces();
    boolean that_present_networkInterfaces = true && that.isSetNetworkInterfaces();
    if (this_present_networkInterfaces || that_present_networkInterfaces) {
      if (!(this_present_networkInterfaces && that_present_networkInterfaces))
        return false;
      if (!this.networkInterfaces.equals(that.networkInterfaces))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(SystemNetworkInterfaceInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    SystemNetworkInterfaceInfo typedOther = (SystemNetworkInterfaceInfo)other;

    lastComparison = Boolean.valueOf(isSetNetworkInterfaces()).compareTo(typedOther.isSetNetworkInterfaces());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNetworkInterfaces()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.networkInterfaces, typedOther.networkInterfaces);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // NETWORK_INTERFACES
          if (field.type == org.apache.thrift.protocol.TType.LIST) {
            {
              org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
              this.networkInterfaces = new ArrayList<String>(_list8.size);
              for (int _i9 = 0; _i9 < _list8.size; ++_i9)
              {
                String _elem10;
                _elem10 = iprot.readString();
                this.networkInterfaces.add(_elem10);
              }
              iprot.readListEnd();
            }
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.networkInterfaces != null) {
      oprot.writeFieldBegin(NETWORK_INTERFACES_FIELD_DESC);
      {
        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, this.networkInterfaces.size()));
        for (String _iter11 : this.networkInterfaces)
        {
          oprot.writeString(_iter11);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SystemNetworkInterfaceInfo(");
    boolean first = true;

    sb.append("networkInterfaces:");
    if (this.networkInterfaces == null) {
      sb.append("null");
    } else {
      sb.append(this.networkInterfaces);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

