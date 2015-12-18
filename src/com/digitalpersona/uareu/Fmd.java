package com.digitalpersona.uareu;


public interface Fmd {

   int getCbeffId();

   int getCaptureEquipmentCompliance();

   int getCaptureEquipmentId();

   int getWidth();

   int getHeight();

   int getResolution();

   int getViewCnt();

   Fmd.Format getFormat();

   Fmd.Fmv[] getViews();

   byte[] getData();

   public static enum Format {

      ANSI_378_2004("ANSI_378_2004", 0),
      ISO_19794_2_2005("ISO_19794_2_2005", 1),
      DP_PRE_REG_FEATURES("DP_PRE_REG_FEATURES", 2),
      DP_REG_FEATURES("DP_REG_FEATURES", 3),
      DP_VER_FEATURES("DP_VER_FEATURES", 4);
      // $FF: synthetic field
      private static final Fmd.Format[] $VALUES = new Fmd.Format[]{ANSI_378_2004, ISO_19794_2_2005, DP_PRE_REG_FEATURES, DP_REG_FEATURES, DP_VER_FEATURES};


      private Format(String var1, int var2) {}

   }

   public interface Fmv {

      int getFingerPosition();

      int getViewNumber();

      int getImpressionType();

      int getQuality();

      int getMinutiaCnt();

      byte[] getData();

      byte[] getExtBlockData();
   }
}
