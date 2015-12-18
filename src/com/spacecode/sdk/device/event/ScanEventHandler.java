package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface ScanEventHandler extends DeviceEventHandler {

   void scanStarted();

   void scanCompleted();

   void tagAdded(String var1);

   void scanFailed();

   void scanCancelledByHost();
}
