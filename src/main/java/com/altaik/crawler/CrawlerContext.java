/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.crawler;

import com.altaik.bp.proxy.Proxies;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aset
 */
public class CrawlerContext {
    public Proxies proxies;
    
    public CrawlerContext(Proxies proxies){
        if(proxies==null) Logger.getLogger(CrawlerContext.class.getName()).log(Level.WARNING, "ProxyList is null.");
        this.proxies = proxies;
    }
}
