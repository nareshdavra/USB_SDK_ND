package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface MaintenanceEventHandler extends DeviceEventHandler {

   void flashingProgress(int var1, int var2);

   void correlationSample(int var1, int var2);

   void correlationSampleSeries(short[] var1, short[] var2);
}
