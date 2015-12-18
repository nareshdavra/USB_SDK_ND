package com.digitalpersona.uareu;


public interface Fid {

   int getCbeffId();

   int getCaptureDeviceId();

   int getAcquisitionLevel();

   int getFingerCnt();

   int getScaleUnits();

   int getScanResolution();

   int getImageResolution();

   int getBpp();

   int getCompression();

   Fid.Format getFormat();

   Fid.Fiv[] getViews();

   byte[] getData();

   public interface Fiv {

      int getFingerPosition();

      int getViewCnt();

      int getViewNumber();

      int getQuality();

      int getImpressionType();

      int getHeight();

      int getWidth();

      byte[] getData();

      byte[] getImageData();
   }

   public static enum Format {

      ANSI_381_2004("ANSI_381_2004", 0),
      ISO_19794_4_2005("ISO_19794_4_2005", 1);
      // $FF: synthetic field
      private static final Fid.Format[] $VALUES = new Fid.Format[]{ANSI_381_2004, ISO_19794_4_2005};


      private Format(String var1, int var2) {}

   }
}
