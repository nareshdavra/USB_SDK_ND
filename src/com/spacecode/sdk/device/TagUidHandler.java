package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.CRC;
import com.spacecode.sdk.device.DatatypeConversion;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

final class TagUidHandler {

   private static final byte UID_LENGTH_BITS = 60;
   private static final byte DIGIT_LENGTH_BITS = 3;
   private static final byte UID_LENGTH_STRING = 10;
   private static final byte UID_FULL_MEM_BITS = 42;
   public static final String SPCE2_BLANK_PREFIX = "@";
   public static final char SPCE2_FULL_MEM_EOF = '\u00a7';
   private static final char[] SPCE2_CONVERSION_TABLE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', '@', '@', '@', '@', '@', '@', '@', '@', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '-', '/', '.', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '@', '\u00a7'};
   private static final List SPCE2_CONVERSION_LIST = initializeAlphanumConversionList();


   private static List initializeAlphanumConversionList() {
      ArrayList characterList = new ArrayList();
      char[] arr$ = SPCE2_CONVERSION_TABLE;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         characterList.add(Character.valueOf(c));
      }

      return characterList;
   }

   private static String getOctalUidFromHexBytes(String rawTagUID, int digitNumber) {
      if(rawTagUID != null && !"".equals(rawTagUID.trim()) && rawTagUID.length() % 2 == 0) {
         byte[] tagIdBytes = DatatypeConversion.parseHexBinary(rawTagUID);
         long uidBits = (new BigInteger(1, tagIdBytes)).longValue();
         byte bitShifting = 60;
         StringBuilder sb = new StringBuilder();

         while(bitShifting > 0) {
            bitShifting = (byte)(bitShifting - 3);
            sb.append((int)(uidBits >> bitShifting & 7L));
         }

         return sb.toString().substring(0, digitNumber);
      } else {
         return "";
      }
   }

   public static String getR8Uid(String rawTagUID) {
      return getOctalUidFromHexBytes(rawTagUID, 10);
   }

   public static String getE2Uid(String rawTagUID) {
      return octalE2UidToAlphanumeric(getOctalUidFromHexBytes(rawTagUID, 20));
   }

   private static String octalE2UidToAlphanumeric(String octalUid) {
      StringBuilder sb = new StringBuilder(10);

      for(byte i = 0; i < octalUid.length(); i = (byte)(i + 2)) {
         sb.append(SPCE2_CONVERSION_TABLE[Integer.parseInt(octalUid.substring(i, i + 2), 8)]);
      }

      return sb.toString();
   }

   public static String getBlankE2Uid(String rawTagUID) {
      return "@" + getR8Uid(rawTagUID);
   }

   public static String getBlankE2PureOctalUid(String e2Uid) {
      return e2Uid != null && e2Uid.startsWith("@") && e2Uid.length() == 11?String.format("%1$-20s", new Object[]{e2Uid.substring(1)}).replace(' ', '0'):"";
   }

   public static String alphanumericE2UidToOctal(String alphanumericUid) {
      String anUid = padRightWithSpacesUid(alphanumericUid);
      StringBuilder sb = new StringBuilder();
      char[] arr$ = anUid.toCharArray();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Character character = Character.valueOf(arr$[i$]);
         int index = SPCE2_CONVERSION_LIST.indexOf(character);
         if(index == -1) {
            return "";
         }

         sb.append(String.format("%02o", new Object[]{Integer.valueOf(index)}));
      }

      return sb.toString();
   }

   public static byte[] simpleToCompleteOctalUid(String octalUid, boolean computeCRC) {
      byte tagDigitsLength = (byte)(octalUid.length() + octalUid.length() / 5);
      byte[] tagDigits = new byte[tagDigitsLength];
      byte indexByte = 0;
      byte indexOct = 0;

      for(byte chunkDigitIndex = 0; indexOct < octalUid.length(); ++indexByte) {
         if(chunkDigitIndex != 5) {
            tagDigits[indexByte] = Byte.parseByte(String.valueOf(octalUid.charAt(indexOct)));
            ++chunkDigitIndex;
            ++indexOct;
         } else {
            tagDigits[indexByte] = computeCRC?CRC.computeDigitGroupCRC(tagDigits, indexByte - 5):8;
            chunkDigitIndex = 0;
         }
      }

      if(indexByte < tagDigits.length) {
         tagDigits[indexByte] = computeCRC?CRC.computeDigitGroupCRC(tagDigits, indexByte - 5):8;
      }

      return tagDigits;
   }

   public static char[] octalUidTo64BitsWord(byte[] tagDigits) {
      byte[] tag24Digits = new byte[24];
      System.arraycopy(tagDigits, 0, tag24Digits, 0, tagDigits.length > 24?24:tagDigits.length);
      char word = 0;
      ByteBuffer byteBuffer = ByteBuffer.allocate(8);
      byte shift = 9;
      byte chunkDigitIndex = 0;
      byte[] word8bytes = tag24Digits;
      int result = tag24Digits.length;

      for(int i$ = 0; i$ < result; ++i$) {
         byte tagDigit = word8bytes[i$];
         if(chunkDigitIndex == 5) {
            chunkDigitIndex = 0;
         } else {
            ++chunkDigitIndex;
            word |= (char)(tagDigit << shift);
            if(shift < 3) {
               byteBuffer.put((byte)(word >> 8));
               shift = (byte)(shift + 8);
               word = (char)(word << 8);
            }

            shift = (byte)(shift - 3);
         }
      }

      byteBuffer.put((byte)(word >> 8));
      word8bytes = byteBuffer.array();
      char[] var10 = new char[]{(char)(word8bytes[6] << 8 | word8bytes[7] & 255), (char)(word8bytes[4] << 8 | word8bytes[5] & 255), (char)(word8bytes[2] << 8 | word8bytes[3] & 255), (char)(word8bytes[0] << 8 | word8bytes[1] & 255)};
      return var10;
   }

   public static byte[] expandFullUid(byte[] fullUID, int newLength) {
      if(fullUID.length >= newLength) {
         return fullUID;
      } else {
         byte[] newFullUID = new byte[newLength];
         byte chunkDigit = 0;
         System.arraycopy(fullUID, 0, newFullUID, 0, fullUID.length);

         for(byte i = (byte)fullUID.length; i < newLength; ++i) {
            if(chunkDigit < 5) {
               newFullUID[i] = 0;
               ++chunkDigit;
            } else {
               newFullUID[i] = CRC.computeDigitGroupCRC(newFullUID, i - 5);
               chunkDigit = 0;
            }
         }

         return newFullUID;
      }
   }

   public static int[] octalBytesUidTo128BitsWord(byte[] octalUidBytes) {
      short[] steps = new short[]{(short)0, (short)32, (short)64, (short)96, (short)128, (short)160};
      byte stepLevel = 0;
      byte currentBit = 31;
      int currentWord = 0;
      int[] result = new int[4];
      byte[] arr$ = octalUidBytes;
      int len$ = octalUidBytes.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte octalByte = arr$[i$];

         for(byte bitShifting = 0; bitShifting < 3; ++bitShifting) {
            short currentBlock = steps[stepLevel + 1];
            if((octalByte & 1 << bitShifting) != 0) {
               currentWord |= 1 << currentBlock - (currentBit + 1);
            }

            --currentBit;
            if(currentBlock - currentBit == 33) {
               result[stepLevel] = currentWord;
               currentWord = 0;
               ++stepLevel;
               currentBit = (byte)(steps[stepLevel + 1] - 1);
            }
         }
      }

      return result;
   }

   public static boolean isValidAlphanumericUid(String stringUid) {
      return isValidAlphanumericUid(stringUid, 10);
   }

   public static boolean isValidAlphanumericUidFullMemory(String stringUid) {
      return isValidAlphanumericUid(stringUid, 17);
   }

   private static boolean isValidAlphanumericUid(String stringUid, int maxLength) {
      if(stringUid != null && maxLength > 0 && maxLength <= 17) {
         if(stringUid.trim().isEmpty()) {
            return false;
         } else if(stringUid.length() > maxLength) {
            return false;
         } else if(stringUid.contains("@")) {
            return false;
         } else {
            for(byte i = 0; i < stringUid.length(); ++i) {
               if(!SPCE2_CONVERSION_LIST.contains(Character.valueOf(stringUid.charAt(i)))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static String padRightWithSpacesUid(String newUid) {
      return newUid.length() == 10?newUid:String.format("%1$-10s", new Object[]{newUid});
   }

   public static String rawUidDigitsToFullOctal(String rawTagUID, int expectedLength) {
      if(rawTagUID.length() < expectedLength) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder(rawTagUID.substring(0, expectedLength));

         int i;
         for(i = sb.length() - 2; i > -1; i -= 2) {
            sb.deleteCharAt(i);
         }

         for(i = sb.length() - 1; i > 4; i -= 6) {
            sb.deleteCharAt(i);
         }

         return sb.toString();
      }
   }

   public static String fullOctalToDecimalUid(String fullOctalUid) {
      try {
         return Long.toString(Long.parseLong(fullOctalUid.substring(1), 8));
      } catch (NumberFormatException var2) {
         SmartLogger.getLogger().log(Level.SEVERE, "Invalid octal UID given for conversion to \'Full memory - Decimal\' format.", var2);
         return null;
      }
   }

   public static String fullOctalToAlphaNumericUid(String fullOctalUid) {
      fullOctalUid = fullOctalUid.substring(1);
      StringBuilder sb = new StringBuilder();

      for(byte i = 0; i < fullOctalUid.length(); i = (byte)(i + 2)) {
         sb.append(SPCE2_CONVERSION_TABLE[Integer.parseInt(fullOctalUid.substring(i, i + 2), 8)]);
         if(sb.charAt(sb.length() - 1) == 167) {
            return sb.toString().substring(0, sb.length() - 1);
         }
      }

      return sb.toString();
   }

   public static String displayedToFullOctalUid(String displayedUid) {
      return displayedUid != null && !"".equals(displayedUid.trim())?(displayedUid.length() != 12?(displayedUid.startsWith("3") && containsOnlyDigits(displayedUid, 8)?simpleOctalE2UidToFullOctal(displayedUid):alphaNumericToFullOctalUid(displayedUid)):(containsOnlyDigits(displayedUid)?decimalToFullOctalUid(displayedUid):alphaNumericToFullOctalUid(displayedUid))):null;
   }

   private static String simpleOctalE2UidToFullOctal(String octalE2Uid) {
      byte[] tagUidBytes = new byte[35];
      StringBuilder sb = new StringBuilder(42);
      byte crcCounter = 0;
      if(octalE2Uid.length() > tagUidBytes.length) {
         return null;
      } else {
         int i;
         for(i = 0; i < octalE2Uid.length(); ++i) {
            tagUidBytes[i] = (byte)(octalE2Uid.charAt(i) - 48);
         }

         for(i = 0; i < tagUidBytes.length; ++i) {
            if(crcCounter == 5) {
               sb.append(CRC.computeDigitGroupCRC(tagUidBytes, i - 5));
               crcCounter = 0;
            }

            sb.append(tagUidBytes[i]);
            ++crcCounter;
         }

         if(crcCounter == 5) {
            sb.append(CRC.computeDigitGroupCRC(tagUidBytes, tagUidBytes.length - 5));
         }

         return sb.toString();
      }
   }

   private static String alphaNumericToFullOctalUid(String alphanumericUid) {
      StringBuilder sb = new StringBuilder();
      sb.append(2);
      char[] index = alphanumericUid.toCharArray();
      int len$ = index.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Character character = Character.valueOf(index[i$]);
         int index1 = SPCE2_CONVERSION_LIST.indexOf(character);
         if(index1 == -1) {
            return null;
         }

         sb.append(String.format("%02o", new Object[]{Integer.valueOf(index1)}));
      }

      if(alphanumericUid.length() < 17) {
         int var7 = SPCE2_CONVERSION_LIST.indexOf(Character.valueOf('\u00a7'));
         if(var7 == -1) {
            return null;
         }

         sb.append(String.format("%02o", new Object[]{Integer.valueOf(var7)}));
      }

      return simpleOctalE2UidToFullOctal(sb.toString());
   }

   private static String decimalToFullOctalUid(String decimalUid) {
      String paddedDecimalUid = "00000000000000";

      try {
         String nfe = Long.toString(Long.parseLong(decimalUid), 8);
         nfe = "1" + paddedDecimalUid.substring(nfe.length()) + nfe;
         return simpleOctalE2UidToFullOctal(nfe);
      } catch (NumberFormatException var3) {
         SmartLogger.getLogger().log(Level.SEVERE, "Invalid octal UID given for conversion to \'Full memory - Decimal\' format.", var3);
         return null;
      }
   }

   public static long[] octalFullUidTo128BitsWord(String octalFullUid) {
      if(octalFullUid != null && !"".equals(octalFullUid.trim())) {
         int uidSize = octalFullUid.length();
         if(uidSize % 2 != 0) {
            return new long[0];
         } else {
            long[] word = new long[]{0L, 0L};
            byte[] uidBytes = new byte[uidSize];

            int i;
            for(i = 0; i < uidSize; ++i) {
               uidBytes[i] = (byte)(octalFullUid.charAt(i) - 48);
            }

            for(i = 0; i < uidSize / 2; ++i) {
               word[0] <<= 3;
               word[0] |= (long)uidBytes[i];
            }

            for(i = uidSize / 2; i < uidSize; ++i) {
               word[1] <<= 3;
               word[1] |= (long)uidBytes[i];
            }

            word[0] = word[0] << 1 | word[1] >> 62;
            word[1] <<= 2;
            return word;
         }
      } else {
         return new long[0];
      }
   }

   protected static boolean containsOnlyDigits(String uid) {
      return containsOnlyDigits(uid, 10);
   }

   private static boolean containsOnlyDigits(String uid, int base) {
      char maxDigit = (char)(base == 8?55:57);

      for(int i = 0; i < uid.length(); ++i) {
         char iChar = uid.charAt(i);
         if(iChar < 48 || iChar > maxDigit) {
            return false;
         }
      }

      return true;
   }

}
