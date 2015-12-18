package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;

public interface DoorEventHandler extends DeviceEventHandler {

   void scanCancelledByDoor();

   void doorOpened();

   void doorClosed();

   void doorOpenDelay();
}
