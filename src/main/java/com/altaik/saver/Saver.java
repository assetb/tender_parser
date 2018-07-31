/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.saver;

import com.altaik.db.IDatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 * @param <T>
 */
public abstract class Saver<T> implements ISaver<T> {

    private static final Logger LOG = Logger.getLogger(Saver.class.getName());

    private final IDatabaseManager dbManager;

    public Saver(IDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    protected abstract int CheckExist(T bo, IDatabaseManager databaseManager);

    protected abstract Query GetQuery(T bo, IDatabaseManager databaseManager);

    /**
     *
     * @param objectForSave
     * @return
     */
    @Override
    public Query Do(T objectForSave) {
        Query query = GetQuery(objectForSave, dbManager);
        int id = CheckExist(objectForSave, dbManager);
        if (id > 0) {
            query.pk = id;
        } else {
            Insert(query, null);
        }
        return query;
    }

    private void Insert(Query query, Object fk) {
        if (fk != null) {
            query.setFk(fk);
        }
        String queryStr = query.toString();
        if (queryStr != null && dbManager.Insert(queryStr)) {

            ResultSet lastId = dbManager.getLastInsertId();
            try {
                if (null != lastId && lastId.first()) {
                    query.pk = lastId.getInt(1);

                    if (query.querys != null && !query.querys.isEmpty()) {
                        query.querys.stream().forEach((q) -> {
                            Insert(q, query.pk);
                        });
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Saver.class.getName()).log(Level.SEVERE, "Error insert", ex);
            }
        }
    }

    @Override
    public void Close() {
    }

}
