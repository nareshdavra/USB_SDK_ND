package com.digitalpersona.uareu;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;

public interface Engine {

   int PROBABILITY_ONE = Integer.MAX_VALUE;


   Fmd CreateFmd(Fid var1, Fmd.Format var2) throws UareUException;

   Fmd CreateFmd(byte[] var1, int var2, int var3, int var4, int var5, int var6, Fmd.Format var7) throws UareUException;

   int Compare(Fmd var1, int var2, Fmd var3, int var4) throws UareUException;

   Engine.Candidate[] Identify(Fmd var1, int var2, Fmd[] var3, int var4, int var5) throws UareUException;

   Fmd CreateEnrollmentFmd(Fmd.Format var1, Engine.EnrollmentCallback var2) throws UareUException;

   public static class PreEnrollmentFmd {

      public Fmd fmd;
      public int view_index;


   }

   public interface EnrollmentCallback {

      Engine.PreEnrollmentFmd GetFmd(Fmd.Format var1);
   }

   public static class Candidate {

      public int fmd_index;
      public int view_index;


   }
}
