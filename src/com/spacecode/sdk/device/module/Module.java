package com.spacecode.sdk.device.module;

import com.spacecode.sdk.device.Device;

public abstract class Module {

   private final String _serialNumber;
   protected final Device _listeningDevice;
   protected Device.EventDispatcher _deviceEventDispatcher;


   protected Module(String serialNumber, Device listeningDevice) {
      this(serialNumber, listeningDevice, (Device.EventDispatcher)null);
   }

   protected Module(String serialNumber, Device listeningDevice, Device.EventDispatcher eventDispatcher) {
      this._serialNumber = serialNumber;
      this._listeningDevice = listeningDevice;
      this._deviceEventDispatcher = eventDispatcher;
   }

   public String getSerialNumber() {
      return this._serialNumber;
   }

   public abstract void release();
}
