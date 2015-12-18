package com.spacecode.sdk.user;

import com.spacecode.sdk.device.module.Module;
import com.spacecode.sdk.device.module.authentication.FingerprintReader;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.data.FingerIndex;
import com.spacecode.sdk.user.data.GrantType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class UsersService {

   private final Pattern _usernameRegex = Pattern.compile("^[A-zÀàÂâÄäÈèÉéÊêËëÏïÎîÔôÖöÙùÛûÜüÇç][A-z0-9_\\.\\-\\sÀàÂâÄäÈèÉéÊêËëÏïÎîÔôÖöÙùÛûÜüÇç]{2,249}$");
   private final Map _grantedUsers = new HashMap();
   private final Map _unregisteredUsers = new HashMap();
   private final List _modules;


   public UsersService(List modules) {
      this._modules = modules;
   }

   public boolean enrollFinger(String username, FingerIndex fingerIndex) throws TimeoutException {
      return this.enrollFinger(username, fingerIndex, true);
   }

   public boolean enrollFinger(String username, FingerIndex fingerIndex, boolean useMasterReader) throws TimeoutException {
      User user = (User)this._grantedUsers.get(username);
      if(username != null && !"".equals(username.trim()) && user != null) {
         FingerprintReader fpReader = this.getReader(useMasterReader);
         if(fpReader == null) {
            return false;
         } else {
            String template = fpReader.enrollFinger();
            if(template == null) {
               return false;
            } else {
               user.setFingerprintTemplate(fingerIndex, template);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Deprecated
   public boolean enrollFinger(String username, FingerIndex fingerIndex, String t) {
      User user = (User)this._grantedUsers.get(username);
      if(username != null && !"".equals(username.trim()) && user != null) {
         if(t != null && !t.trim().isEmpty()) {
            user.setFingerprintTemplate(fingerIndex, t);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean removeFingerprint(String username, FingerIndex fingerIndex) {
      User user = this.getUserByName(username);
      if(user != null && fingerIndex != null) {
         user.setFingerprintTemplate(fingerIndex, (String)null);
         return true;
      } else {
         return false;
      }
   }

   public FingerIndex getLastVerifiedFingerIndex(boolean master) {
      FingerprintReader fpReader = this.getReader(master);
      return fpReader == null?null:fpReader.getLastVerifiedFingerIndex();
   }

   public FingerIndex getLastVerifiedFingerIndex() {
      return this.getLastVerifiedFingerIndex(true);
   }

   public boolean updateBadgeNumber(String username, String badgeNumber) {
      User user = this.getUserByName(username);
      if(user == null) {
         return false;
      } else {
         user.setBadgeNumber(badgeNumber);
         return true;
      }
   }

   public boolean updatePermission(String username, GrantType permission) {
      User user = this.getUserByName(username);
      if(user == null) {
         user = this.getUnregisteredUserByName(username);
         if(user == null) {
            return false;
         }

         this._unregisteredUsers.remove(username);
         if(!this.addUser(user)) {
            this._unregisteredUsers.put(username, user);
            return false;
         }
      }

      user.setPermission(permission);
      return true;
   }

   public boolean addUser(User user) {
      if(user == null) {
         return false;
      } else {
         String name = user.getUsername();
         if(name != null && !name.trim().isEmpty() && !this._grantedUsers.containsKey(name) && !this._unregisteredUsers.containsKey(name)) {
            this._grantedUsers.put(user.getUsername(), user);
            return true;
         } else {
            return false;
         }
      }
   }

   public List addUsers(Collection users) {
      ArrayList notAddedUsers = new ArrayList();
      if(users == null) {
         return notAddedUsers;
      } else {
         LinkedHashSet users1 = new LinkedHashSet(users);
         Iterator i$ = users1.iterator();

         while(i$.hasNext()) {
            User user = (User)i$.next();
            if(user != null && !this.addUser(user)) {
               notAddedUsers.add(user);
            }
         }

         return notAddedUsers;
      }
   }

   public boolean removeUser(String username) {
      if(username != null && !"".equals(username.trim())) {
         Iterator it = this._grantedUsers.entrySet().iterator();

         User user;
         do {
            if(!it.hasNext()) {
               return false;
            }

            Entry entry = (Entry)it.next();
            user = (User)entry.getValue();
         } while(user == null || !username.equals(user.getUsername()));

         this._unregisteredUsers.put(username, user);
         it.remove();
         return true;
      } else {
         return false;
      }
   }

   public User getUserByName(String username) {
      return username != null && !"".equals(username.trim())?(User)this._grantedUsers.get(username):null;
   }

   public List<User> getUsers() {
      return new ArrayList(this._grantedUsers.values());
   }

   public List<String> getUnregisteredUsers() {
      return new ArrayList(this._unregisteredUsers.keySet());
   }

   private User getUnregisteredUserByName(String username) {
      return username != null && !"".equals(username.trim())?(User)this._unregisteredUsers.get(username):null;
   }

   private FingerprintReader getReader(boolean masterReader) {
      Iterator i$ = this._modules.iterator();

      while(i$.hasNext()) {
         Module module = (Module)i$.next();
         if(module instanceof FingerprintReader) {
            FingerprintReader currentFpReader = (FingerprintReader)module;
            if(currentFpReader.isMaster() == masterReader) {
               return currentFpReader;
            }
         }
      }

      return null;
   }
}
