package com.cloudera.sa.node360.nodepuller;

import com.cloudera.sa.node360.nodepuller.listener.SystemOutListener;
import com.cloudera.sa.node360.nodepuller.operations.DfOperation;
import com.cloudera.sa.node360.nodepuller.operations.SshExecOperation;
import com.cloudera.sa.node360.nodepuller.operations.EtcChangesOperation;
import com.cloudera.sa.node360.nodepuller.operations.TopOperation;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted.malaska on 6/3/15.
 */
public class SshGrabberMain {
  static public void main(String[] args) throws JSchException, IOException, InterruptedException {
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    String username = args[2];
    String password = args[3];

    List<SshExecOperation> operationList = new ArrayList<SshExecOperation>();
    operationList.add(new DfOperation());
    operationList.add(new TopOperation());
    operationList.add(new EtcChangesOperation());

    SshConnection sshConn = new SshConnection(host, port, username, password, 5000,
            operationList,
            new SystemOutListener());
    sshConn.connect();
    sshConn.disconnect();
  }
}
