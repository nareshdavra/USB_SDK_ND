package com.digitalpersona.uareu.jni;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfpdd.FidImpl;

public class Dpfpdd {

   public native int DpfpddInit();

   public native int DpfpddExit();

   public native int DpfpddQueryDevices(Dpfpdd.IntReference var1, Reader.Description[] var2, Dpfpdd.IntReference[] var3, Dpfpdd.IntReference[] var4);

   public native int DpfpddOpen(String var1, int var2, Dpfpdd.LongReference var3);

   public native int DpfpddClose(long var1);

   public native int DpfpddGetCapabilities(long var1, Reader.Capabilities var3);

   public native int DpfpddGetStatus(long var1, Reader.Status var3, Dpfpdd.IntReference var4);

   public native int DpfpddReset(long var1);

   public native int DpfpddCalibrate(long var1);

   public native int DpfpddCapture(long var1, int var3, int var4, int var5, int var6, int var7, Dpfpdd.IntReference var8, Dpfpdd.IntReference var9, FidImpl var10);

   public native int DpfpddCaptureCancel(long var1);

   public native int DpfpddStartStream(long var1);

   public native int DpfpddStopStream(long var1);

   public native int DpfpddGetStreamImage(long var1, int var3, int var4, int var5, int var6, Dpfpdd.IntReference var7, Dpfpdd.IntReference var8, FidImpl var9);

   public Dpfpdd() {
      System.loadLibrary("dpuareu_jni");
   }

   public void init() throws UareUException {
      synchronized(this) {
         int result = this.DpfpddInit();
         if(0 != result) {
            throw new UareUException(result);
         }
      }
   }

   public void exit() throws UareUException {
      synchronized(this) {
         int result = this.DpfpddExit();
         if(0 != result) {
            throw new UareUException(result);
         }
      }
   }

   public Reader.Description[] query_devices() throws UareUException {
      Reader.Description[] descriptions = new Reader.Description[0];
      Dpfpdd.IntReference[] technologies = new Dpfpdd.IntReference[0];
      Dpfpdd.IntReference[] modalities = new Dpfpdd.IntReference[0];
      Dpfpdd.IntReference cnt = new Dpfpdd.IntReference(0);

      while(true) {
         int result = this.DpfpddQueryDevices(cnt, descriptions, technologies, modalities);
         int i;
         if(0 == result) {
            for(i = 0; i < descriptions.length; ++i) {
               descriptions[i].technology = toTechnology(technologies[i].value);
               descriptions[i].modality = toModality(modalities[i].value);
            }

            return descriptions;
         }

         if(0 != result) {
            if(96075789 != result) {
               throw new UareUException(result);
            }

            descriptions = new Reader.Description[cnt.value];
            technologies = new Dpfpdd.IntReference[cnt.value];
            modalities = new Dpfpdd.IntReference[cnt.value];

            for(i = 0; i < descriptions.length; ++i) {
               descriptions[i] = new Reader.Description();
               technologies[i] = new Dpfpdd.IntReference(0);
               modalities[i] = new Dpfpdd.IntReference(0);
            }
         }
      }
   }

   public long open(String strReaderName, Reader.Priority prio) throws UareUException {
      Dpfpdd.LongReference hReader = new Dpfpdd.LongReference(0L);
      int result = this.DpfpddOpen(strReaderName, fromPriority(prio), hReader);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return hReader.value;
      }
   }

   public void close(long hReader) throws UareUException {
      int result = this.DpfpddClose(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public Reader.Capabilities get_capabilities(long hReader) throws UareUException {
      Reader.Capabilities caps = new Reader.Capabilities();
      int result = this.DpfpddGetCapabilities(hReader, caps);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return caps;
      }
   }

   public Reader.Status get_status(long hReader) throws UareUException {
      Reader.Status status = new Reader.Status();
      Dpfpdd.IntReference IntStatus = new Dpfpdd.IntReference(0);
      int result = this.DpfpddGetStatus(hReader, status, IntStatus);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         status.status = toStatus(IntStatus.value);
         return status;
      }
   }

   public void reset(long hReader) throws UareUException {
      int result = this.DpfpddReset(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public void calibrate(long hReader) throws UareUException {
      int result = this.DpfpddCalibrate(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public Reader.CaptureResult capture(long hReader, int size_expected, Fid.Format img_format, Reader.ImageProcessing img_proc, int resolution, int timeout) throws UareUException {
      Dpfpdd.IntReference score = new Dpfpdd.IntReference(0);
      Dpfpdd.IntReference quality = new Dpfpdd.IntReference(0);
      FidImpl fid = new FidImpl(img_format, 1);
      int result = this.DpfpddCapture(hReader, size_expected, fromFormat(img_format), fromImageProcessing(img_proc), resolution, timeout, score, quality, fid);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         Reader.CaptureResult cres = new Reader.CaptureResult();
         cres.score = score.value;
         cres.quality = toQuality(quality.value);
         if(null != fid.getData() && 0 != fid.getData().length) {
            cres.image = fid;
         }

         return cres;
      }
   }

   public void capture_cancel(long hReader) throws UareUException {
      int result = this.DpfpddCaptureCancel(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public void start_stream(long hReader) throws UareUException {
      int result = this.DpfpddStartStream(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public void stop_stream(long hReader) throws UareUException {
      int result = this.DpfpddStopStream(hReader);
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   public Reader.CaptureResult get_stream_image(long hReader, int size_expected, Fid.Format img_format, Reader.ImageProcessing img_proc, int resolution) throws UareUException {
      Dpfpdd.IntReference score = new Dpfpdd.IntReference(0);
      Dpfpdd.IntReference quality = new Dpfpdd.IntReference(0);
      FidImpl fid = new FidImpl(img_format, 1);
      int result = this.DpfpddGetStreamImage(hReader, size_expected, fromFormat(img_format), fromImageProcessing(img_proc), resolution, score, quality, fid);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         Reader.CaptureResult cres = new Reader.CaptureResult();
         cres.score = score.value;
         cres.quality = toQuality(quality.value);
         if(null != fid.getData() && 0 != fid.getData().length) {
            cres.image = fid;
         }

         return cres;
      }
   }

   private static Reader.Technology toTechnology(int n) {
      switch(n) {
      case 0:
         return Reader.Technology.HW_TECHNOLOGY_UNKNOWN;
      case 1:
         return Reader.Technology.HW_TECHNOLOGY_OPTICAL;
      case 2:
         return Reader.Technology.HW_TECHNOLOGY_CAPACITIVE;
      case 3:
         return Reader.Technology.HW_TECHNOLOGY_THERMAL;
      case 4:
         return Reader.Technology.HW_TECHNOLOGY_PRESSURE;
      default:
         return Reader.Technology.HW_TECHNOLOGY_UNKNOWN;
      }
   }

   private static Reader.Modality toModality(int n) {
      switch(n) {
      case 0:
         return Reader.Modality.HW_MODALITY_UNKNOWN;
      case 1:
         return Reader.Modality.HW_MODALITY_SWIPE;
      case 2:
         return Reader.Modality.HW_MODALITY_AREA;
      default:
         return Reader.Modality.HW_MODALITY_UNKNOWN;
      }
   }

   private static Reader.ReaderStatus toStatus(int n) {
      switch(n) {
      case 0:
         return Reader.ReaderStatus.READY;
      case 1:
         return Reader.ReaderStatus.BUSY;
      case 2:
         return Reader.ReaderStatus.NEED_CALIBRATION;
      case 3:
         return Reader.ReaderStatus.FAILURE;
      default:
         return Reader.ReaderStatus.FAILURE;
      }
   }

   private static int fromFormat(Fid.Format fmt) {
      switch(Dpfpdd.NamelessClass1983933473.$SwitchMap$com$digitalpersona$uareu$Fid$Format[fmt.ordinal()]) {
      case 1:
         return 1770497;
      case 2:
         return 16842759;
      default:
         return 0;
      }
   }

   private static int fromImageProcessing(Reader.ImageProcessing proc) {
      switch(Dpfpdd.NamelessClass1983933473.$SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing[proc.ordinal()]) {
      case 1:
         return 0;
      case 2:
         return 1;
      case 3:
         return 2;
      case 4:
         return 1382119241;
      default:
         return 0;
      }
   }

   private static int fromPriority(Reader.Priority prio) {
      switch(Dpfpdd.NamelessClass1983933473.$SwitchMap$com$digitalpersona$uareu$Reader$Priority[prio.ordinal()]) {
      case 1:
         return 2;
      case 2:
         return 4;
      default:
         return 2;
      }
   }

   private static Reader.CaptureQuality toQuality(int n) {
      switch(n) {
      case 0:
         return Reader.CaptureQuality.GOOD;
      case 1:
         return Reader.CaptureQuality.TIMED_OUT;
      case 2:
         return Reader.CaptureQuality.CANCELED;
      case 4:
         return Reader.CaptureQuality.NO_FINGER;
      case 8:
         return Reader.CaptureQuality.FAKE_FINGER;
      case 16:
         return Reader.CaptureQuality.FINGER_TOO_LEFT;
      case 32:
         return Reader.CaptureQuality.FINGER_TOO_RIGHT;
      case 64:
         return Reader.CaptureQuality.FINGER_TOO_HIGH;
      case 128:
         return Reader.CaptureQuality.FINGER_TOO_LOW;
      case 256:
         return Reader.CaptureQuality.FINGER_OFF_CENTER;
      case 512:
         return Reader.CaptureQuality.SCAN_SKEWED;
      case 1024:
         return Reader.CaptureQuality.SCAN_TOO_SHORT;
      case 2048:
         return Reader.CaptureQuality.SCAN_TOO_LONG;
      case 4096:
         return Reader.CaptureQuality.SCAN_TOO_SLOW;
      case 8192:
         return Reader.CaptureQuality.SCAN_TOO_FAST;
      case 16384:
         return Reader.CaptureQuality.SCAN_WRONG_DIRECTION;
      case '\u8000':
         return Reader.CaptureQuality.READER_DIRTY;
      default:
         return Reader.CaptureQuality.NO_FINGER;
      }
   }

   private class IntReference {

      protected int value;


      protected IntReference(int n) {
         this.value = n;
      }
   }

   // $FF: synthetic class
   static class NamelessClass1983933473 {

      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Fid$Format;
      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing;
      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Reader$Priority = new int[Reader.Priority.values().length];


      static {
         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$Priority[Reader.Priority.COOPERATIVE.ordinal()] = 1;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$Priority[Reader.Priority.EXCLUSIVE.ordinal()] = 2;
         } catch (NoSuchFieldError var7) {
            ;
         }

         $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing = new int[Reader.ImageProcessing.values().length];

         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing[Reader.ImageProcessing.IMG_PROC_DEFAULT.ordinal()] = 1;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing[Reader.ImageProcessing.IMG_PROC_PIV.ordinal()] = 2;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing[Reader.ImageProcessing.IMG_PROC_ENHANCED.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Reader$ImageProcessing[Reader.ImageProcessing.IMG_PROC_UNPROCESSED.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
            ;
         }

         $SwitchMap$com$digitalpersona$uareu$Fid$Format = new int[Fid.Format.values().length];

         try {
            $SwitchMap$com$digitalpersona$uareu$Fid$Format[Fid.Format.ANSI_381_2004.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fid$Format[Fid.Format.ISO_19794_4_2005.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }

   private class LongReference {

      protected long value;


      protected LongReference(long n) {
         this.value = n;
      }
   }
}
