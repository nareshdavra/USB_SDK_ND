package com.spacecode.sdk.device.data;

import com.spacecode.sdk.device.data.DeviceType;

public final class PluggedDevice {

   private final String _serialNumber;
   private final String _serialPort;
   private final String _softwareVersion;
   private final String _hardwareVersion;
   private final DeviceType _deviceType;


   public PluggedDevice(String serialNumber, String serialPort, String softwareVersion, String hardwareVersion, DeviceType deviceType) {
      this._serialNumber = serialNumber;
      this._serialPort = serialPort;
      this._softwareVersion = softwareVersion;
      this._hardwareVersion = hardwareVersion;
      this._deviceType = deviceType;
   }

   public String getSerialNumber() {
      return this._serialNumber;
   }

   public String getSerialPort() {
      return this._serialPort;
   }

   public String getSoftwareVersion() {
      return this._softwareVersion;
   }

   public String getHardwareVersion() {
      return this._hardwareVersion;
   }

   public DeviceType getDeviceType() {
      return this._deviceType;
   }
}
