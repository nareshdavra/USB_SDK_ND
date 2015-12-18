package com.spacecode.sdk.device.data;


public enum DeviceType {

   UNKNOWN("UNKNOWN", 0),
   SAS("SAS", 1),
   SMARTBOARD("SMARTBOARD", 2),
   SMARTBOX("SMARTBOX", 3),
   SMARTCABINET("SMARTCABINET", 4),
   SMARTDRAWER("SMARTDRAWER", 5),
   SMARTFRIDGE("SMARTFRIDGE", 6),
   SMARTPAD("SMARTPAD", 7),
   SMARTRACK("SMARTRACK", 8),
   SMARTSTATION("SMARTSTATION", 9);
   // $FF: synthetic field
   private static final DeviceType[] $VALUES = new DeviceType[]{UNKNOWN, SAS, SMARTBOARD, SMARTBOX, SMARTCABINET, SMARTDRAWER, SMARTFRIDGE, SMARTPAD, SMARTRACK, SMARTSTATION};


   private DeviceType(String var1, int var2) {}

}
