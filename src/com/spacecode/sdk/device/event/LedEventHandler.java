package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;
import java.util.List;

public interface LedEventHandler extends DeviceEventHandler {

   void lightingStarted(List var1);

   void lightingStopped();
}
