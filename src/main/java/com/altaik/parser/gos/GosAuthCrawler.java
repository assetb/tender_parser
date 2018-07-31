/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gos;

import com.altaik.crawler.AuthCrawler;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Aset
 */
public class GosAuthCrawler extends AuthCrawler{
    
    String authLoginUrl = "https://oebs.goszakup.gov.kz:443/OA_HTML/AppsLogin";
    String baseUrlPort = "https://oebs.goszakup.gov.kz:10443";
    public String fullOA_HTML;
    

    
    public GosAuthCrawler(){
        baseUrl = "https://oebs.goszakup.gov.kz";
        fullOA_HTML = baseUrl + "/OA_HTML/";
        
        setBodySize(20 * (int) Math.pow(10, 6));
    }
    
    /**
     * Authorization to Goszakup Auction. The doc is set to downloaded Purchase Home Page.
     * @param password - password to login
     * @return
     */
    public boolean authGosAuction(String password) {
        
        //авторизация: шаг 1 (app login)
        setUrl(authLoginUrl);
        setMethod(Connection.Method.GET);
        Document appsLoginDoc = getDoc();
        
        if (appsLoginDoc == null) return false;
        
        //авторизация: шаг 2 (ecc auth)
        Map<String, String> appsLoginData = getFormData(appsLoginDoc);
        Elements _els = appsLoginDoc.select("form");
        if (_els.size() == 0) return false;
        String appsLoginUrl = _els.attr("action");
        setData(appsLoginData,true);
        setUrl(appsLoginUrl);
        setMethod(Connection.Method.POST);
        Document loginDoc = getDoc();
        if (loginDoc == null) return false;
        
        //аутентификация по паролю (логин из сертификата) - Переход на главную страницу ЛК
        Elements loginDocEls = loginDoc.select("form");
        if (loginDocEls.size() == 0) return false;
        
        String loginUrl = baseUrlPort + loginDocEls.attr("action");
        Map<String, String> loginData = getFormData(loginDoc);
        loginData.put("password", password);
        Elements loginDocElsOpt = loginDoc.select("form option#supplierUser");
        if (loginDocElsOpt.size() == 0) return false;
        loginData.put("ssousername", loginDocElsOpt.attr("value"));
        if (loginData.get("ssousername").isEmpty()) return false;
        setData(loginData,true);
        setMethod(Connection.Method.POST);
        setUrl(loginUrl);
        Document mainDoc = getDoc();
        if (mainDoc == null) return false;
        
        //выбор закупки поставщика
        Elements mainEls = mainDoc.select("a#AppsNavLink");
        if (mainEls.size() == 0 || mainEls.size() < 2) {
            logger.log(Level.WARNING, "AppsNavLink number: {0}", mainEls.size());
            return false;
        }
        String mainUrl = mainEls.get(1).attr("href");
        if(!mainUrl.contains("navRespId=50724")){
            mainUrl = mainEls.get(0).attr("href");
        }
        setUrl(mainUrl);
        setMethod(Connection.Method.GET);
        Document customerPurchasesDoc = getDoc();
        if (customerPurchasesDoc == null) {
            logger.log(Level.WARNING, "No customer purchases doc found.");
            return false;
        }
        
        //домашняя страница закупок
        if(!GetAuthGosAucPurchasesHomePage(customerPurchasesDoc)){
            return false;
        }
        
        logger.log(Level.INFO, "Auction authorized.");
        return true;
    }

    
    public boolean authGosAuctionStructured(String password) {
        //главная страница ЛК
        if(!GetAuthGosAucMainPage(password)) {
            logger.log(Level.WARNING, "No Main Page entered.");
            return false;
        }

        //выбор закупки поставщика
        if(!GetAuthGosCustomerPurchasesPage(getCurrentDoc())) {
            logger.log(Level.WARNING, "No Customer Purchases Doc found.");
            return false;
        }
        
        //домашняя страница закупок
        return GetAuthGosAucPurchasesHomePage(getCurrentDoc());
    }
    
    
    public boolean GetAuthGosAucMainPage(String password){
        
        //авторизация: шаг 1 (app login)
        setUrl(authLoginUrl);
        setMethod(Connection.Method.GET);
        Document appsLoginDoc = getDoc();
        if (appsLoginDoc == null) return false;
        
        //авторизация: шаг 2 (ecc auth)
        Map<String, String> appsLoginData = getFormData(appsLoginDoc);
        Elements _els = appsLoginDoc.select("form");
        if (_els.size() == 0) return false;
        String appsLoginUrl = _els.attr("action");
        setData(appsLoginData,true);
        setUrl(appsLoginUrl);
        setMethod(Connection.Method.POST);
        Document loginDoc = getDoc();
        if (loginDoc == null) return false;
        
        //аутентификация по паролю (логин из сертификата) - Переход на главную страницу ЛК
        Elements loginDocEls = loginDoc.select("form");
        if (loginDocEls.size() == 0) return false;
        String loginUrl = baseUrl + loginDocEls.attr("action");
        Map<String, String> loginData = getFormData(loginDoc);
        loginData.put("password", password);
        Elements loginDocElsOpt = loginDoc.select("form option#supplierUser");
        if (loginDocElsOpt.size() == 0) return false;
        loginData.put("ssousername", loginDocElsOpt.attr("value"));
        if (loginData.get("ssousername").isEmpty()) return false;
        setData(loginData,true);
        setMethod(Connection.Method.POST);
        setUrl(loginUrl);
        
        return getDoc() != null;
    }
    
    
    public boolean GetAuthGosAucPurchasesHomePage(Document customerPurchasesDoc){
        Elements cusPurEls = customerPurchasesDoc.select("a#N90");
        if (cusPurEls.size() == 0) {
            cusPurEls = customerPurchasesDoc.select("a#N91");
            if(cusPurEls.size() == 0) {
                logger.log(Level.WARNING,"No purchase home page found.");
                return false;
            }
        }
        
        setUrl(cusPurEls.attr("href"));
        
        return getDoc() != null;
    }
    
    
    public boolean GetAuthGosCustomerPurchasesPage(Document mainDoc){
        Elements mainEls = mainDoc.select("a#AppsNavLink");
        if (mainEls.size() == 0 || mainEls.size() < 2) {
            logger.log(Level.WARNING, "AppsNavLink number: {0}", mainEls.size());
            return false;
        }

        String mainUrl = mainEls.get(1).attr("href");
        if(!mainUrl.contains("navRespId=50724")){
            mainUrl = mainEls.get(0).attr("href");
        }
        
        setUrl(mainUrl);
        setMethod(Connection.Method.GET);
        
        return getDoc() != null;
    }
    

   public boolean ExitFromGosAucCabinet(){
        setUrl(baseUrl + "/OA_HTML/OALogout.jsp?menu=Y");
        setMethod(Connection.Method.GET);
        setMethod(Connection.Method.POST);
        setData(getFormData(getDoc()), true);
        setUrl(doc.select("form").attr("action"));
        getDoc();
        logger.log(Level.OFF, doc.title());
        return true;
    }
 
   
   
   
    public boolean authGosOpenBuys(String password) {
        setUrl("https://goszakup.gov.kz/app/index.php/ru/user/login");
        Document _doc = getDoc();
        if (_doc == null) return false;

        Elements els = _doc.select("input#username");
        if (els.size() == 0) return false;
        String login = els.val();
        if (login.isEmpty()) return false;
        setMethod(Connection.Method.POST);
        Map<String, String> data = new HashMap<>();
        data.put("username", login);
        data.put("password", password);
        data.put("loginin", "Вход");
        setData(data,true);
        if(getDoc() == null) return false;
        
        logger.log(Level.FINE, "OpenBuys authorized.");
        return true;
    }

    
}
