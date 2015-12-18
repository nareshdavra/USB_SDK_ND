package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface AccessModuleEventHandler extends DeviceEventHandler {

   void badgeReaderConnected(boolean var1);

   void badgeReaderDisconnected(boolean var1);

   void badgeScanned(String var1);

   void fingerTouched(boolean var1);

   void fingerprintEnrollmentSample(byte var1);
}
