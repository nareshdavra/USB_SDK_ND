package com.digitalpersona.uareu.dpfj;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.Dpfj;

public class EngineImpl implements Engine {

   private final Dpfj m_dpfj = new Dpfj();


   public Fmd CreateFmd(Fid fid, Fmd.Format format) throws UareUException {
      return this.m_dpfj.create_fmd_from_fid(fid, format);
   }

   public Fmd CreateFmd(byte[] data, int width, int height, int resolution, int finger_position, int cbeff_id, Fmd.Format format) throws UareUException {
      return this.m_dpfj.create_fmd_from_raw(data, width, height, resolution, finger_position, cbeff_id, format);
   }

   public int Compare(Fmd fmd1, int view_index1, Fmd fmd2, int view_index2) throws UareUException {
      return this.m_dpfj.compare(fmd1, view_index1, fmd2, view_index2);
   }

   public Engine.Candidate[] Identify(Fmd fmd1, int view_index1, Fmd[] fmds, int threshold_score, int candidates_requested) throws UareUException {
      return this.m_dpfj.identify(fmd1, view_index1, fmds, threshold_score, candidates_requested);
   }

   public Fmd CreateEnrollmentFmd(Fmd.Format format, Engine.EnrollmentCallback enrollment_callback) throws UareUException {
      Fmd.Format pre_format = Fmd.Format.ANSI_378_2004;
      switch(EngineImpl.NamelessClass267042760.$SwitchMap$com$digitalpersona$uareu$Fmd$Format[format.ordinal()]) {
      case 1:
         pre_format = Fmd.Format.ANSI_378_2004;
         break;
      case 2:
         pre_format = Fmd.Format.ISO_19794_2_2005;
         break;
      default:
         pre_format = Fmd.Format.DP_PRE_REG_FEATURES;
      }

      this.m_dpfj.start_enrollment(format);
      UareUException ex = null;
      Fmd fmd = null;

      try {
         boolean e;
         Engine.PreEnrollmentFmd pre_fmd;
         for(e = false; !e; e = this.m_dpfj.add_to_enrollment(pre_fmd.fmd, pre_fmd.view_index)) {
            pre_fmd = enrollment_callback.GetFmd(pre_format);
            if(null == pre_fmd || null == pre_fmd.fmd) {
               break;
            }
         }

         if(e) {
            fmd = this.m_dpfj.create_enrollment_fmd();
         }
      } catch (UareUException var8) {
         ex = var8;
      }

      this.m_dpfj.finish_enrollment();
      if(null != ex) {
         throw ex;
      } else {
         return fmd;
      }
   }

   // $FF: synthetic class
   static class NamelessClass267042760 {

      // $FF: synthetic field
      static final int[] $SwitchMap$com$digitalpersona$uareu$Fmd$Format = new int[Fmd.Format.values().length];


      static {
         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.ANSI_378_2004.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            $SwitchMap$com$digitalpersona$uareu$Fmd$Format[Fmd.Format.ISO_19794_2_2005.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }
}
