package com.cloudera.sa.node360.nodepuller.operations;

import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.jcraft.jsch.Session;

/**
 * Created by ted.malaska on 6/3/15.
 */
public interface SshExecOperation {
  public void execute(Session session, EventListener listener);
}
