package com.spacecode.sdk.user.data;


public enum AccessType {

   UNDEFINED("UNDEFINED", 0),
   BADGE("BADGE", 1),
   FINGERPRINT("FINGERPRINT", 2);
   // $FF: synthetic field
   private static final AccessType[] $VALUES = new AccessType[]{UNDEFINED, BADGE, FINGERPRINT};


   private AccessType(String var1, int var2) {}

}
