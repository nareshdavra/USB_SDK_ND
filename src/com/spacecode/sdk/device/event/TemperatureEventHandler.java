package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface TemperatureEventHandler extends DeviceEventHandler {

   void temperatureMeasure(double var1);
}
