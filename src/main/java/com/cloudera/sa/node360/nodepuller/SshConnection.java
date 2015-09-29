package com.cloudera.sa.node360.nodepuller;

import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.cloudera.sa.node360.nodepuller.operations.SshExecOperation;
import com.jcraft.jsch.*;

import java.io.IOException;
import java.util.List;

public class SshConnection {

  private String host;
  private int port;
  private String username;
  private String password;
  private List<SshExecOperation> operationList;
  private long waitTime;

  private Session session = null;
  private EventListener listener = null;


  public SshConnection(String host, int port, String username,
                       String password, long waitTime,
                       List<SshExecOperation> operationList,
                       EventListener listener) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.waitTime = waitTime;
    this.operationList = operationList;
    this.listener = listener;
  }

  public void connect() throws JSchException, IOException, InterruptedException {
    JSch jsch=new JSch();
    session=jsch.getSession(username, host, 22);
    session.setPassword(password);

    UserInfo ui = new MyUserInfo(){

      public boolean promptYesNo(String message){
        return true;
      }

      // If password is not given before the invocation of Session#connect(),
      // implement also following methods,
      //   * UserInfo#getPassword(),
      //   * UserInfo#promptPassword(String message) and
      //   * UIKeyboardInteractive#promptKeyboardInteractive()

    };

    session.setUserInfo(ui);

    session.connect(30000);

    while (true) {
      for (SshExecOperation operation: operationList) {
        operation.execute(session, listener);
      }
      Thread.sleep(waitTime);
    }
  }

  public void disconnect() {
    session.disconnect();
  }

  public static abstract class MyUserInfo
          implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return null; }
    public boolean promptYesNo(String str){ return false; }
    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return false; }
    public boolean promptPassword(String message){ return false; }
    public void showMessage(String message){ }
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      return null;
    }
  }
}
