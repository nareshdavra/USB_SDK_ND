package com.spacecode.sdk.device;


public class DeviceCreationException extends Exception {

   public DeviceCreationException(String message, Exception initialException) {
      super(message, initialException);
   }

   public DeviceCreationException(String message) {
      super(message);
   }
}
