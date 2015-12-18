package com.spacecode.sdk.device.data;


public enum DeviceStatus {

   NOT_READY("NOT_READY", 0),
   READY("READY", 1),
   DOOR_OPEN("DOOR_OPEN", 2),
   DOOR_CLOSED("DOOR_CLOSED", 3),
   SCANNING("SCANNING", 4),
   WAIT_MODE("WAIT_MODE", 5),
   ERROR("ERROR", 6),
   FLASHING_FIRMWARE("FLASHING_FIRMWARE", 7),
   LED_ON("LED_ON", 8),
   ENROLLING("ENROLLING", 9);
   // $FF: synthetic field
   private static final DeviceStatus[] $VALUES = new DeviceStatus[]{NOT_READY, READY, DOOR_OPEN, DOOR_CLOSED, SCANNING, WAIT_MODE, ERROR, FLASHING_FIRMWARE, LED_ON, ENROLLING};


   private DeviceStatus(String var1, int var2) {}

}
