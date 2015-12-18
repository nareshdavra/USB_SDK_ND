package com.digitalpersona.uareu.dpfpdd;

import com.digitalpersona.uareu.Fid;

public final class FidImpl implements Fid {

   private int cbeff_id;
   private int capture_device_id;
   private int acquisition_level;
   private int scale_units;
   private int scan_resolution;
   private int image_resolution;
   private int bpp;
   private int compression;
   private Fid.Format m_format;
   private byte[] m_data;
   private FidImpl.FivImpl[] m_views;


   public int getCbeffId() {
      return this.cbeff_id;
   }

   public int getCaptureDeviceId() {
      return this.capture_device_id;
   }

   public int getAcquisitionLevel() {
      return this.acquisition_level;
   }

   public int getFingerCnt() {
      return this.m_views.length;
   }

   public int getScaleUnits() {
      return this.scale_units;
   }

   public int getScanResolution() {
      return this.scan_resolution;
   }

   public int getImageResolution() {
      return this.image_resolution;
   }

   public int getBpp() {
      return this.bpp;
   }

   public int getCompression() {
      return this.compression;
   }

   public Fid.Format getFormat() {
      return this.m_format;
   }

   public Fid.Fiv[] getViews() {
      return this.m_views;
   }

   public byte[] getData() {
      return this.m_data;
   }

   public FidImpl(Fid.Format format, int nViewCnt) {
      this.m_format = format;
      this.m_views = new FidImpl.FivImpl[nViewCnt];

      for(int i = 0; i < nViewCnt; ++i) {
         this.m_views[i] = new FidImpl.FivImpl(this);
      }

   }

   private final class FivImpl implements Fid.Fiv {

      private int finger_position;
      private int view_cnt;
      private int view_number;
      private int quality;
      private int impression_type;
      private int height;
      private int width;
      private int m_length;
      private int m_offset;
      private int m_image_offset;
      private FidImpl m_fid;


      public int getFingerPosition() {
         return this.finger_position;
      }

      public int getViewCnt() {
         return this.view_cnt;
      }

      public int getViewNumber() {
         return this.view_number;
      }

      public int getQuality() {
         return this.quality;
      }

      public int getImpressionType() {
         return this.impression_type;
      }

      public int getHeight() {
         return this.height;
      }

      public int getWidth() {
         return this.width;
      }

      public byte[] getData() {
         byte[] data = new byte[this.m_length];
         System.arraycopy(this.m_fid.m_data, this.m_offset, data, 0, this.m_length);
         return data;
      }

      public byte[] getImageData() {
         int nImageLength = this.m_length - (this.m_image_offset - this.m_offset);
         byte[] data = new byte[nImageLength];
         System.arraycopy(this.m_fid.m_data, this.m_image_offset, data, 0, nImageLength);
         return data;
      }

      protected FivImpl(FidImpl fid) {
         this.m_fid = fid;
      }
   }
}
