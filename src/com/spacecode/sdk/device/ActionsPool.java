package com.spacecode.sdk.device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ActionsPool {

   private static ExecutorService SERVICE;


   public static synchronized ExecutorService getService() {
      if(SERVICE == null || SERVICE.isShutdown()) {
         SERVICE = Executors.newCachedThreadPool();
      }

      return SERVICE;
   }
}
