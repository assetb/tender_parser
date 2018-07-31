/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.db.IDatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aset
 */
public class ParserSettings {
    private static final Logger logger = Logger.getLogger(ParserSettings.class.getName());
    
    private final IDatabaseManager dbManager;
    
    public ParserSettings(IDatabaseManager dbManager){
        this.dbManager = dbManager;
    }
    
    public ArrayList<String[]> getSites() {
        String query = "select * from sites";
        return (ArrayList<String[]>) dbManager.Execute(query, new String[]{"siteid", "sitename"});
    }
    
    public ArrayList<String[]> getLinks(String siteid) {
        String query = "select link from links where siteid = " + "'" + siteid + "'";
        ResultSet table = dbManager.Execute(query);
        ArrayList<String[]> result = null;
        try {
            while(table.next()){
                if(result == null){
                    result = new ArrayList<>();
                }
                String[] item = new String[2];
                item[0] = table.getString("siteid");
                item[1] = table.getString("link");
                result.add(item);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
