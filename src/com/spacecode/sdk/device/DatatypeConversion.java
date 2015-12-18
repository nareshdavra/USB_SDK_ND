package com.spacecode.sdk.device;


class DatatypeConversion {

   private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();


   static short[] stringToShortArray(String messagePackets) {
      byte[] asByteArray = parseHexBinary(messagePackets);
      short[] result = new short[asByteArray.length];

      for(int i = 0; i < asByteArray.length; ++i) {
         result[i] = (short)(asByteArray[i] & 255);
      }

      return result;
   }

   static String shortArrayToString(short[] messagePackets) {
      byte[] asByteArray = new byte[messagePackets.length];

      for(int i = 0; i < messagePackets.length; ++i) {
         asByteArray[i] = (byte)messagePackets[i];
      }

      return printHexBinary(asByteArray);
   }

   static byte[] stringToByteArray(String messagePackets) {
      return parseHexBinary(messagePackets);
   }

   static byte[] parseHexBinary(String lexicalXSDHexBinary) {
      int len = lexicalXSDHexBinary.length();
      if(len % 2 != 0) {
         throw new IllegalArgumentException("hexBinary needs to be even-length: " + lexicalXSDHexBinary);
      } else {
         byte[] out = new byte[len / 2];

         for(int i = 0; i < len; i += 2) {
            int h = hexToBin(lexicalXSDHexBinary.charAt(i));
            int l = hexToBin(lexicalXSDHexBinary.charAt(i + 1));
            if(h == -1 || l == -1) {
               throw new IllegalArgumentException("contains illegal character for hexBinary: " + lexicalXSDHexBinary);
            }

            out[i / 2] = (byte)(h * 16 + l);
         }

         return out;
      }
   }

   private static int hexToBin(char ch) {
      return 48 <= ch && ch <= 57?ch - 48:(65 <= ch && ch <= 70?ch - 65 + 10:(97 <= ch && ch <= 102?ch - 97 + 10:-1));
   }

   static String printHexBinary(byte[] data) {
      StringBuilder r = new StringBuilder(data.length * 2);
      byte[] arr$ = data;
      int len$ = data.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte b = arr$[i$];
         r.append(HEX_CODE[b >> 4 & 15]);
         r.append(HEX_CODE[b & 15]);
      }

      return r.toString();
   }

}
