package com.cloudera.sa.node360.model;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by ted.malaska on 6/8/15.
 */
public class NetFlowPojo  implements java.io.Serializable{
  String protocal;
  String sourceAddress;
  int sourcePort;
  String destinationAddress;
  int destinationPort;
  int numberOfPackets;
  int numberOfBytes;

  public NetFlowPojo(String protocal, String sourceAddress, int sourcePort, String destinationAddress, int destinationPort, int numberOfPackets, int numberOfBytes) {
    this.protocal = protocal;
    this.sourceAddress = sourceAddress;
    this.sourcePort = sourcePort;
    this.destinationAddress = destinationAddress;
    this.destinationPort = destinationPort;
    this.numberOfPackets = numberOfPackets;
    this.numberOfBytes = numberOfBytes;
  }

  public NetFlowPojo(String sourceNode, byte[] column, byte[] value) {
    String[] parts = StringUtils.split(Bytes.toString(column), ',');

    this.sourceAddress = sourceNode;

    protocal = parts[0];
    sourcePort = Integer.parseInt(parts[1]);
    destinationAddress = parts[2];
    destinationPort = Integer.parseInt(parts[3]);

    numberOfBytes = Integer.parseInt(Bytes.toString(value));
  }

  public String getProtocal() {
    return protocal;
  }

  public void setProtocal(String protocal) {
    this.protocal = protocal;
  }

  public String getSourceAddress() {
    return sourceAddress;
  }

  public void setSourceAddress(String sourceAddress) {
    this.sourceAddress = sourceAddress;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(int sourcePort) {
    this.sourcePort = sourcePort;
  }

  public String getDestinationAddress() {
    return destinationAddress;
  }

  public void setDestinationAddress(String destinationAddress) {
    this.destinationAddress = destinationAddress;
  }

  public int getDestinationPort() {
    return destinationPort;
  }

  public void setDestinationPort(int destinationPort) {
    this.destinationPort = destinationPort;
  }

  public int getNumberOfPackets() {
    return numberOfPackets;
  }

  public void setNumberOfPackets(int numberOfPackets) {
    this.numberOfPackets = numberOfPackets;
  }

  public int getNumberOfBytes() {
    return numberOfBytes;
  }

  public void setNumberOfBytes(int numberOfBytes) {
    this.numberOfBytes = numberOfBytes;
  }

  public String generateColumn() {
    return protocal + ',' +
            sourcePort + ',' +
            destinationAddress + ',' +
            destinationPort;

  }

  @Override
  public String toString() {
    return "NetFlowPojo{" +
            "protocal='" + protocal + '\'' +
            ", sourceAddress='" + sourceAddress + '\'' +
            ", sourcePort=" + sourcePort +
            ", destinationAddress='" + destinationAddress + '\'' +
            ", destinationPort=" + destinationPort +
            ", numberOfPackets=" + numberOfPackets +
            ", numberOfBytes=" + numberOfBytes +
            '}';
  }
}
