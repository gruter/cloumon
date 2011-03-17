package org.cloumon.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONObject;

public abstract class ExecPlugin {
  public final int statusOK = 100;
  public final int statusKO = -100;

  Process process = null;

  public abstract String getCmde();

  public ExecPlugin() {
  }

  public void stop() {
    if (process != null)
      process.destroy();
  }

  public int waitFor() throws InterruptedException {
    return process.waitFor();
  }

  public JSONObject postProcess(JSONObject execResult) {
    return execResult;
  }

  public JSONObject execute() {
    JSONObject result = new JSONObject();
    try {
      result.put("timestamp", System.currentTimeMillis());

      Runtime runtime = Runtime.getRuntime();
      String cmd = getCmde();
      String[] cmds = null;
      if(System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
        cmds = new String[]{cmd};
      } else {
        cmds = new String[]{"/bin/sh", "-c", cmd};
      }
      process = runtime.exec(cmds);
      OutputReader stdOut = new OutputReader(process, Output.stdOut);
      stdOut.start();
      OutputReader stdErr = new OutputReader(process, Output.stdErr);
      stdErr.start();
      int exitValue = process.waitFor();
      stdOut.join();
      stdErr.join();
      process.getInputStream().close(); // otherwise this implicitly stays open
      result.put("exitValue", exitValue);
      result.put("stdout", stdOut.output.toString());
      result.put("stderr", stdErr.output.toString());
      result.put("status", statusOK);
      process.getOutputStream().close();
      process.getErrorStream().close();
    } catch (Throwable e) {
      try {
        result.put("status", statusKO);
        result.put("errorLog", e.getMessage());
        if (e.getMessage().contains("Too many open files")) {
          // maybe die abruptly? Error is ir-recoverable and runtime can reboot
          // us.
          // System.exit(1);
        }
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      e.printStackTrace();
    }

    return postProcess(result);
  }
}

enum Output {
  stdOut, stdErr
};

class OutputReader extends Thread {
  private Process process = null;
  private Output outputType = null;
  public StringBuilder output = new StringBuilder();
  public boolean isOk = true;

  public OutputReader(Process process, Output outputType) {
    this.process = process;
    this.outputType = outputType;
  }

  public void run() {
    try {
      String line = null;
      InputStream is = null;
      switch (this.outputType) {
      case stdOut:
        is = process.getInputStream();
        break;
      case stdErr:
        is = process.getErrorStream();
        break;

      }

      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      while ((line = br.readLine()) != null) {
        //System.out.println("========>>>>>>>[" + line + "]");
        output.append(line).append("\n");
      }
      br.close();
    } catch (IOException e) {
      isOk = false;
      e.printStackTrace();
    } catch (Throwable e) {
      isOk = false;
      e.printStackTrace();
    }
  }
}