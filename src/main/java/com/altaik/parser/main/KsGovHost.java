/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.main;

import com.altaik.bo.Company;
import com.altaik.bo.Product;
import com.altaik.bo.ProductsOfCompany;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.ISiteParser;
import com.altaik.parser.ksgov.KsGovParser;
import com.altaik.saver.*;

import java.util.List;
import java.util.Properties;

/**
 *
 * @author Vladimir
 */
public class KsGovHost {
    
    private final String COUNT_PAGES_PROPS = "ksgov.countpages";
    private final IDatabaseManager databaseManager;
    private final Properties props;
    
    public KsGovHost(IDatabaseManager databaseManager, Properties props) {
        this.props = props;
        this.databaseManager = databaseManager;
    }
    
    public void Run() {
        int countPages = Integer.parseInt(props.getProperty(COUNT_PAGES_PROPS, "1"));
        ISiteParser parser = new KsGovParser("http://ks.gov.kz", countPages);
        for (int i = 0; i < countPages; i++) {
//            List<Company> companies = parser.Do();
            List<Company> companies = parser.Proccess(i);
            
            ISaver companySaver = new CompanySaver(databaseManager);
            ISaver productSaver = new ProductSaver(databaseManager);
            ISaver productsOfCompanySaver = new ProductsOfCompanySaver(databaseManager);
            
            for (Company company : companies) {
                Query companyQuery = companySaver.Do(company);
                for (Product product : company.products) {
                    ProductsOfCompany productsOfCompany = new ProductsOfCompany();
                    Query productQuery = productSaver.Do(product);
                    productsOfCompany.companyId = companyQuery.pk;
                    productsOfCompany.productId = productQuery.pk;
                    productsOfCompanySaver.Do(productsOfCompany);
                }
            }
        }
    }
}
