package com.spacecode.sdk.network;

import java.util.Arrays;
import java.util.List;

public class DbConfiguration {

   private String _host;
   private int _port;
   private String _name;
   private String _user;
   private String _password;
   private String _dbms;
   public static final String MySQL = "mysql";
   public static final String PostgreSQL = "postgresql";
   public static final String SQL_Server = "sqlserver";
   private static final List ALLOWED_DBMS = Arrays.asList(new String[]{"mysql", "postgresql", "sqlserver"});


   public DbConfiguration(String host, int port, String name, String user, String password, String dbms) {
      if(!ALLOWED_DBMS.contains(dbms)) {
         throw new IllegalArgumentException("Invalid DBMS: only MySQL, PostgreSQL and SQL Server are supported.");
      } else if(host != null && !host.trim().isEmpty()) {
         if(name != null && !name.trim().isEmpty()) {
            if(user != null && !user.trim().isEmpty()) {
               if(port >= 1 && port <= '\uffff') {
                  this._host = host;
                  this._port = port;
                  this._name = name;
                  this._user = user;
                  this._password = password == null?"":password;
                  this._dbms = dbms;
               } else {
                  throw new IllegalArgumentException("Invalid TCP port number");
               }
            } else {
               throw new IllegalArgumentException("Invalid username");
            }
         } else {
            throw new IllegalArgumentException("Invalid database name");
         }
      } else {
         throw new IllegalArgumentException("Invalid host");
      }
   }

   public void setHost(String host) {
      if(host != null && !host.trim().isEmpty()) {
         this._host = host;
      } else {
         throw new IllegalArgumentException("Invalid host");
      }
   }

   public String getHost() {
      return this._host;
   }

   public void setPort(int port) {
      if(port >= 1 && port <= '\uffff') {
         this._port = port;
      } else {
         throw new IllegalArgumentException("Invalid TCP port number");
      }
   }

   public int getPort() {
      return this._port;
   }

   public void setName(String name) {
      if(name != null && !name.trim().isEmpty()) {
         this._name = name;
      } else {
         throw new IllegalArgumentException("Invalid database name");
      }
   }

   public String getName() {
      return this._name;
   }

   public void setUser(String username) {
      if(username != null && !username.trim().isEmpty()) {
         this._user = username;
      } else {
         throw new IllegalArgumentException("Invalid username");
      }
   }

   public String getUser() {
      return this._user;
   }

   public void setPassword(String password) {
      this._password = password == null?"":password;
   }

   public String getPassword() {
      return this._password;
   }

   public void setDbms(String dbms) {
      if(!ALLOWED_DBMS.contains(dbms)) {
         throw new IllegalArgumentException("Invalid DBMS: only MySQL, PostgreSQL and SQL Server are supported.");
      } else {
         this._dbms = dbms;
      }
   }

   public String getDbms() {
      return this._dbms;
   }

}
