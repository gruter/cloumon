package org.cloumon.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class ReverseFileInputStream extends InputStream {
  private final byte[] buffer = new byte[4096];
  private final RandomAccessFile raf;
  private long currentPositionInFile;
  private int currentPositionInBuffer;

  public ReverseFileInputStream(File file) throws IOException {
    assert (file != null) && file.exists() && file.isFile() && file.canRead();

    raf = new RandomAccessFile(file, "r");
    currentPositionInFile = raf.length();
    currentPositionInBuffer = 0;
  }

  public int read() throws IOException {
    if (currentPositionInFile <= 0) {
        return -1;
    }
    if (--currentPositionInBuffer < 0) {
        currentPositionInBuffer = buffer.length;
        long startOfBlock = currentPositionInFile - buffer.length;
        if (startOfBlock < 0)
        {
            currentPositionInBuffer = buffer.length + (int)startOfBlock;
            startOfBlock = 0;
        }
        raf.seek(startOfBlock);
        raf.readFully(buffer, 0, currentPositionInBuffer);
        return read();
    }
    currentPositionInFile--;
    return buffer[currentPositionInBuffer];
  }

  public void close() throws IOException {
    raf.close();
  }
}
