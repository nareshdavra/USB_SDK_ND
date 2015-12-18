package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.data.DeviceStatus;
import com.spacecode.sdk.device.event.AccessControlEventHandler;
import com.spacecode.sdk.device.event.AccessModuleEventHandler;
import com.spacecode.sdk.device.event.AlertEventHandler;
import com.spacecode.sdk.device.event.BasicEventHandler;
import com.spacecode.sdk.device.event.DoorEventHandler;
import com.spacecode.sdk.device.event.LedEventHandler;
import com.spacecode.sdk.device.event.ScanEventHandler;
import com.spacecode.sdk.device.event.TemperatureEventHandler;
import com.spacecode.sdk.network.alert.Alert;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.data.AccessType;
import java.util.List;

public abstract class AbstractEventHandler implements AccessControlEventHandler, AccessModuleEventHandler, AlertEventHandler, BasicEventHandler, DoorEventHandler, LedEventHandler, ScanEventHandler, TemperatureEventHandler {

   public void scanStarted() {}

   public void scanCompleted() {}

   public void tagAdded(String tagUid) {}

   public void scanFailed() {}

   public void scanCancelledByHost() {}

   public void authenticationSuccess(User user, AccessType accessType, boolean isMaster) {}

   public void authenticationFailure(User user, AccessType accessType, boolean isMaster) {}

   public void badgeReaderConnected(boolean isMaster) {}

   public void badgeReaderDisconnected(boolean isMaster) {}

   public void badgeScanned(String badgeNumber) {}

   public void fingerTouched(boolean isMaster) {}

   public void fingerprintEnrollmentSample(byte sampleNumber) {}

   public void alertRaised(Alert alert, String extraData) {}

   public void deviceDisconnected() {}

   public void deviceStatusChanged(DeviceStatus status) {}

   public void scanCancelledByDoor() {}

   public void doorOpened() {}

   public void doorClosed() {}

   public void doorOpenDelay() {}

   public void lightingStarted(List tagsLeft) {}

   public void lightingStopped() {}

   public void temperatureMeasure(double value) {}
}
