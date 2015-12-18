package com.spacecode.sdk.network;

import com.spacecode.sdk.user.data.AccessType;
import java.util.Date;

public class Authentication {

   private String _username;
   private AccessType _accessType;
   private Date _date;


   public Authentication(String username, AccessType accessType, Date date) {
      this._username = username;
      this._accessType = accessType;
      this._date = new Date(date.getTime());
   }

   public String getUsername() {
      return this._username;
   }

   public AccessType getAccessType() {
      return this._accessType;
   }

   public Date getDate() {
      return new Date(this._date.getTime());
   }
}
