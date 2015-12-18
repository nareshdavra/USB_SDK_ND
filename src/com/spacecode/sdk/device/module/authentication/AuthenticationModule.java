package com.spacecode.sdk.device.module.authentication;

import com.spacecode.sdk.device.Device;
import com.spacecode.sdk.device.module.Module;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.data.GrantType;

public abstract class AuthenticationModule extends Module {

   final boolean _isMaster;


   protected AuthenticationModule(String serialNumber, Device listeningDevice, boolean isMaster) {
      this(serialNumber, listeningDevice, (Device.EventDispatcher)null, isMaster);
   }

   protected AuthenticationModule(String serialNumber, Device listeningDevice, Device.EventDispatcher eventDispatcher, boolean isMaster) {
      super(serialNumber, listeningDevice);
      this._isMaster = isMaster;
      this._deviceEventDispatcher = eventDispatcher;
   }

   public boolean isMaster() {
      return this._isMaster;
   }

   protected void checkUserGrants(User user) {
      boolean isUserGranted = user.getPermission() == GrantType.ALL || user.getPermission() == GrantType.MASTER && this._isMaster || user.getPermission() == GrantType.SLAVE && !this._isMaster;
      if(isUserGranted) {
         this._deviceEventDispatcher.eventAuthenticationSuccess(this, user);
      } else {
         this._deviceEventDispatcher.eventAuthenticationFailure(this, user);
      }

   }
}
