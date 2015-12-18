package com.digitalpersona.uareu.dpfj;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Importer;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.Dpfj;

public class ImporterImpl implements Importer {

   private final Dpfj m_dpfj = new Dpfj();


   public Fid ImportDPFid(byte[] data, Fid.Format out_format, int out_dpi, boolean rotate180) throws UareUException {
      return this.m_dpfj.import_dp_fid(data, out_format, out_dpi, rotate180);
   }

   public Fid ImportFid(byte[] data, Fid.Format format) throws UareUException {
      return this.m_dpfj.import_fid(data, format);
   }

   public Fmd ImportFmd(byte[] data, Fmd.Format format, Fmd.Format out_format) throws UareUException {
      return this.m_dpfj.import_fmd(data, format, out_format);
   }
}
