package com.digitalpersona.uareu;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;

public interface Importer {

   Fid ImportDPFid(byte[] var1, Fid.Format var2, int var3, boolean var4) throws UareUException;

   Fid ImportFid(byte[] var1, Fid.Format var2) throws UareUException;

   Fmd ImportFmd(byte[] var1, Fmd.Format var2, Fmd.Format var3) throws UareUException;
}
