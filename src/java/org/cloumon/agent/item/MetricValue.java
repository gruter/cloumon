package org.cloumon.agent.item;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class MetricValue {
  protected String resourceName = "";
  
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> fieldMaps = new HashMap<String, Object>();
    Field[] fields = this.getClass().getDeclaredFields();
    
    for (Field eachField: fields) {
      try {
        fieldMaps.put(eachField.getName(), eachField.get(this));
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return fieldMaps;
  }
  
  public void print(OutputStream out) throws IOException {
    out.write(toString().getBytes());
  }
  
  public String toString() {
    Field[] fields = this.getClass().getDeclaredFields();
    
    StringBuilder sb = new StringBuilder();
    
    for (Field eachField: fields) {
      try {
        String fieldName = eachField.getName();
        Object fieldValue = eachField.get(this);
        if(fieldValue != null && fieldValue.getClass().isArray()) {
          //Class dataType = fieldValue.getClass().getComponentType();
          int fieldArrayLength = Array.getLength(fieldValue);
          String arrayStr = fieldName + "=";
          for(int i = 0; i < fieldArrayLength; i++) {
            arrayStr += Array.get(fieldValue, i) + ",";
          }
          if(arrayStr.charAt(arrayStr.length() - 1) == ',') {
            arrayStr = arrayStr.substring(0, arrayStr.length() - 1);
          }
          
          sb.append(arrayStr).append(";");
        } else {
          sb.append(fieldName + "=" + fieldValue).append(";");
        }
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    
    return sb.toString();
  }
}
