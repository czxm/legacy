package com.intel.cedar.util;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intel.cedar.core.CedarException;

public class EntityWrapper<TYPE> {

    static Logger LOG = LoggerFactory.getLogger(EntityWrapper.class);
    private TxHandle tx;
    private static final boolean TRACE = true;

    public EntityWrapper() {
        this("cedar_general");
    }

    @SuppressWarnings("unchecked")
    public EntityWrapper(String persistenceContext) {
        try {
            if (TRACE)
                LOG.trace(DbEvent.CREATE.begin());
            this.tx = new TxHandle(persistenceContext);
        } catch (Throwable e) {
            if (TRACE)
                LOG.trace(DbEvent.CREATE.fail());
            this.exceptionCaught(e);
            throw (RuntimeException) e;
        }
        if (TRACE)
            LOG.trace(DbEvent.CREATE.end(), Long.toString(tx.splitOperation()),
                    tx.getTxUuid());
    }

    @SuppressWarnings("unchecked")
    public List<TYPE> query(TYPE example) {
        if (TRACE)
            LOG.trace(DbEvent.QUERY.begin(), tx.getTxUuid());
        Example qbe = Example.create(example).enableLike(MatchMode.EXACT);
        List<TYPE> resultList = (List<TYPE>) this.getSession().createCriteria(
                example.getClass()).setCacheable(true).add(qbe).list();
        if (TRACE)
            LOG.trace(DbEvent.QUERY.end(), Long.toString(tx.splitOperation()),
                    tx.getTxUuid());
        return Lists.newArrayList(Sets.newHashSet(resultList));
    }

    public TYPE load(Class<?> clz, Serializable id) {
        return (TYPE) tx.getSession().get(clz, id);
    }

    public TYPE getUnique(TYPE example) throws CedarException {
        if (TRACE)
            LOG.trace(DbEvent.UNIQUE.begin(), tx.getTxUuid());
        List<TYPE> res = this.query(example);
        if (res.size() != 1) {
            String msg = null;
            try {
                msg = example.toString();
            } catch (Exception e) {
                msg = example.toString();
            }
            if (TRACE)
                LOG.trace(DbEvent.QUERY.fail(), Long.toString(tx
                        .splitOperation()), tx.getTxUuid());
            throw new CedarException("Error locating information for " + msg);
        }
        if (TRACE)
            LOG.trace(DbEvent.QUERY.end(), Long.toString(tx.splitOperation()),
                    tx.getTxUuid());
        return res.get(0);
    }

    @SuppressWarnings("unchecked")
    private void exceptionCaught(Throwable cause) {
        LOG.error(cause.getMessage(), cause);
    }

    public void add(TYPE newObject) {
        this.getEntityManager().persist(newObject);
        EntityNotifier.getInstance().newEvent(this,
                EntityNotifier.Operation.Add, newObject);
    }

    public void merge(TYPE newObject) {
        this.getEntityManager().merge(newObject);
        EntityNotifier.getInstance().newEvent(this,
                EntityNotifier.Operation.Update, newObject);
    }

    public void mergeAndCommit(TYPE newObject) {
        this.getEntityManager().merge(newObject);
        EntityNotifier.getInstance().newEvent(this,
                EntityNotifier.Operation.Update, newObject);
        this.commit();
    }

    public void delete(TYPE deleteObject) {
        this.getEntityManager().remove(deleteObject);
        EntityNotifier.getInstance().newEvent(this,
                EntityNotifier.Operation.Delete, deleteObject);
    }

    public void rollback() {
        if (TRACE)
            LOG.trace(DbEvent.ROLLBACK.begin(), tx.getTxUuid());
        try {
            this.tx.rollback();
        } catch (Throwable e) {
            if (TRACE)
                LOG.trace(DbEvent.ROLLBACK.fail(), Long.toString(tx
                        .splitOperation()), tx.getTxUuid());
            this.exceptionCaught(e);
        }
        if (TRACE)
            LOG.trace(DbEvent.ROLLBACK.end(), Long
                    .toString(tx.splitOperation()), tx.getTxUuid());
        EntityNotifier.getInstance().clearEvents(this);
    }

    public void commit() {
        if (TRACE)
            LOG.trace(DbEvent.COMMIT.begin(), tx.getTxUuid());
        try {
            this.tx.commit();
        } catch (Throwable e) {
            if (TRACE)
                LOG.trace(DbEvent.COMMIT.fail(), Long.toString(tx
                        .splitOperation()), tx.getTxUuid());
            this.exceptionCaught(e);
            throw (RuntimeException) e;
        }
        if (TRACE)
            LOG.trace(DbEvent.COMMIT.end(), Long.toString(tx.splitOperation()),
                    tx.getTxUuid());
        EntityNotifier.getInstance().notifyChanges(this);
    }

    public Session getSession() {
        return tx.getSession();
    }

    public EntityManager getEntityManager() {
        return tx.getEntityManager();
    }

    @SuppressWarnings("unchecked")
    public <NEWTYPE> EntityWrapper<NEWTYPE> recast(Class<NEWTYPE> c) {
        return (EntityWrapper<NEWTYPE>) this;
    }

    public static StackTraceElement getMyStackTraceElement() {
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (i++ < 2 || ste.getClassName().matches(".*EntityWrapper.*")
                    || ste.getClassName().matches(".*TxHandle.*")
                    || ste.getMethodName().equals("getEntityWrapper")) {
                continue;
            } else {
                return ste;
            }
        }
        throw new RuntimeException(
                "BUG: Reached bottom of stack trace without finding any relevent frames.");
    }

    enum DbEvent {
        CREATE, COMMIT, ROLLBACK, UNIQUE, QUERY;
        public String fail() {
            return this.name() + ":FAIL";
        }

        public String begin() {
            return this.name() + ":BEGIN";
        }

        public String end() {
            return this.name() + ":END";
        }

        public String getMessage() {
            if (TRACE) {
                return EntityWrapper.getMyStackTraceElement().toString();
            } else {
                return "n.a";
            }
        }
    }

}
