package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.data.DeviceStatus;
import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface BasicEventHandler extends DeviceEventHandler {

   void deviceDisconnected();

   void deviceStatusChanged(DeviceStatus var1);
}
