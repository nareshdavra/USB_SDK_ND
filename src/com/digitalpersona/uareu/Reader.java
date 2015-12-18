package com.digitalpersona.uareu;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.UareUException;

public interface Reader {

   void Open(Reader.Priority var1) throws UareUException;

   void Close() throws UareUException;

   Reader.Status GetStatus() throws UareUException;

   Reader.Capabilities GetCapabilities();

   Reader.Description GetDescription();

   Reader.CaptureResult Capture(Fid.Format var1, Reader.ImageProcessing var2, int var3, int var4) throws UareUException;

   void CancelCapture() throws UareUException;

   void StartStreaming() throws UareUException;

   void StopStreaming() throws UareUException;

   Reader.CaptureResult GetStreamImage(Fid.Format var1, Reader.ImageProcessing var2, int var3) throws UareUException;

   void Calibrate() throws UareUException;

   void Reset() throws UareUException;

   public static enum Technology {

      HW_TECHNOLOGY_UNKNOWN("HW_TECHNOLOGY_UNKNOWN", 0),
      HW_TECHNOLOGY_OPTICAL("HW_TECHNOLOGY_OPTICAL", 1),
      HW_TECHNOLOGY_CAPACITIVE("HW_TECHNOLOGY_CAPACITIVE", 2),
      HW_TECHNOLOGY_THERMAL("HW_TECHNOLOGY_THERMAL", 3),
      HW_TECHNOLOGY_PRESSURE("HW_TECHNOLOGY_PRESSURE", 4);
      // $FF: synthetic field
      private static final Reader.Technology[] $VALUES = new Reader.Technology[]{HW_TECHNOLOGY_UNKNOWN, HW_TECHNOLOGY_OPTICAL, HW_TECHNOLOGY_CAPACITIVE, HW_TECHNOLOGY_THERMAL, HW_TECHNOLOGY_PRESSURE};


      private Technology(String var1, int var2) {}

   }

   public static class Status {

      public boolean finger_detected;
      public Reader.ReaderStatus status;
      public byte[] vendor_data;


   }

   public static enum ReaderStatus {

      READY("READY", 0),
      BUSY("BUSY", 1),
      NEED_CALIBRATION("NEED_CALIBRATION", 2),
      FAILURE("FAILURE", 3);
      // $FF: synthetic field
      private static final Reader.ReaderStatus[] $VALUES = new Reader.ReaderStatus[]{READY, BUSY, NEED_CALIBRATION, FAILURE};


      private ReaderStatus(String var1, int var2) {}

   }

   public static class Capabilities {

      public boolean can_capture;
      public boolean can_stream;
      public boolean can_extract_features;
      public boolean can_match;
      public boolean can_identify;
      public boolean has_fingerprint_storage;
      public int indicator_type;
      public boolean has_power_management;
      public boolean has_calibration;
      public boolean piv_compliant;
      public int[] resolutions;


   }

   public static class Id {

      public int product_id;
      public int vendor_id;
      public String product_name;
      public String vendor_name;


   }

   public static class CaptureResult {

      public int score;
      public Reader.CaptureQuality quality;
      public Fid image;


   }

   public static enum Priority {

      COOPERATIVE("COOPERATIVE", 0),
      EXCLUSIVE("EXCLUSIVE", 1);
      // $FF: synthetic field
      private static final Reader.Priority[] $VALUES = new Reader.Priority[]{COOPERATIVE, EXCLUSIVE};


      private Priority(String var1, int var2) {}

   }

   public static enum CaptureQuality {

      GOOD("GOOD", 0),
      TIMED_OUT("TIMED_OUT", 1),
      CANCELED("CANCELED", 2),
      NO_FINGER("NO_FINGER", 3),
      FAKE_FINGER("FAKE_FINGER", 4),
      FINGER_TOO_LEFT("FINGER_TOO_LEFT", 5),
      FINGER_TOO_RIGHT("FINGER_TOO_RIGHT", 6),
      FINGER_TOO_HIGH("FINGER_TOO_HIGH", 7),
      FINGER_TOO_LOW("FINGER_TOO_LOW", 8),
      FINGER_OFF_CENTER("FINGER_OFF_CENTER", 9),
      SCAN_SKEWED("SCAN_SKEWED", 10),
      SCAN_TOO_SHORT("SCAN_TOO_SHORT", 11),
      SCAN_TOO_LONG("SCAN_TOO_LONG", 12),
      SCAN_TOO_SLOW("SCAN_TOO_SLOW", 13),
      SCAN_TOO_FAST("SCAN_TOO_FAST", 14),
      SCAN_WRONG_DIRECTION("SCAN_WRONG_DIRECTION", 15),
      READER_DIRTY("READER_DIRTY", 16);
      // $FF: synthetic field
      private static final Reader.CaptureQuality[] $VALUES = new Reader.CaptureQuality[]{GOOD, TIMED_OUT, CANCELED, NO_FINGER, FAKE_FINGER, FINGER_TOO_LEFT, FINGER_TOO_RIGHT, FINGER_TOO_HIGH, FINGER_TOO_LOW, FINGER_OFF_CENTER, SCAN_SKEWED, SCAN_TOO_SHORT, SCAN_TOO_LONG, SCAN_TOO_SLOW, SCAN_TOO_FAST, SCAN_WRONG_DIRECTION, READER_DIRTY};


      private CaptureQuality(String var1, int var2) {}

   }

   public static enum Modality {

      HW_MODALITY_UNKNOWN("HW_MODALITY_UNKNOWN", 0),
      HW_MODALITY_SWIPE("HW_MODALITY_SWIPE", 1),
      HW_MODALITY_AREA("HW_MODALITY_AREA", 2);
      // $FF: synthetic field
      private static final Reader.Modality[] $VALUES = new Reader.Modality[]{HW_MODALITY_UNKNOWN, HW_MODALITY_SWIPE, HW_MODALITY_AREA};


      private Modality(String var1, int var2) {}

   }

   public static class Version {

      public Reader.VersionInfo firmware_version = new Reader.VersionInfo();
      public Reader.VersionInfo hardware_version = new Reader.VersionInfo();
      public int bcd_revision;


   }

   public static class Description {

      public String name;
      public String serial_number;
      public Reader.Id id = new Reader.Id();
      public Reader.Modality modality;
      public Reader.Technology technology;
      public Reader.Version version;


      public Description() {
         this.modality = Reader.Modality.HW_MODALITY_UNKNOWN;
         this.technology = Reader.Technology.HW_TECHNOLOGY_UNKNOWN;
         this.version = new Reader.Version();
      }
   }

   public static enum ImageProcessing {

      IMG_PROC_DEFAULT("IMG_PROC_DEFAULT", 0),
      IMG_PROC_PIV("IMG_PROC_PIV", 1),
      IMG_PROC_ENHANCED("IMG_PROC_ENHANCED", 2),
      IMG_PROC_UNPROCESSED("IMG_PROC_UNPROCESSED", 3);
      // $FF: synthetic field
      private static final Reader.ImageProcessing[] $VALUES = new Reader.ImageProcessing[]{IMG_PROC_DEFAULT, IMG_PROC_PIV, IMG_PROC_ENHANCED, IMG_PROC_UNPROCESSED};


      private ImageProcessing(String var1, int var2) {}

   }

   public static class VersionInfo {

      public int major;
      public int minor;
      public int maintenance;


   }
}
