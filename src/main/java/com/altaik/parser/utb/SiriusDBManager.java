/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.utb;

import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.bo.Purchases;
import com.altaik.db.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class SiriusDBManager extends DatabaseManager {

    private final String NAME_PURCHASE = "Закупки на товарной бирже АО \"УТБ Астана\"";

    SiriusDBManager(Properties props) {
        super(props);
    }

    private ArrayList<Lot> GetLots(int idPurchase, String negNumber) throws SQLException {
        ResultSet rs = Execute("SELECT * FROM lots_view WHERE id_auction = " + idPurchase + ";");
        ArrayList lots = new ArrayList<>();
        while (rs.next()) {
            Lot l = new Lot();
            l.lotNumber = rs.getString("number");
            l.ruName = rs.getString("description");
            l.unit = rs.getString("measure");
            l.quantity = rs.getString("amount");
            l.price = rs.getString("price");
            l.sum = rs.getString("sum");
            l.deliveryTerms = rs.getString("payment_term");
            l.deliveryPlace = rs.getString("delivery_place");
            l.deliverySchedule = rs.getString("delivery_time");
            l.purchaseNumber = negNumber;
            lots.add(l);
        }
        return lots;
    }

    public Purchases GetPurchases() {
        ResultSet rs = Execute("SELECT id, number, date, customer, type FROM active_auctions_view;");
        try {
            Purchases purchases = new Purchases();
            while (rs.next()) {
                Purchase p = new Purchase();

                p.setNumber(rs.getString("number"));
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                p.startDay = df.format(new Date());
                p.endDay = rs.getString("date");
                p.ruName = NAME_PURCHASE;
                p.method = rs.getString("type");
                p.customer = rs.getString("customer");
                p.lots = GetLots(rs.getInt("id"), p.getNumber());
                purchases.add(p);
            }
            
            return purchases;
        } catch (SQLException ex) {
            Logger.getLogger(SiriusDBManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
