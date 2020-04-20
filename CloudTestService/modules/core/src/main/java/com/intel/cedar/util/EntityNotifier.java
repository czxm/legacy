package com.intel.cedar.util;

import java.util.ArrayList;
import java.util.HashMap;

public class EntityNotifier {
    private static EntityNotifier singleton;

    public static EntityNotifier getInstance() {
        if (singleton == null)
            singleton = new EntityNotifier();
        return singleton;
    }

    public enum Operation {
        Add, Delete, Update
    }

    class EntityEvent {
        Operation op;
        Object e;

        EntityEvent(Operation op, Object e) {
            this.op = op;
            this.e = e;
        }
    }

    private HashMap<String, ArrayList<EntityListener>> listeners;
    private HashMap<EntityWrapper, ArrayList<EntityEvent>> events;

    private EntityNotifier() {
        listeners = new HashMap<String, ArrayList<EntityListener>>(5);
        events = new HashMap<EntityWrapper, ArrayList<EntityEvent>>();
    }

    public synchronized void addListener(Class<?> clz, EntityListener listener) {
        if (listener != null) {
            ArrayList<EntityListener> list = listeners.get(clz
                    .getCanonicalName());
            if (list == null) {
                list = new ArrayList<EntityListener>(5);
                listeners.put(clz.getCanonicalName(), list);
            }
            list.add(listener);
        }
    }

    public synchronized void removeListener(Class<?> clz,
            EntityListener listener) {
        if (listener != null) {
            ArrayList<EntityListener> list = listeners.get(clz
                    .getCanonicalName());
            if (list != null) {
                list.remove(listener);
            }
        }
    }

    private ArrayList<EntityListener> getListeners(Class<?> clz) {
        ArrayList<EntityListener> list = listeners.get(clz.getCanonicalName());
        if (list == null) {
            list = new ArrayList<EntityListener>(5);
            listeners.put(clz.getCanonicalName(), list);
        }
        return list;
    }

    private void notifyEntityAdded(Object e) {
        for (EntityListener l : getListeners(e.getClass())) {
            l.entityAdded(e);
        }
    }

    private void notifyEntityDeleted(Object e) {
        for (EntityListener l : getListeners(e.getClass())) {
            l.entityDeleted(e);
        }
    }

    private void notifyEntityUpdated(Object e) {
        for (EntityListener l : getListeners(e.getClass())) {
            l.entityUpdated(e);
        }
    }

    public synchronized void newEvent(EntityWrapper db, Operation op, Object e) {
        ArrayList<EntityEvent> list = this.events.get(db);
        if (list == null) {
            list = new ArrayList<EntityEvent>();
            this.events.put(db, list);
        }
        list.add(new EntityEvent(op, e));
    }

    public void notifyChanges(EntityWrapper db) {
        ArrayList<EntityEvent> list = this.events.get(db);
        if (list == null) {
            return;
        }
        for (EntityEvent e : list) {
            if (e.op == Operation.Add) {
                notifyEntityAdded(e.e);
            } else if (e.op == Operation.Delete) {
                notifyEntityDeleted(e.e);
            } else if (e.op == Operation.Update) {
                notifyEntityUpdated(e.e);
            }
        }
        clearEvents(db);
    }

    public synchronized void clearEvents(EntityWrapper db) {
        this.events.remove(db);
    }
}
