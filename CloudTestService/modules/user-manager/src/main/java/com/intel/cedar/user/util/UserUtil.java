package com.intel.cedar.user.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.CedarException;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.user.SessionInfo;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public class UserUtil {
    private static Logger LOG = LoggerFactory.getLogger(UserUtil.class);

    static{
        CedarTimer.getInstance().scheduleTask(60, new CedarTimerTask("Session Cleaner"){
            @Override
            public void run() {
                UserUtil.purgeSessions();
            }
        });
    }
    
    public static boolean isRegistered(UserInfo user, boolean login) {
        UserInfo info = new UserInfo();
        UserInfo mailInfo = new UserInfo();
        if (login) {
            info.setUser(user.getUser());
            info.setPassword(user.getPassword());
        } else {
            info.setUser(user.getUser());
            mailInfo.setEmail(user.getEmail());
        }
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            if (db.query(info).size() > 0)
                return true;
            else
                return false;
            /*
             * if(db.query(info).size()>0){ return true; } if(!login &&
             * db.query(mailInfo).size()>0){ return true; }else{ return false; }
             */
        } finally {
            db.rollback();
        }
    }

    public static boolean registerUser(UserInfo user) {
        if (isRegistered(user, false)) {
            return false;
        }

        EntityWrapper<UserInfo> entityWrapper = EntityUtil
                .getUserEntityWrapper();
        try {
            entityWrapper.add(user);
            // TODO: send email notification
            return true;
        } catch (Exception e) {

        } finally {
            entityWrapper.commit();
        }

        return false;
    }

    public static UserInfo loadUser(Long id) {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            return db.load(UserInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public static UserInfo loadUser(String name) {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            for (UserInfo user : db.query(new UserInfo())) {
                if (user.getUser().equals(name))
                    return user;
            }
            return null;
        } finally {
            db.rollback();
        }
    }

    public static boolean saveSession(SessionInfo session) {
        EntityWrapper<SessionInfo> db = EntityUtil.getSessionEntityWrapper();
        try {
            db.add(session);
            return true;
        } catch (Exception e) {

        } finally {
            db.commit();
        }
        return false;
    }

    public static void updateSession(SessionInfo session) {

    }

    public static void purgeSessions() {
        EntityWrapper<SessionInfo> db = EntityUtil.getSessionEntityWrapper();
        try {
            for(SessionInfo s : db.query(new SessionInfo())){
                if(s.getTimestamp() == null || 
                   s.getTimestamp() + 3 * 24 * 3600 * 1000 < System.currentTimeMillis()){
                    db.delete(s);
                }
            }
            
        } catch (Exception e) {

        } finally {
            db.commit();
        }
    }

    public static UserInfo getSessionBindUser(String secret) {
        SessionInfo sessionInfo = getSession(secret);
        if (sessionInfo == null) {
            return null;
        } else {
            Long userid = sessionInfo.getUserid();
            return loadUser(userid);
        }
    }

    public static SessionInfo getSession(String secret) {
        EntityWrapper<SessionInfo> db = EntityUtil.getSessionEntityWrapper();
        SessionInfo info = new SessionInfo();
        info.setSessionid(secret);
        try {
            List<SessionInfo> list = db.query(info);
            if (list.size() == 1) {
                return list.get(0);
            }
        } finally {
            db.rollback();
        }
        return null;
    }

    public static UserInfo loginUser(UserInfo user) {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        UserInfo info = new UserInfo();
        info.setUser(user.getUser());
        info.setPassword(user.getPassword());
        try {
            List<UserInfo> list = db.query(info);
            if (list.size() > 0)
                return list.get(0);
        } finally {
            db.rollback();
        }
        return null;
    }

    public static UserInfo getAdmin() {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        UserInfo info = new UserInfo();
        info.setAdmin(true);
        UserInfo user = null;
        try {
            user = db.getUnique(info);
        } catch (CedarException e) {
        } finally {
            db.rollback();
        }
        return user;
    }

    public static UserInfo getUserById(Long id) {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            return db.load(UserInfo.class, id);
        } catch (Exception e) {
        } finally {
            db.rollback();
        }
        return null;
    }

    public static List<UserInfo> listUsers() {
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            return db.query(new UserInfo());
        } finally {
            db.rollback();
        }
    }

    public static boolean deleteUser(UserInfo user) {
        if (!isRegistered(user, false)) {
            LOG.error("trying to delete a non-existent user:" + user);
            return false;
        }
        EntityWrapper<UserInfo> db = EntityUtil.getUserEntityWrapper();
        try {
            db.delete(db.getUnique(user));
            return true;
        } catch (CedarException e) {
            e.printStackTrace();
        } finally {
            db.commit();
        }

        return false;
    }
}
