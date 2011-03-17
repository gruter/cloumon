package org.cloumon.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionUtils {
  public static String toString(List<? extends Object> list) {
    if (list == null || list.size() == 0) {
      return "";
    }
    StringBuilder sb = new  StringBuilder();
    
    for (Object eachValue: list) {
      sb.append(eachValue.toString()).append(",");
    }
    
    String result = sb.toString();
    return result.substring(0, result.getBytes().length - 1);
  }

  public static List<String> toList(String str) {
    List<String> result = new ArrayList<String>();
    
    if (str == null) {
      return result;
    }

    for (String eachToken: str.split(",")) {
      result.add(eachToken);
    }
    return result;
  }

  /**
   * 첫번째와 두번째 파라미터의 Collection을 비교하여 첫번째에서 삭제된 객체, 두번째에 추가된 객체를 찾는다. 
   * @param origin
   * @param current
   * @param added
   * @param removed
   */
  public static void compareList(List<? extends Object> origin, List<? extends Object> current,
      List<Object> added, List<Object> removed) {
    if (origin == null) {
      if (current != null) {
        added.addAll(current);
      }
      return;
    }
    
    if (current == null) {
      if (origin != null) {
        removed.addAll(origin);
      }
      return;
    }
    
    Set<Object> originSet = new HashSet<Object>(origin);
    
    for(Object eachValue: current) {
      if(!originSet.remove(eachValue)) {
        added.add(eachValue);
      }
    }
    
    removed.addAll(originSet);
  }  
}
