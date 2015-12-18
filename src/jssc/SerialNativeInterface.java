package jssc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SerialNativeInterface {

   private static final String libVersion = "2.8";
   private static final String libMinorSuffix = "0";
   public static final int OS_LINUX = 0;
   public static final int OS_WINDOWS = 1;
   public static final int OS_SOLARIS = 2;
   public static final int OS_MAC_OS_X = 3;
   private static int osType = -1;
   public static final long ERR_PORT_BUSY = -1L;
   public static final long ERR_PORT_NOT_FOUND = -2L;
   public static final long ERR_PERMISSION_DENIED = -3L;
   public static final long ERR_INCORRECT_SERIAL_PORT = -4L;
   public static final String PROPERTY_JSSC_NO_TIOCEXCL = "JSSC_NO_TIOCEXCL";
   public static final String PROPERTY_JSSC_IGNPAR = "JSSC_IGNPAR";
   public static final String PROPERTY_JSSC_PARMRK = "JSSC_PARMRK";


   private static boolean isLibFolderExist(String var0) {
      boolean var1 = false;
      File var2 = new File(var0);
      if(var2.exists() && var2.isDirectory()) {
         var1 = true;
      }

      return var1;
   }

   private static boolean isLibFileExist(String var0) {
      boolean var1 = false;
      File var2 = new File(var0);
      if(var2.exists() && var2.isFile()) {
         var1 = true;
      }

      return var1;
   }

   private static boolean extractLib(String var0, String var1, String var2) {
      boolean var3 = false;
      File var4 = new File(var0);
      InputStream var5 = null;
      FileOutputStream var6 = null;
      var5 = SerialNativeInterface.class.getResourceAsStream("/libs/" + var1 + "/" + var2);
      if(var5 != null) {
         byte[] var8 = new byte[4096];

         try {
            var6 = new FileOutputStream(var0);

            int var7;
            while((var7 = var5.read(var8)) != -1) {
               var6.write(var8, 0, var7);
            }

            var6.close();
            var5.close();
            var3 = true;
         } catch (Exception var13) {
            try {
               var6.close();
               if(var4.exists()) {
                  var4.delete();
               }
            } catch (Exception var12) {
               ;
            }

            try {
               var5.close();
            } catch (Exception var11) {
               ;
            }
         }
      }

      return var3;
   }

   public static int getOsType() {
      return osType;
   }

   public static String getLibraryVersion() {
      return "2.8.0";
   }

   public static String getLibraryBaseVersion() {
      return "2.8";
   }

   public static String getLibraryMinorSuffix() {
      return "0";
   }

   public static native String getNativeLibraryVersion();

   public native long openPort(String var1, boolean var2);

   public native boolean setParams(long var1, int var3, int var4, int var5, int var6, boolean var7, boolean var8, int var9);

   public native boolean purgePort(long var1, int var3);

   public native boolean closePort(long var1);

   public native boolean setEventsMask(long var1, int var3);

   public native int getEventsMask(long var1);

   public native int[][] waitEvents(long var1);

   public native boolean setRTS(long var1, boolean var3);

   public native boolean setDTR(long var1, boolean var3);

   public native byte[] readBytes(long var1, int var3);

   public native boolean writeBytes(long var1, byte[] var3);

   public native int[] getBuffersBytesCount(long var1);

   public native boolean setFlowControlMode(long var1, int var3);

   public native int getFlowControlMode(long var1);

   public native String[] getSerialPortNames();

   public native int[] getLinesStatus(long var1);

   public native boolean sendBreak(long var1, int var3);

   static {
      String var2 = System.getProperty("os.name");
      String var3 = System.getProperty("os.arch");
      String var4 = System.getProperty("user.home");
      String var5 = System.getProperty("file.separator");
      String var6 = System.getProperty("java.io.tmpdir");
      String var7 = (new File(var4)).canWrite()?var4:var6;
      String var8 = System.getProperty("java.library.path");
      if(var2.equals("Linux")) {
         var2 = "linux";
         osType = 0;
      } else if(var2.startsWith("Win")) {
         var2 = "windows";
         osType = 1;
      } else if(var2.equals("SunOS")) {
         var2 = "solaris";
         osType = 2;
      } else if(var2.equals("Mac OS X") || var2.equals("Darwin")) {
         var2 = "mac_os_x";
         osType = 3;
      }

      if(!var3.equals("i386") && !var3.equals("i686")) {
         if(!var3.equals("amd64") && !var3.equals("universal")) {
            if(var3.equals("arm")) {
               String var9 = "sf";
               if(!var8.toLowerCase().contains("gnueabihf") && !var8.toLowerCase().contains("armhf")) {
                  try {
                     Process var10 = Runtime.getRuntime().exec("readelf -A " + System.getProperty("java.home") + "/bin/java");
                     BufferedReader var11 = new BufferedReader(new InputStreamReader(var10.getInputStream()));
                     String var12 = "";

                     while((var12 = var11.readLine()) != null && !var12.isEmpty()) {
                        if(var12.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())) {
                           var9 = "hf";
                           break;
                        }
                     }

                     var11.close();
                  } catch (Exception var13) {
                     ;
                  }
               } else {
                  var9 = "hf";
               }

               var3 = "arm" + var9;
            }
         } else {
            var3 = "x86_64";
         }
      } else {
         var3 = "x86";
      }

      String var0 = var7 + var5 + ".jssc" + var5 + var2;
      String var1 = "jSSC-2.8_" + var3;
      var1 = System.mapLibraryName(var1);
      if(var1.endsWith(".dylib")) {
         var1 = var1.replace(".dylib", ".jnilib");
      }

      boolean var14 = false;
      if(isLibFolderExist(var0)) {
         if(isLibFileExist(var0 + var5 + var1)) {
            var14 = true;
         } else if(extractLib(var0 + var5 + var1, var2, var1)) {
            var14 = true;
         }
      } else if((new File(var0)).mkdirs() && extractLib(var0 + var5 + var1, var2, var1)) {
         var14 = true;
      }

      if(var14) {
         System.load(var0 + var5 + var1);
         String var15 = getLibraryBaseVersion();
         String var16 = getNativeLibraryVersion();
         if(!var15.equals(var16)) {
            System.err.println("Warning! jSSC Java and Native versions mismatch (Java: " + var15 + ", Native: " + var16 + ")");
         }
      }

   }
}
