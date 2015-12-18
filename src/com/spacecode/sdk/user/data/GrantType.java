package com.spacecode.sdk.user.data;


public enum GrantType {

   UNDEFINED("UNDEFINED", 0),
   SLAVE("SLAVE", 1),
   MASTER("MASTER", 2),
   ALL("ALL", 3);
   // $FF: synthetic field
   private static final GrantType[] $VALUES = new GrantType[]{UNDEFINED, SLAVE, MASTER, ALL};


   private GrantType(String var1, int var2) {}

}
