package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;
import com.spacecode.sdk.network.alert.Alert;

public interface AlertEventHandler extends DeviceEventHandler {

   void alertRaised(Alert var1, String var2);
}
