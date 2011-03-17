package org.cloumon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class FileHeadTail {
  public static List<String> head(File file, int numberOfLinesToRead) throws IOException {
    return head(file, "ISO-8859-1", numberOfLinesToRead);
  }

  public static List<String> head(File file, String encoding, int numberOfLinesToRead) throws IOException {
    assert (file != null) && file.exists() && file.isFile() && file.canRead();
    assert numberOfLinesToRead > 0;
    assert encoding != null;

    LinkedList<String> lines = new LinkedList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
    for (String line = null; (numberOfLinesToRead-- > 0) && (line = reader.readLine()) != null;) {
      lines.addLast(line);
    }
    reader.close();
    return lines;
  }

  public static List<String> tail(File file, int numberOfLinesToRead) throws IOException {
    return tail(file, "ISO-8859-1", numberOfLinesToRead);
  }

  public static List<String> tail(File file, String encoding, int numberOfLinesToRead) throws IOException {
    assert (file != null) && file.exists() && file.isFile() && file.canRead();
    assert numberOfLinesToRead > 0;
    assert (encoding != null) && encoding.matches("(?i)(iso-8859|ascii|us-ascii).*");

    LinkedList<String> lines = new LinkedList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new ReverseFileInputStream(file), encoding));
    for (String line = null; (numberOfLinesToRead-- > 0) && (line = reader.readLine()) != null;) {
      // Reverse the order of the characters in the string
      char[] chars = line.toCharArray();
      for (int j = 0, k = chars.length - 1; j < k; j++, k--) {
        char temp = chars[j];
        chars[j] = chars[k];
        chars[k] = temp;
      }
      lines.addFirst(new String(chars));
    }
    reader.close();
    return lines;
  }
}
