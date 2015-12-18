package com.digitalpersona.uareu.dpfj;

import com.digitalpersona.uareu.Fmd;

public class FmdImpl implements Fmd {

   private int cbeff_id;
   private int capture_equipment_comp;
   private int capture_equipment_id;
   private int width;
   private int height;
   private int resolution;
   private Fmd.Format m_format;
   private byte[] m_data;
   private FmdImpl.FmvImpl[] m_views;


   public int getCbeffId() {
      return this.cbeff_id;
   }

   public int getCaptureEquipmentCompliance() {
      return this.capture_equipment_comp;
   }

   public int getCaptureEquipmentId() {
      return this.capture_equipment_id;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getResolution() {
      return this.resolution;
   }

   public int getViewCnt() {
      return this.m_views.length;
   }

   public Fmd.Format getFormat() {
      return this.m_format;
   }

   public Fmd.Fmv[] getViews() {
      return this.m_views;
   }

   public byte[] getData() {
      return this.m_data;
   }

   public FmdImpl(Fmd.Format format, int nViewCnt) {
      this.m_format = format;
      this.m_views = new FmdImpl.FmvImpl[nViewCnt];

      for(int i = 0; i < nViewCnt; ++i) {
         this.m_views[i] = new FmdImpl.FmvImpl(this);
      }

   }

   private class FmvImpl implements Fmd.Fmv {

      private int finger_position;
      private int view_number;
      private int impression_type;
      private int quality;
      private int minutia_cnt;
      private int ext_block_length;
      private int m_length;
      private int m_offset;
      private FmdImpl m_fmd;


      public int getFingerPosition() {
         return this.finger_position;
      }

      public int getViewNumber() {
         return this.view_number;
      }

      public int getImpressionType() {
         return this.impression_type;
      }

      public int getQuality() {
         return this.quality;
      }

      public int getMinutiaCnt() {
         return this.minutia_cnt;
      }

      public byte[] getData() {
         byte[] data = new byte[this.m_length];
         System.arraycopy(this.m_fmd.m_data, this.m_offset, data, 0, this.m_length);
         return data;
      }

      public byte[] getExtBlockData() {
         if(0 != this.ext_block_length && -1 != this.ext_block_length) {
            byte[] data = new byte[this.ext_block_length];
            System.arraycopy(this.m_fmd.m_data, this.m_offset + this.m_length - this.ext_block_length, data, 0, this.ext_block_length);
            return data;
         } else {
            return null;
         }
      }

      protected FmvImpl(FmdImpl fmd) {
         this.m_fmd = fmd;
      }
   }
}
