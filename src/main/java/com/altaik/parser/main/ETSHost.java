/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.main;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.ZakupTypesEnum;
import com.altaik.parser.ets.ETSCrawler;
import com.altaik.parser.ets.ETSParser;
import com.altaik.parser.ets.ETSSaver;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Vladimir
 */
public class ETSHost {

    protected static final Logger logger = Logger.getLogger(ETSHost.class.getName());

    private final ArrayList<ETSParser> parsers = new ArrayList<>();
    private IDatabaseManager databaseManager;

    private ZakupTypesEnum parserType;

    public ETSHost(IDatabaseManager databaseManager, ZakupTypesEnum type) {
        this.databaseManager = databaseManager;
        this.parserType = type;
    }

    public void Run() {
        // Загрузка страницы "Члены биржи"
        ETSCrawler memberCrawler = new ETSCrawler();
        memberCrawler.setUrlPath("ru/members.aspx");
        Document membersDoc = memberCrawler.getDoc();
        // Загрузка страницы "Объявленые аукционы"
        ETSCrawler crawler = new ETSCrawler();
        crawler.setUrlPath("ru/auctions.aspx");
        Map<String, String> data = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        currentDate.add(Calendar.DATE, -7);
        data.put("ctl00$PageContent$txtDateFrom", dateFormat.format(currentDate.getTime()));
        data.put("ctl00$PageContent$hdnDateFrom", dateFormat.format(currentDate.getTime()));
//        currentDate.add(Calendar.DATE, 30);
//        data.put("ctl00$PageContent$txtDateTo", dateFormat.format(currentDate.getTime()));
//        data.put("ctl00$PageContent$hdnDateTo", dateFormat.format(currentDate.getTime()));
        data.put("ctl00$PageContent$txtDateTo", "");
        data.put("ctl00$PageContent$hdnDateTo", "");
        data.put("ctl00$PageContent$rblSortDate", "1");
        data.put("ctl00$PageContent$ddlTypes", "");
        data.put("ctl00$PageContent$ddlSections", "");
        data.put("ctl00$PageContent$ddlBrokers", "");
        data.put("ctl00$PageContent$txtGoods", "");
        data.put("ctl00$PageContent$btnSearch", "Искать");
        crawler.setData(data, true);
        crawler.setMethod(Connection.Method.POST);
        Document auctionDoc = crawler.getDoc();

        ETSParser parser = new ETSParser(auctionDoc, databaseManager);
        parser.parseMembers(membersDoc);
        parser.PurchasesDo();

        if (null != parser.getData() && !parser.getData().isEmpty()) {
            ETSSaver saver = new ETSSaver(databaseManager);
            saver.Do(parser.getData());
            saver.Close();
        }
        parser.Close();

    }
}
