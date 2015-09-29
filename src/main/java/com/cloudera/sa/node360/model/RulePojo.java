package com.cloudera.sa.node360.model;

import org.apache.commons.lang.StringUtils;

/**
 * Created by ted.malaska on 6/8/15.
 */
public class RulePojo  implements java.io.Serializable{
  String ruleId;
  String sourcePort;
  String destinationPort;
  String destinationIp;

  public RulePojo(String ruleId, String sourcePort, String destinationPort, String destinationIp) {
    this.ruleId = ruleId;
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;
    this.destinationIp = destinationIp;
    if (this.sourcePort == null || this.sourcePort.equals("null")) {this.sourcePort = null;}
    if (this.destinationPort == null || this.destinationPort.equals("null")) {this.destinationPort = null;}
    if (this.destinationIp == null || this.destinationIp.equals("null")) {this.destinationIp = null;}
  }

  public RulePojo(String ruleId, String value) {
    this.ruleId = ruleId;

    String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(value, ",");
    sourcePort = parts[0];
    if (sourcePort.isEmpty() || sourcePort.equals("null")) {sourcePort = "";}

    destinationPort = parts[1];
    if (destinationPort.isEmpty() || destinationPort.equals("null")) {destinationPort = "";}

    destinationIp = parts[2];
    if (destinationIp.isEmpty() || destinationIp.equals("null")) {destinationIp = "";}
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public String getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(String sourcePort) {
    this.sourcePort = sourcePort;
  }

  public String getDestinationPort() {
    return destinationPort;
  }

  public void setDestinationPort(String destinationPort) {
    this.destinationPort = destinationPort;
  }

  public String getDestinationIp() {
    return destinationIp;
  }

  public void setDestinationIp(String destinationIp) {
    this.destinationIp = destinationIp;
  }

  public String toString() {
    return sourcePort  + "," +  destinationPort + "," + destinationIp;
  }

  public String toPrettyString() {
    return "RulePojo{" +
            "ruleId=" + ruleId +
            "sourcePort=" + sourcePort +
            ", destinationPort=" + destinationPort +
            ", destinationIp=" + destinationIp +
            '}';
  }

  public NotificationPojo runRule(String node, long timeStamp, String group, NetFlowPojo netFlowPojo) {
    boolean fireNotification = true;
    if (!sourcePort.isEmpty()) {
      if (!sourcePort.equals(netFlowPojo.getSourcePort())) {
        return null;
      }
    }
    if (!destinationPort.isEmpty()) {
      if (!destinationPort.equals(netFlowPojo.getDestinationPort())) {
        return null;
      }
    }
    if (!destinationIp.isEmpty()) {
      if (!destinationIp.equals(netFlowPojo.getDestinationAddress())) {
        return null;
      }
    }

    if (fireNotification) {
      return new NotificationPojo(System.currentTimeMillis(), "node: " + node + " " + this.toPrettyString() + " - " + netFlowPojo.toString(), System.currentTimeMillis() - timeStamp);
    } else {
      return null;
    }
  }
}
