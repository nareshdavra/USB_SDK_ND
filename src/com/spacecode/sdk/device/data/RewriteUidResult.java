package com.spacecode.sdk.device.data;


public enum RewriteUidResult {

   ERROR("ERROR", 0),
   TAG_NOT_DETECTED("TAG_NOT_DETECTED", 1),
   TAG_NOT_CONFIRMED("TAG_NOT_CONFIRMED", 2),
   TAG_BLOCKED_OR_NOT_SUPPLIED("TAG_BLOCKED_OR_NOT_SUPPLIED", 3),
   TAG_BLOCKED("TAG_BLOCKED", 4),
   TAG_NOT_SUPPLIED("TAG_NOT_SUPPLIED", 5),
   WRITING_CONFIRMATION_FAILED("WRITING_CONFIRMATION_FAILED", 6),
   WRITING_SUCCESS("WRITING_SUCCESS", 7),
   NEW_UID_INVALID("NEW_UID_INVALID", 8);
   // $FF: synthetic field
   private static final RewriteUidResult[] $VALUES = new RewriteUidResult[]{ERROR, TAG_NOT_DETECTED, TAG_NOT_CONFIRMED, TAG_BLOCKED_OR_NOT_SUPPLIED, TAG_BLOCKED, TAG_NOT_SUPPLIED, WRITING_CONFIRMATION_FAILED, WRITING_SUCCESS, NEW_UID_INVALID};


   private RewriteUidResult(String var1, int var2) {}

}