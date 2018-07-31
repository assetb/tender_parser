/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.saver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author admin
 */
public class Query extends HashMap<String, Object> {

    private final String tableName;
    private String where;
    private Object fk;
    public int pk;

    public static enum Type {
        INSERT, INSERT_OR_UPDATE, INSERT_IF_NOT_EXISTS;
    }

    public Type type = Type.INSERT;

    public List<Query> querys = new ArrayList<>();

    public void setFk(Object fk) {
        this.fk = fk;
    }
    private String fkColumn;

    public String getFkColumn() {
        return fkColumn;
    }

    public Query(String tableName, String fkColumn) {
        this(tableName, null, fkColumn);
    }

    public Query(String tableName) {
        this(tableName, null, null);
    }

    public Query(String tableName, String where, String fkColumn) {
        super();
        this.tableName = tableName;
        this.where = where;
        this.fkColumn = fkColumn;

    }

    private String Insert() {
        String columns = "";
        String values = "";
        if (fkColumn != null) {
            columns += (columns.length() == 0 ? "" : ", ") + fkColumn;
            values += (values.length() == 0 ? "" : ", ") + "'" + fk + "'";
        }
        for (Map.Entry entry : this.entrySet()) {
            columns += (columns.length() == 0 ? "" : ", ") + entry.getKey();
            values += (values.length() == 0 ? "" : ", ") + "'" + entry.getValue() + "'";
        }
        return "INSERT INTO " + tableName + "(" + columns + ") VALUE(" + values + ")" + (where != null ? " " + where : "");
    }

    private String GetQueryInsertOrUpdate() {
        String query = Insert();

        String values = "";
        for (Map.Entry entry : this.entrySet()) {
            values += (values.length() == 0 ? "" : ", ") + entry.getKey() + "='" + entry.getValue() + "'";
        }
        return query + " ON DUPLICATE KEY UPDATE " + values;
    }

    @Override
    public String toString() {
        String query = null;
        switch (type) {
            case INSERT: {
                query = Insert();
            }
            break;
            case INSERT_OR_UPDATE: {
                query = GetQueryInsertOrUpdate();
            }
            break;

        }
        return query + ";";
    }
}
