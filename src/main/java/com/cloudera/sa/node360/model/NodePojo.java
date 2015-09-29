package com.cloudera.sa.node360.model;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class NodePojo {
  String ipAddress;
  String name;
  String zipCode;

  String group;

  public NodePojo(String ipAddress, String name, String zipCode, String group) {
    this.ipAddress = ipAddress;
    this.name = name;
    this.zipCode = zipCode;
    this.group = group;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String toString() {
    return "ipAddress:" + ipAddress +
            ",name:" + name +
            ",zipCode:" + zipCode +
            ",group:" + group;
  }
}
