package com.cloudera.sa.node360.model;

import org.apache.commons.lang.StringUtils;

/**
 * Created by ted.malaska on 6/8/15.
 */
public class NotificationPojo {
  long timeOfNotification;
  String notificationMessage;
  long timeToAlert;

  public NotificationPojo(long timeOfNotification, String notificationMessage, long timeToAlert) {
    this.timeOfNotification = timeOfNotification;
    this.notificationMessage = notificationMessage;
    this.timeToAlert = timeToAlert;
  }

  public NotificationPojo(String value) {
    String[] parts = StringUtils.split(value, '|');
    timeOfNotification = Long.parseLong(parts[0]);
    timeToAlert = Long.parseLong(parts[1]);
    notificationMessage = parts[2];
  }

  public long getTimeOfNotification() { return timeOfNotification; }

  public void setTimeOfNotification(long timeOfNotification) { this.timeOfNotification = timeOfNotification; }

  public String getNotificationMessage() {
    return notificationMessage;
  }

  public void setNotificationMessage(String notificationMessage) {
    this.notificationMessage = notificationMessage;
  }

  public long getTimeToAlert() {
    return timeToAlert;
  }

  public void setTimeToAlert(long timeToAlert) {
    this.timeToAlert = timeToAlert;
  }

  public String toString() {
    return timeOfNotification + "|" + timeToAlert + "|" + notificationMessage;
  }
}
