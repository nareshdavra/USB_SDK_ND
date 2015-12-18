package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.CRC;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

final class FirmwareParser {

   private static final byte BYTES_PER_INSTRUCTION_WORD = 3;
   private static final byte ADDRESS_SPACE_PER_INSTRUCTION_WORD = 2;
   private static final byte INSTRUCTION_WORDS_PER_ROW = 64;
   private static final int BYTES_PER_ROW = 192;
   private static final int ADDRESS_SPACE_PER_ROW = 128;
   private static final byte ROWS_PER_PAGE = 8;
   private static final int INSTRUCTION_WORDS_PER_PAGE = 512;
   private static final byte BOOT_PAGE_BASE = 1;
   private static final byte BOOT_PAGE_COUNT = 6;
   private static final byte CONFIG_PAGE_COUNT = 2;
   private static final int APPLICATION_PAGE_BASE = 9;
   private static final byte FIRST_PAGE_ALLOWABLE_ROWS = 5;
   private static final int FIRST_BOOT_LOADER_ROW_INDEX = 8;
   private static final int END_BOOT_LOADER_ROW_INDEX = 56;
   public static final int FIRST_APPLICATION_ROW_INDEX = 72;
   private static final int LAST_ALLOWABLE_APPLICATION_ROW = 65536;
   private static final byte[] BLANK_ROW = new byte[192];
   private boolean _hexLoaded = false;
   private final Map _pages = new TreeMap();
   private final byte[] _iteratorRowBytes = new byte[192];
   private int _iteratorPageListIndex = 0;
   private int _iteratorRowIndex = 0;
   private int _applicationRowCount;
   private int _totalRowCount;
   private String _crc;


   private FirmwareParser(List firmwareLines, boolean withBootLoader) {
      int baseAddress = 0;
      boolean eofReached = false;
      Iterator i$ = firmwareLines.iterator();

      while(i$.hasNext()) {
         String line = (String)i$.next();
         if(eofReached) {
            return;
         }

         if(line.length() < 11 || line.charAt(0) != 58 || (line.length() & 1) == 0) {
            return;
         }

         int dataByteCount = Integer.parseInt(line.substring(1, 3), 16);
         int address = Integer.parseInt(line.substring(3, 7), 16);
         FirmwareParser.RecordType recordType = FirmwareParser.RecordType.fromCode(Integer.parseInt(line.substring(7, 9), 16));
         int checksumDigit = Integer.parseInt(line.substring(line.length() - 2), 16);
         byte checksum = (byte)(dataByteCount + (address >> 8) + (address & 255) + recordType.getCode() + checksumDigit);
         int dataDigitIndex = 9;
         int dataDigitEnd = line.length() - 2;
         char[] dataBytes = new char[dataByteCount];

         char dataByte;
         for(int dataByteIndex = 0; dataDigitIndex < dataDigitEnd; checksum = (byte)(checksum + dataByte)) {
            dataByte = (char)Integer.parseInt(line.substring(dataDigitIndex, dataDigitIndex + 2), 16);
            dataDigitIndex += 2;
            dataBytes[dataByteIndex++] = dataByte;
         }

         if(checksum != 0) {
            return;
         }

         switch(FirmwareParser.NamelessClass1455733574.$SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType[recordType.ordinal()]) {
         case 1:
            this.addData(baseAddress + address, dataBytes);
            break;
         case 2:
            eofReached = true;
            break;
         case 3:
            return;
         case 4:
            baseAddress = (dataBytes[0] << 8 | dataBytes[1]) << 16;
            break;
         default:
            return;
         }
      }

      if(this.calculateRowCountAndCRC(withBootLoader)) {
         if(this._applicationRowCount == 0) {
            return;
         }

         this._hexLoaded = true;
      }

   }

   private boolean calculateRowCountAndCRC(boolean bBootLoaderCode) {
      int applicationRowCount = 0;
      int totalRowCount = 0;
      byte startAllowableRowIndex;
      int endAllowableRowIndex;
      if(bBootLoaderCode) {
         startAllowableRowIndex = 8;
         endAllowableRowIndex = 56;
      } else {
         startAllowableRowIndex = 72;
         endAllowableRowIndex = 65536;
      }

      int currentRowIndex = 0;
      boolean bFirst = true;
      CRC crcCalculator = new CRC();
      FirmwareParser.RowDetails rowDetails = new FirmwareParser.RowDetails();

      while(this.enumerateRows(bFirst, rowDetails)) {
         bFirst = false;
         ++totalRowCount;
         if(rowDetails.getRowIndex() < 65536L) {
            for(; (long)currentRowIndex < rowDetails.getRowIndex(); ++currentRowIndex) {
               if(currentRowIndex < 5 || currentRowIndex >= startAllowableRowIndex) {
                  crcCalculator.bufferize(BLANK_ROW);
               }
            }

            if(rowDetails.getRowIndex() >= 5L && (rowDetails.getRowIndex() < (long)startAllowableRowIndex || rowDetails.getRowIndex() >= (long)endAllowableRowIndex)) {
               byte[] arr$ = rowDetails.getRow();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  byte rowByte = arr$[i$];
                  if(rowByte != -1) {
                     SmartLogger.getLogger().severe("File contains flash data in a protected page.");
                     return false;
                  }
               }
            } else {
               crcCalculator.bufferize(rowDetails.getRow());
            }

            ++currentRowIndex;
         }
      }

      if(currentRowIndex > startAllowableRowIndex) {
         applicationRowCount = currentRowIndex - startAllowableRowIndex;
      }

      this._applicationRowCount = applicationRowCount;
      this._crc = crcCalculator.getCRC(false);
      this._totalRowCount = totalRowCount;
      return true;
   }

   private void addData(int address, char[] dataBytes) {
      if((address & 3) == 0 && (dataBytes.length & 3) == 0) {
         int dataWords = dataBytes.length / 4;
         int wordIndex = address / 4;
         int pageIndex = wordIndex / 512;
         int dstWordIndex = wordIndex % 512;
         int srcByteIndex = 0;
         int srcByteEndIndex = dataBytes.length;
         int srcByteEndIndexNextPass = 0;
         if(dstWordIndex + dataWords > 512) {
            srcByteEndIndexNextPass = srcByteEndIndex;
            srcByteEndIndex -= 4 * (dstWordIndex + dataWords - 512);
         }

         while(true) {
            Long[] page = (Long[])this._pages.get(Integer.valueOf(pageIndex));

            while(page == null) {
               page = new Long[512];

               for(int word = 0; word < page.length; ++word) {
                  page[word] = Long.valueOf(16777215L);
               }

               this._pages.put(Integer.valueOf(pageIndex), page);
            }

            while(srcByteIndex < srcByteEndIndex) {
               long var14 = 0L;

               for(int i = 0; i < 32; i += 8) {
                  var14 |= (long)(dataBytes[srcByteIndex++] << i);
               }

               page[dstWordIndex++] = Long.valueOf(var14);
            }

            if(srcByteEndIndexNextPass == 0) {
               return;
            }

            ++pageIndex;
            dstWordIndex = 0;
            srcByteEndIndex = srcByteEndIndexNextPass;
            srcByteEndIndexNextPass = 0;
         }
      }
   }

   boolean enumerateRowsWords(boolean bStartOver, FirmwareParser.RowDetails rowDetails) {
      if(bStartOver) {
         this._iteratorPageListIndex = 0;
         this._iteratorRowIndex = 0;
      }

      if(this._iteratorPageListIndex >= this._pages.size()) {
         rowDetails.setRowWords((Long[])null);
         rowDetails.setRowIndex(0L);
         return false;
      } else {
         int pageIndex = -1;
         int i = 0;

         for(Iterator pageWords = this._pages.entrySet().iterator(); pageWords.hasNext(); ++i) {
            Entry from = (Entry)pageWords.next();
            if(i == this._iteratorPageListIndex) {
               pageIndex = ((Integer)from.getKey()).intValue();
               break;
            }
         }

         if(pageIndex == -1) {
            return false;
         } else {
            rowDetails.setRowIndex((long)(8 * pageIndex + this._iteratorRowIndex));
            Long[] var8 = (Long[])this._pages.get(Integer.valueOf(pageIndex));
            int var9 = this._iteratorRowIndex * 64;
            int to = var9 + 64;
            rowDetails.setRowWords((Long[])Arrays.copyOfRange(var8, var9, to));
            ++this._iteratorRowIndex;
            if(this._iteratorRowIndex >= 8) {
               ++this._iteratorPageListIndex;
               this._iteratorRowIndex = 0;
            }

            return true;
         }
      }
   }

   public boolean enumerateRows(boolean bStartOver, FirmwareParser.RowDetails rowDetails) {
      rowDetails.setRow((byte[])null);
      if(!this.enumerateRowsWords(bStartOver, rowDetails)) {
         return false;
      } else {
         int dstByte = 0;

         for(int wordIndex = 0; wordIndex < 64; ++wordIndex) {
            for(int shift = 0; shift < 24; shift += 8) {
               this._iteratorRowBytes[dstByte++] = (byte)((int)(rowDetails.getRowWords()[wordIndex].longValue() >> shift));
            }
         }

         rowDetails.setRow(this._iteratorRowBytes);
         return true;
      }
   }

   public static FirmwareParser loadHexLines(List firmwareLines, boolean withBootLoader) {
      if(firmwareLines != null && !firmwareLines.isEmpty()) {
         FirmwareParser loader = new FirmwareParser(firmwareLines, withBootLoader);
         return loader._hexLoaded?loader:null;
      } else {
         return null;
      }
   }

   public int getTotalRowCount() {
      return this._totalRowCount;
   }

   public int getApplicationRowCount() {
      return this._applicationRowCount;
   }

   public String getCrc() {
      return this._crc;
   }

   static {
      for(int i = 0; i < 192; ++i) {
         BLANK_ROW[i] = -1;
      }

   }

   private static enum RecordType {

      DATA("DATA", 0, 0),
      END_OF_FILE("END_OF_FILE", 1, 1),
      SEGMENT_ADDRESS("SEGMENT_ADDRESS", 2, 2),
      LINEAR_ADDRESS("LINEAR_ADDRESS", 3, 4);
      private final int _code;
      private static final Map CODE_TO_RECORD_TYPE = new HashMap();
      // $FF: synthetic field
      private static final FirmwareParser.RecordType[] $VALUES = new FirmwareParser.RecordType[]{DATA, END_OF_FILE, SEGMENT_ADDRESS, LINEAR_ADDRESS};


      private RecordType(String var1, int var2, int code) {
         this._code = code;
      }

      public int getCode() {
         return this._code;
      }

      public static FirmwareParser.RecordType fromCode(int code) {
         if(CODE_TO_RECORD_TYPE.isEmpty()) {
            FirmwareParser.RecordType[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               FirmwareParser.RecordType recordType = arr$[i$];
               CODE_TO_RECORD_TYPE.put(Integer.valueOf(recordType.getCode()), recordType);
            }
         }

         return (FirmwareParser.RecordType)CODE_TO_RECORD_TYPE.get(Integer.valueOf(code));
      }

   }

   public static final class RowDetails {

      private byte[] _row;
      private long _rowIndex;
      private Long[] _words;


      public byte[] getRow() {
         return this._row == null?null:Arrays.copyOf(this._row, this._row.length);
      }

      public long getRowIndex() {
         return this._rowIndex;
      }

      public Long[] getRowWords() {
         return (Long[])Arrays.copyOf(this._words, this._words.length);
      }

      public void setRow(byte[] row) {
         this._row = row == null?null:Arrays.copyOf(row, row.length);
      }

      public void setRowIndex(long rowIndex) {
         this._rowIndex = rowIndex;
      }

      public void setRowWords(Long[] words) {
         this._words = words == null?null:(Long[])Arrays.copyOf(words, words.length);
      }
   }

   // $FF: synthetic class
   static class NamelessClass1455733574 {

      // $FF: synthetic field
      static final int[] $SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType = new int[FirmwareParser.RecordType.values().length];


      static {
         try {
            $SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType[FirmwareParser.RecordType.DATA.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            $SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType[FirmwareParser.RecordType.END_OF_FILE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            $SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType[FirmwareParser.RecordType.SEGMENT_ADDRESS.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            $SwitchMap$com$spacecode$sdk$device$FirmwareParser$RecordType[FirmwareParser.RecordType.LINEAR_ADDRESS.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }
}
