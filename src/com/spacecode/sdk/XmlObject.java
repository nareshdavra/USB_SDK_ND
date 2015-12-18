package com.spacecode.sdk;


public class XmlObject {

   public static boolean notNegativeIndex(int ... indexes) {
      if(indexes == null) {
         return false;
      } else {
         int[] arr$ = indexes;
         int len$ = indexes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int index = arr$[i$];
            if(index == -1) {
               return false;
            }
         }

         return true;
      }
   }
}
