package com.spacecode.sdk.network;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.module.authentication.FingerprintReader;
//import com.spacecode.sdk.network.TcpClient;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.UsersService;
import com.spacecode.sdk.user.data.FingerIndex;
import com.spacecode.sdk.user.data.GrantType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
/*
final class TcpUsersService extends UsersService {

   private final TcpClient _tcpClient;


   public TcpUsersService(List modules, TcpClient tcpClient) {
      super(modules);
      this._tcpClient = tcpClient;
   }

   public boolean addUser(User user) {
      if(user == null) {
         return false;
      } else {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"adduser", user.serialize()});
            return te != null && te.length >= 2 && "adduser".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Adding user request timed out.", var3);
            return false;
         }
      }
   }

   public List addUsers(Collection users) {
      return super.addUsers(users);
   }

   public boolean removeUser(String username) {
      if(username != null && !"".equals(username.trim())) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"removeuser", username});
            return te != null && te.length == 2 && "removeuser".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Removing user request timed out.", var3);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean enrollFinger(String username, FingerIndex fingerIndex) throws TimeoutException {
      return this.enrollFinger(username, fingerIndex, true);
   }

   public boolean enrollFinger(String username, FingerIndex fingerIndex, boolean useMasterReader) throws TimeoutException {
      if(username != null && !"".equals(username.trim()) && fingerIndex != null) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(FingerprintReader.getEnrollingTimeoutDelay(), new String[]{"enrollfinger", username, String.valueOf(fingerIndex.getIndex()), String.valueOf(useMasterReader)});
            return te != null && te.length == 2 && "enrollfinger".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Enrolling finger request timed out.", var5);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean enrollFinger(String username, FingerIndex fingerIndex, String t) {
      if(username != null && !username.trim().isEmpty() && fingerIndex != null && t != null && !t.trim().isEmpty()) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(FingerprintReader.getEnrollingTimeoutDelay(), new String[]{"enrollfinger", username, String.valueOf(fingerIndex.getIndex()), String.valueOf(true), t});
            return te != null && te.length == 2 && "enrollfinger".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Enrolling finger request timed out.", var5);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean removeFingerprint(String username, FingerIndex fingerIndex) {
      if(username != null && !"".equals(username.trim()) && fingerIndex != null) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"removefingerprint", username, String.valueOf(fingerIndex.getIndex())});
            return te != null && te.length == 2 && "removefingerprint".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Removing fingerprint request timed out.", var4);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean updateBadgeNumber(String username, String badgeNumber) {
      if(username != null && !"".equals(username.trim())) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"updatebadge", username, badgeNumber});
            return te != null && te.length >= 2 && "updatebadge".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Updating Badge Number request timed out.", var4);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean updatePermission(String username, GrantType permission) {
      if(username != null && !"".equals(username.trim()) && permission != null) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"updatepermission", username, permission.name()});
            return te != null && te.length >= 2 && "updatepermission".equals(te[0])?"true".equals(te[1]):false;
         } catch (TimeoutException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Updating Permission request timed out.", var4);
            return false;
         }
      } else {
         return false;
      }
   }

   public User getUserByName(String username) {
      if(username != null && !"".equals(username.trim())) {
         try {
            String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"userbyname", username});
            return te != null && te.length >= 2 && "userbyname".equals(te[0])?User.deserialize(te[1].trim()):null;
         } catch (TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Getting user request timed out.", var3);
            return null;
         }
      } else {
         return null;
      }
   }

   public List getUsers() {
      ArrayList users = new ArrayList();

      try {
         String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"userslist"});
         if(te == null || te.length < 2 || !"userslist".equals(te[0])) {
            return users;
         }

         for(int i = 1; i < te.length; ++i) {
            User newUser = User.deserialize(te[i]);
            if(newUser != null) {
               users.add(newUser);
            }
         }
      } catch (TimeoutException var5) {
         SmartLogger.getLogger().log(Level.SEVERE, "Getting users list request timed out.", var5);
      }

      return users;
   }

   public List getUnregisteredUsers() {
      ArrayList unregisteredUsers = new ArrayList();

      try {
         String[] te = this._tcpClient.sendSynchronousRequest(new String[]{"usersunregistered"});
         if(te == null || te.length < 2 || !"usersunregistered".equals(te[0])) {
            return unregisteredUsers;
         }

         unregisteredUsers.addAll(Arrays.asList(Arrays.copyOfRange(te, 1, te.length)));
      } catch (TimeoutException var3) {
         SmartLogger.getLogger().log(Level.SEVERE, "Getting users list request timed out.", var3);
      }

      return unregisteredUsers;
   }
}*/
