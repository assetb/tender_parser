/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.saver;

import com.altaik.bo.Company;
import com.altaik.db.IDatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class CompanySaver extends Saver<Company> implements ISaver<Company> {

    public CompanySaver(IDatabaseManager dbManager) {
        super(dbManager);
    }

    @Override
    protected int CheckExist(Company objectForSave, IDatabaseManager databaseManager) {
        ResultSet res = databaseManager.Execute("SELECT id FROM companyinfo WHERE bin = '" + objectForSave.bin + "';");
        try {
            if (res != null && res.first()) {
                return res.getInt("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CompanySaver.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    protected Query GetQuery(Company objectForSave, IDatabaseManager databaseManager) {

        Query query = new Query("companyinfo");

        query.put("bin", objectForSave.bin);
        query.put("runame", objectForSave.runame);
        query.put("kzname", objectForSave.kzname);
        query.put("email", objectForSave.email);
        query.put("tel", objectForSave.tel);
        query.put("region", objectForSave.region);
        query.put("address", objectForSave.address);
        query.put("source", objectForSave.source);
        return query;
    }

}
