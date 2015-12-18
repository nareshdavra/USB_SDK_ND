package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

final class CRC {

   public static final byte SIZE = 4;
   private int _ucCrcHi = 255;
   private int _ucCrcLo = 255;
   private static final char[] CRC_HIGH_LOOKUP_TABLE = new char[]{'\u0000', '\u0010', ' ', '0', '@', 'P', '`', 'p', '\u0081', '\u0091', '\u00a1', '\u00b1', '\u00c1', '\u00d1', '\u00e1', '\u00f1', '\u0012', '\u0002', '2', '\"', 'R', 'B', 'r', 'b', '\u0093', '\u0083', '\u00b3', '\u00a3', '\u00d3', '\u00c3', '\u00f3', '\u00e3', '$', '4', '\u0004', '\u0014', 'd', 't', 'D', 'T', '\u00a5', '\u00b5', '\u0085', '\u0095', '\u00e5', '\u00f5', '\u00c5', '\u00d5', '6', '&', '\u0016', '\u0006', 'v', 'f', 'V', 'F', '\u00b7', '\u00a7', '\u0097', '\u0087', '\u00f7', '\u00e7', '\u00d7', '\u00c7', 'H', 'X', 'h', 'x', '\b', '\u0018', '(', '8', '\u00c9', '\u00d9', '\u00e9', '\u00f9', '\u0089', '\u0099', '\u00a9', '\u00b9', 'Z', 'J', 'z', 'j', '\u001a', '\n', ':', '*', '\u00db', '\u00cb', '\u00fb', '\u00eb', '\u009b', '\u008b', '\u00bb', '\u00ab', 'l', '|', 'L', '\\', ',', '<', '\f', '\u001c', '\u00ed', '\u00fd', '\u00cd', '\u00dd', '\u00ad', '\u00bd', '\u008d', '\u009d', '~', 'n', '^', 'N', '>', '.', '\u001e', '\u000e', '\u00ff', '\u00ef', '\u00df', '\u00cf', '\u00bf', '\u00af', '\u009f', '\u008f', '\u0091', '\u0081', '\u00b1', '\u00a1', '\u00d1', '\u00c1', '\u00f1', '\u00e1', '\u0010', '\u0000', '0', ' ', 'P', '@', 'p', '`', '\u0083', '\u0093', '\u00a3', '\u00b3', '\u00c3', '\u00d3', '\u00e3', '\u00f3', '\u0002', '\u0012', '\"', '2', 'B', 'R', 'b', 'r', '\u00b5', '\u00a5', '\u0095', '\u0085', '\u00f5', '\u00e5', '\u00d5', '\u00c5', '4', '$', '\u0014', '\u0004', 't', 'd', 'T', 'D', '\u00a7', '\u00b7', '\u0087', '\u0097', '\u00e7', '\u00f7', '\u00c7', '\u00d7', '&', '6', '\u0006', '\u0016', 'f', 'v', 'F', 'V', '\u00d9', '\u00c9', '\u00f9', '\u00e9', '\u0099', '\u0089', '\u00b9', '\u00a9', 'X', 'H', 'x', 'h', '\u0018', '\b', '8', '(', '\u00cb', '\u00db', '\u00eb', '\u00fb', '\u008b', '\u009b', '\u00ab', '\u00bb', 'J', 'Z', 'j', 'z', '\n', '\u001a', '*', ':', '\u00fd', '\u00ed', '\u00dd', '\u00cd', '\u00bd', '\u00ad', '\u009d', '\u008d', '|', 'l', '\\', 'L', '<', ',', '\u001c', '\f', '\u00ef', '\u00ff', '\u00cf', '\u00df', '\u00af', '\u00bf', '\u008f', '\u009f', 'n', '~', 'N', '^', '.', '>', '\u000e', '\u001e'};
   private static final char[] CRC_LOW_LOOKUP_TABLE = new char[]{'\u0000', '!', 'B', 'c', '\u0084', '\u00a5', '\u00c6', '\u00e7', '\b', ')', 'J', 'k', '\u008c', '\u00ad', '\u00ce', '\u00ef', '1', '\u0010', 's', 'R', '\u00b5', '\u0094', '\u00f7', '\u00d6', '9', '\u0018', '{', 'Z', '\u00bd', '\u009c', '\u00ff', '\u00de', 'b', 'C', ' ', '\u0001', '\u00e6', '\u00c7', '\u00a4', '\u0085', 'j', 'K', '(', '\t', '\u00ee', '\u00cf', '\u00ac', '\u008d', 'S', 'r', '\u0011', '0', '\u00d7', '\u00f6', '\u0095', '\u00b4', '[', 'z', '\u0019', '8', '\u00df', '\u00fe', '\u009d', '\u00bc', '\u00c4', '\u00e5', '\u0086', '\u00a7', '@', 'a', '\u0002', '#', '\u00cc', '\u00ed', '\u008e', '\u00af', 'H', 'i', '\n', '+', '\u00f5', '\u00d4', '\u00b7', '\u0096', 'q', 'P', '3', '\u0012', '\u00fd', '\u00dc', '\u00bf', '\u009e', 'y', 'X', ';', '\u001a', '\u00a6', '\u0087', '\u00e4', '\u00c5', '\"', '\u0003', '`', 'A', '\u00ae', '\u008f', '\u00ec', '\u00cd', '*', '\u000b', 'h', 'I', '\u0097', '\u00b6', '\u00d5', '\u00f4', '\u0013', '2', 'Q', 'p', '\u009f', '\u00be', '\u00dd', '\u00fc', '\u001b', ':', 'Y', 'x', '\u0088', '\u00a9', '\u00ca', '\u00eb', '\f', '-', 'N', 'o', '\u0080', '\u00a1', '\u00c2', '\u00e3', '\u0004', '%', 'F', 'g', '\u00b9', '\u0098', '\u00fb', '\u00da', '=', '\u001c', '\u007f', '^', '\u00b1', '\u0090', '\u00f3', '\u00d2', '5', '\u0014', 'w', 'V', '\u00ea', '\u00cb', '\u00a8', '\u0089', 'n', 'O', ',', '\r', '\u00e2', '\u00c3', '\u00a0', '\u0081', 'f', 'G', '$', '\u0005', '\u00db', '\u00fa', '\u0099', '\u00b8', '_', '~', '\u001d', '<', '\u00d3', '\u00f2', '\u0091', '\u00b0', 'W', 'v', '\u0015', '4', 'L', 'm', '\u000e', '/', '\u00c8', '\u00e9', '\u008a', '\u00ab', 'D', 'e', '\u0006', '\'', '\u00c0', '\u00e1', '\u0082', '\u00a3', '}', '\\', '?', '\u001e', '\u00f9', '\u00d8', '\u00bb', '\u009a', 'u', 'T', '7', '\u0016', '\u00f1', '\u00d0', '\u00b3', '\u0092', '.', '\u000f', 'l', 'M', '\u00aa', '\u008b', '\u00e8', '\u00c9', '&', '\u0007', 'd', 'E', '\u00a2', '\u0083', '\u00e0', '\u00c1', '\u001f', '>', ']', '|', '\u009b', '\u00ba', '\u00d9', '\u00f8', '\u0017', '6', 'U', 't', '\u0093', '\u00b2', '\u00d1', '\u00f0'};


   public String getCRC() {
      return this.getCRC(true);
   }

   public String getCRC(boolean bitComplement) {
      int crc = this._ucCrcLo << 8 | this._ucCrcHi;
      return bitComplement?String.format("%04X", new Object[]{Integer.valueOf('\uffff' & ~crc)}).toUpperCase():String.format("%04X", new Object[]{Integer.valueOf('\uffff' & crc)}).toUpperCase();
   }

   public static String computeCRC(String message) {
      CRC crcCalculator = new CRC();

      try {
         crcCalculator.bufferize(message.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var3) {
         SmartLogger.getLogger().log(Level.SEVERE, "Unable to use UTF-8 as charset for CRC encoding", var3);
         return "";
      }

      return crcCalculator.getCRC();
   }

   public void bufferize(byte[] buffer) {
      byte[] arr$ = buffer;
      int len$ = buffer.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte aBuffer = arr$[i$];
         int uLutIndex = this._ucCrcHi ^ aBuffer & 255;
         this._ucCrcHi = CRC_HIGH_LOOKUP_TABLE[uLutIndex] ^ this._ucCrcLo;
         this._ucCrcLo = CRC_LOW_LOOKUP_TABLE[uLutIndex];
      }

   }

   public static boolean isCRCValid(String message) {
      if(message.length() <= 4) {
         return false;
      } else {
         String crc = message.substring(message.length() - 4);
         String messageNoCRC = message.substring(0, message.length() - 4);
         return crc.equals(computeCRC(messageNoCRC));
      }
   }

   public static byte computeDigitGroupCRC(byte[] uid, int groupIndex) {
      return (byte)(7 - (uid[groupIndex] + uid[groupIndex + 1] + uid[groupIndex + 2] + uid[groupIndex + 3] + uid[groupIndex + 4] & 7));
   }

}
