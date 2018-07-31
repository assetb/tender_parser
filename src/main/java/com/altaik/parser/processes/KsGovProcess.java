package com.altaik.parser.processes;

import com.altaik.bo.Company;
import com.altaik.bo.Product;
import com.altaik.bo.ProductsOfCompany;
import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.ISiteParser;
import com.altaik.parser.ksgov.KsGovParser;
import com.altaik.saver.*;

import java.util.List;
import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 30.03.2018
 */
public class KsGovProcess extends ParserProcess {
    private IDatabaseManager dbManager;

    public KsGovProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
    }

    @Override
    protected void onClose() {
        dbManager.close();
    }

    @Override
    protected void onStart() {
        int countPages = getCount();
        ISiteParser parser = new KsGovParser("http://ks.gov.kz", countPages);
        for (int i = 0; i < countPages; i++) {
//            List<Company> companies = parser.Do();
            List<Company> companies = parser.Proccess(i);
            ISaver companySaver = new CompanySaver(dbManager);
            ISaver productSaver = new ProductSaver(dbManager);
            ISaver productsOfCompanySaver = new ProductsOfCompanySaver(dbManager);

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
