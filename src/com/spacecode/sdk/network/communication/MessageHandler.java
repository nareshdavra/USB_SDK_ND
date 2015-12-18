package com.spacecode.sdk.network.communication;

import java.util.Arrays;

public final class MessageHandler {

   public static final char DELIMITER = '\u001c';
   public static final char END_OF_MESSAGE = '\u0004';


   public static String packetsToFullMessage(String ... packets) {
      if(packets != null && packets.length != 0 && !Arrays.asList(packets).contains((Object)null)) {
         StringBuilder sb = new StringBuilder(packets[0]);

         for(int i = 1; i < packets.length; ++i) {
            sb.append('\u001c');
            sb.append(packets[i]);
         }

         sb.append('\u0004');
         return sb.toString();
      } else {
         return null;
      }
   }
}
