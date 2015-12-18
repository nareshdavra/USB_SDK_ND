package com.spacecode.sdk.network.alert;


public enum AlertType {

   DEVICE_DISCONNECTED("DEVICE_DISCONNECTED", 0),
   DOOR_OPEN_DELAY("DOOR_OPEN_DELAY", 1),
   TEMPERATURE("TEMPERATURE", 2),
   THIEF_FINGER("THIEF_FINGER", 3);
   // $FF: synthetic field
   private static final AlertType[] $VALUES = new AlertType[]{DEVICE_DISCONNECTED, DOOR_OPEN_DELAY, TEMPERATURE, THIEF_FINGER};


   private AlertType(String var1, int var2) {}

}
