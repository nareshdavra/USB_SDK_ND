package com.digitalpersona.uareu.jni;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfj.FmdImpl;
import com.digitalpersona.uareu.dpfpdd.FidImpl;

public class Dpfj {

   private Fmd.Format m_enrollment_fmd_format;


   public native int DpfjImportFmd(int var1, byte[] var2, int var3, FmdImpl var4);

   public native int DpfjImportDpFid(byte[] var1, int var2, int var3, int var4, FidImpl var5);

   public native int DpfjImportFid(byte[] var1, int var2, FidImpl var3);

   public native int DpfjCreateFmdFromRaw(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7, FmdImpl var8);

   public native int DpfjCreateFmdFromFid(byte[] var1, int var2, int var3, int var4, FmdImpl var5);

   public native int DpfjCompare(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6, Dpfj.IntReference var7);

   public native int DpfjIdentify(byte[] var1, int var2, int var3, byte[][] var4, int var5, int var6, Dpfj.IntReference var7, Engine.Candidate[] var8);

   public native int DpfjStartEnrollment(int var1);

   public native int DpfjAddToEnrollment(byte[] var1, int var2, int var3);

   public native int DpfjCreateEnrollmentFmd(int var1, FmdImpl var2);

   public native int DpfjFinishEnrollment();

   public Dpfj() {
      System.loadLibrary("dpuareu_jni");
   }

   public Fmd import_fmd(byte[] fmd_data, Fmd.Format fmd_format, Fmd.Format fmd_format_to) throws UareUException {
      byte nViewCnt = 0;
      switch(Dpfj.NamelessClass1461150500.$SwitchMap$com$digitalpersona$uareu$Fmd$Format[fmd_format.ordinal()]) {
      case 1:
         if(0 == fmd_data[8] && 0 == fmd_data[9]) {
            nViewCnt = fmd_data[28];
         } else {
            nViewCnt = fmd_data[24];
         }
         break;
      case 2:
         nViewCnt = fmd_data[22];
         break;
      case 3:
      case 4:
      case 5:
         nViewCnt = 1;
      }

      if(0 == nViewCnt) {
         throw new UareUException(96075977);
      } else {
         int fmd_fmt = fromFmdFormat(fmd_format);
         int fmd_fmt_to = fromFmdFormat(fmd_format_to);
         FmdImpl fmd_to = new FmdImpl(fmd_format_to, nViewCnt);
         int result = this.DpfjImportFmd(fmd_fmt, fmd_data, fmd_fmt_to, fmd_to);
         if(0 != result) {
            throw new UareUException(result);
         } else {
            return fmd_to;
         }
      }
   }

   public Fid import_dp_fid(byte[] data, Fid.Format fid_format_to, int fid_dpi, boolean rotate180) throws UareUException {
      int fid_fmt_to = fromFidFormat(fid_format_to);
      FidImpl fid = new FidImpl(fid_format_to, 1);
      int result = this.DpfjImportDpFid(data, fid_fmt_to, fid_dpi, rotate180?1:0, fid);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return fid;
      }
   }

   public Fid import_fid(byte[] data, Fid.Format format) throws UareUException {
      byte nViewCnt = 0;
      switch(Dpfj.NamelessClass1461150500.$SwitchMap$com$digitalpersona$uareu$Fid$Format[format.ordinal()]) {
      case 1:
         nViewCnt = data[22];
         break;
      case 2:
         nViewCnt = data[18];
      }

      if(0 == nViewCnt) {
         throw new UareUException(96075877);
      } else {
         int fid_fmt = fromFidFormat(format);
         FidImpl fid = new FidImpl(format, nViewCnt);
         int result = this.DpfjImportFid(data, fid_fmt, fid);
         if(0 != result) {
            throw new UareUException(result);
         } else {
            return fid;
         }
      }
   }

   public Fmd create_fmd_from_raw(byte[] data, int width, int height, int dpi, int finger_position, int cbeff_id, Fmd.Format fmd_format) throws UareUException {
      int format = fromFmdFormat(fmd_format);
      FmdImpl fmd = new FmdImpl(fmd_format, 1);
      int result = this.DpfjCreateFmdFromRaw(data, width, height, dpi, finger_position, cbeff_id, format, fmd);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return fmd;
      }
   }

   public Fmd create_fmd_from_fid(Fid fid, Fmd.Format fmd_format) throws UareUException {
      int fmd_fmt = fromFmdFormat(fmd_format);
      int fid_fmt = fromFidFormat(fid.getFormat());
      int view_cnt = fid.getViews().length;
      FmdImpl fmd = new FmdImpl(fmd_format, fid.getViews().length);
      int result = this.DpfjCreateFmdFromFid(fid.getData(), fid_fmt, view_cnt, fmd_fmt, fmd);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return fmd;
      }
   }

   public int compare(Fmd fmd1, int view_index1, Fmd fmd2, int view_index2) throws UareUException {
      int fmt1 = fromFmdFormat(fmd1.getFormat());
      int fmt2 = fromFmdFormat(fmd2.getFormat());
      Dpfj.IntReference objScore = new Dpfj.IntReference(0);
      int result = this.DpfjCompare(fmd1.getData(), fmt1, view_index1, fmd2.getData(), fmt2, view_index2, objScore);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return objScore.value;
      }
   }

   public Engine.Candidate[] identify(Fmd fmd1, int view_index1, Fmd[] fmds, int threshold, int candidates_req) throws UareUException {
      int fmt1 = fromFmdFormat(fmd1.getFormat());
      byte[][] fmds_data = (byte[][])null;
      int fmt2 = -1;
      if(null != fmds && 0 != fmds.length) {
         fmds_data = new byte[fmds.length][];

         for(int candidates = 0; candidates < fmds.length; ++candidates) {
            fmds_data[candidates] = fmds[candidates].getData();
         }

         fmt2 = fromFmdFormat(fmds[0].getFormat());
      }

      Engine.Candidate[] var14 = new Engine.Candidate[candidates_req];

      for(int objCandidatesReq = 0; objCandidatesReq < candidates_req; ++objCandidatesReq) {
         var14[objCandidatesReq] = new Engine.Candidate();
      }

      Dpfj.IntReference var15 = new Dpfj.IntReference(candidates_req);
      int result = this.DpfjIdentify(fmd1.getData(), fmt1, view_index1, fmds_data, fmt2, threshold, var15, var14);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         Engine.Candidate[] candidates_res = new Engine.Candidate[var15.value];

         for(int i = 0; i < var15.value; ++i) {
            candidates_res[i] = var14[i];
         }

         var14 = null;
         return candidates_res;
      }
   }

   public void start_enrollment(Fmd.Format fmd_format) throws UareUException {
      int fmt = fromFmdFormat(fmd_format);
      int result = this.DpfjStartEnrollment(fmt);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         this.m_enrollment_fmd_format = fmd_format;
      }
   }

   public boolean add_to_enrollment(Fmd fmd, int view_index) throws UareUException {
      int fmt = fromFmdFormat(fmd.getFormat());
      int result = this.DpfjAddToEnrollment(fmd.getData(), fmt, view_index);
      if(0 != result && 96075789 != result) {
         throw new UareUException(result);
      } else {
         return 96075789 != result;
      }
   }

   public Fmd create_enrollment_fmd() throws UareUException {
      FmdImpl fmd = new FmdImpl(this.m_enrollment_fmd_format, 1);
      int fmt = fromFmdFormat(this.m_enrollment_fmd_format);
      int result = this.DpfjCreateEnrollmentFmd(fmt, fmd);
      if(0 != result) {
         throw new UareUException(result);
      } else {
         return fmd;
      }
   }

   public void finish_enrollment() throws UareUException {
      int result = this.DpfjFinishEnrollment();
      if(0 != result) {
         throw new UareUException(result);
      }
   }

   private static int fromFidFormat(Fid.Format fmt) {
      switch(Dpfj.NamelessClass1461150500.$SwitchMap$com$digitalpersona$uareu$Fid$Format[fmt.ordinal()]) {
      case 1:
         return 1770497;
      case 2:
         return 16842759;
      default:
         return 0;
      }
   }

   private static int fromFmdFormat(Fmd.Format fmt) {
      switch(Dpfj.NamelessClass1461150500.$SwitchMap$com$digitalpersona$uareu$Fmd$Format[fmt.ordinal()]) {
      case 1:
         return 1769473;
      case 2:
         return 16842753;
      case 3:
         return 0;
      case 4:
         return 1;
      case 5:
         return 2;
      default:
         return -1;
      }
   }

   private class IntReference {

      protected int value;


      protected IntReference(int n) {
         this.value = n;
      }
   }

   // $FF: synthetic class
   static class NamelessClass1461150500 {

      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Fmd$Format;
      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Fid$Format = new int[Fid.Format.values().length];


      static {
         try {
            $SwitchMap$com$digitalpersona$uareu$Fid$Format[Fid.Format.ANSI_381_2004.ordinal()] = 1;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fid$Format[Fid.Format.ISO_19794_4_2005.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
            ;
         }

         $SwitchMap$com$digitalpersona$uareu$Fmd$Format = new int[Fmd.Format.values().length];

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.ANSI_378_2004.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.ISO_19794_2_2005.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.DP_PRE_REG_FEATURES.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.DP_REG_FEATURES.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.DP_VER_FEATURES.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }
}
