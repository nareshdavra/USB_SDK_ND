package com.spacecode.sdk.network.alert;


public class SmtpServer {

   private String _address;
   private int _port;
   private String _username;
   private String _password;
   private boolean _sslEnabled;


   public SmtpServer(String address, int port, String username, String password, boolean sslEnabled) {
      if(address != null && !address.trim().isEmpty()) {
         if(port >= 1 && port <= '\uffff') {
            this._address = address;
            this._port = port;
            this._username = username == null?"":username;
            this._password = password == null?"":password;
            this._sslEnabled = sslEnabled;
         } else {
            throw new IllegalArgumentException("Invalid TCP port number");
         }
      } else {
         throw new IllegalArgumentException("Invalid address");
      }
   }

   public String getAddress() {
      return this._address;
   }

   public void setAddress(String address) {
      if(address != null && !address.trim().isEmpty()) {
         this._address = address;
      } else {
         throw new IllegalArgumentException("Invalid address");
      }
   }

   public int getPort() {
      return this._port;
   }

   public void setPort(int port) {
      if(port >= 1 && port <= '\uffff') {
         this._port = port;
      } else {
         throw new IllegalArgumentException("Invalid TCP port number");
      }
   }

   public String getUsername() {
      return this._username;
   }

   public void setUsername(String username) {
      this._username = username == null?"":username;
   }

   public String getPassword() {
      return this._password;
   }

   public void setPassword(String password) {
      this._password = password == null?"":password;
   }

   public boolean isSslEnabled() {
      return this._sslEnabled;
   }

   public void setSslEnabled(boolean sslEnabled) {
      this._sslEnabled = sslEnabled;
   }
}
