package com.cloudera.sa.node360.nodepuller.operations;

import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/3/15.
 */

//((ChannelExec) channel).setCommand("echo netstat----; netstat -s; echo ifconfig----; ifconfig; "
// "echo df----; df; echo top----; top -n 1 -b; echo cat.host----; cat /etc/hosts");

public abstract class AbstractSshExecOperation implements SshExecOperation{

  public abstract String getCommand();
  public abstract void processResults(String host, int port, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException;

  @Override
  public void execute(Session session, EventListener listener) {
    try {
      System.out.println("1");
      Channel channel = session.openChannel("exec");
      System.out.println("2");
      InputStream in = channel.getInputStream();
      System.out.println("3");

      //((ChannelExec)channel).setCommand("df");
      //((ChannelExec)channel).setCommand("top -n 1 -b");
      //((ChannelExec)channel).setCommand("ifconfig");
      //((ChannelExec)channel).setCommand("netstat -s");
      ((ChannelExec) channel).setCommand(getCommand());

      System.out.println("4");
      channel.connect();
      System.out.println("5");

      StringBuffer strBuffer = new StringBuffer();

      byte[] tmp = new byte[1024];
      while (true) {
        while (in.available() > 0) {
          int i = in.read(tmp, 0, 1024);
          if (i < 0) break;
          strBuffer.append(new String(tmp, 0, i));
        }
        if (channel.isClosed()) {
          if (in.available() > 0) continue;
          System.out.println("exit-status: " + channel.getExitStatus());
          break;
        }
        try {
          Thread.sleep(1000);
        } catch (Exception ee) {
        }
      }

      System.out.println("6");
      //channel.disconnect();

      String results = strBuffer.toString();


      processResults(session.getHost(), session.getPort(), results, listener);

    } catch (Exception e ){
      e.printStackTrace();
    }
  }
}
