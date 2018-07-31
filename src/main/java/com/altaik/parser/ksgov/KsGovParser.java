/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.ksgov;

import com.altaik.bo.Company;
import com.altaik.bo.Product;
import com.altaik.parser.ISiteParser;
import com.altaik.parser.SiteParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vladimir
 */
public class KsGovParser extends SiteParser<Company> implements ISiteParser<Company> {

    private static final Logger LOG = Logger.getLogger(KsGovParser.class.getName());
    private final int SOURCE = 2;
    private final String pathToMainPage = "/Suppliers/SuppliersList.aspx?lang=ru";
    protected KsGosCrawler crawler;

    public KsGovParser(String url, int countPages) {
        super(url, countPages);
        crawler = new KsGosCrawler(url);
    }

    @Override
    protected Document GetMainPage(String url, int ipage) {
        crawler.setUrlPath(pathToMainPage + "&page_num=" + (ipage + 1));
        crawler.getDoc();
        return crawler.getDoc();
    }

    @Override
    protected Elements ParseMainPage(Document page) {
        return page.select("#ctl00_main_center_inner_suppliersListTable tr:not(:first-child)");
    }

    @Override
    protected Company InitBusinessObject(Element row, Document page) {
        Company company = new Company();
        Elements columns = row.select("td");
        Element tdName = columns.get(1);
        company.link = tdName.select("a").attr("href");
        company.runame = tdName.text();
        company.region = columns.get(2).text();
        company.source = SOURCE;
        return company;
    }

    @Override
    protected boolean ParseDetails(Company bo, Element row, Document page) {
        String idSupprier = bo.link.replaceAll(".*supplierId=", "");
        crawler.setHeader("Referer", url + bo.link + "&lang=ru");
        crawler.setUrlPath("/forms/SupplierRegistrationForm.aspx?action=&lang=ru&supplierId=" + idSupprier);

        Document pageDetails = crawler.getDoc();
        if (pageDetails == null) {
            LOG.log(Level.WARNING, "Page details is null. Url {0}", bo.link);
            return false;
        }

        Elements inputEls = pageDetails.select("input");
        bo.bin = inputEls.select("#bin").val();
        bo.kzname = inputEls.select("#nameKz").val();
        bo.runame = inputEls.select("#nameRu").val();
        bo.email = inputEls.select("#email").val();
        bo.tel = inputEls.select("#phone").val();
        bo.address = inputEls.select("#katoTxt").val() + ", " + inputEls.select("input#addressRu").val();
        bo.source = SOURCE;
        crawler.setUrlPath("/Suppliers/SuppliersTRU.aspx?supplierId=" + idSupprier);
        Document pageProducts = crawler.getDoc();

        Elements productRows = pageProducts.select("#ctl00_main_center_inner_suppliersTruTable tr:not(:first-child)");
        bo.products = new ArrayList<>();
        for (Element productRow : productRows) {
            String click = productRow.select("td > a").attr("onclick");
            crawler.setUrlPath("/forms/AddNewTRU.aspx?action=view&itemId=" + click.replaceAll("\\D", ""));
            Document formTru = crawler.getDoc();

            Product product = new Product();
            product.code = formTru.select("input#kpved").val();
            product.name = formTru.select("input#nameRu").val();
            product.source = SOURCE;
            bo.products.add(product);
        }
        return true;
    }

}
