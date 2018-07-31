/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.saver;

import com.altaik.bo.ProductsOfCompany;
import com.altaik.db.IDatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class ProductsOfCompanySaver extends Saver<ProductsOfCompany> implements ISaver<ProductsOfCompany> {

    public ProductsOfCompanySaver(IDatabaseManager dbManager) {
        super(dbManager);
    }

    @Override
    protected int CheckExist(ProductsOfCompany objectForSave, IDatabaseManager databaseManager) {
        ResultSet res = databaseManager.Execute("SELECT id FROM goodscompany WHERE companyid = '" + objectForSave.companyId + "' and goodid='" + objectForSave.productId + "';");
        try {
            if (res != null && res.first()) {
                return res.getInt("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    protected Query GetQuery(ProductsOfCompany objectForSave, IDatabaseManager databaseManager) {
        Query query = new Query("goodscompany");
        query.put("companyid", objectForSave.companyId);
        query.put("goodid", objectForSave.productId);
        return query;
    }

}
