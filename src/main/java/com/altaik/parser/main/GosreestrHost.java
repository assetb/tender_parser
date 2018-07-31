/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.main;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.ZakupTypesEnum;
import com.altaik.parser.gosreestr.GosReestrCrawler;
import com.altaik.parser.gosreestr.GosReestrParser;
import com.altaik.parser.gosreestr.GosReestrSaver;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class GosreestrHost {

    protected static final Logger logger = Logger.getLogger(GosreestrHost.class.getName());

    protected boolean isNigthly = false;
    protected int pageMultiplier = 4;
    private final IDatabaseManager databaseManager;
    private int countPages = 100;
    private final ZakupTypesEnum typeParser;

    public GosreestrHost(IDatabaseManager databaseManager, int countPages, boolean isNightly, ZakupTypesEnum typeParser) {
        this.databaseManager = databaseManager;
        this.isNigthly = isNightly;
        this.countPages = isNightly ? countPages * pageMultiplier : countPages;
        this.typeParser = typeParser;
    }

    public void Run() {
        GosReestrCrawler crawler = new GosReestrCrawler();
        crawler.setUrlPath("/p/ru/auction-guest-list");
        Document auctionListDoc = crawler.getDoc();
        List<GosReestrParser> parsers = new ArrayList<>();

        for (int i = 0; i < countPages; i++) {

            if (auctionListDoc == null) {
                break;
            }

            Map formData = crawler.getFormData(auctionListDoc, "#GuestAllAuctionList");
            formData.put("pager-page-index_auction-guest-list", String.format("%s", i));
            GosReestrCrawler auctionListcrawler = new GosReestrCrawler();
            auctionListcrawler.setUrlPath("/p/ru/auction-guest-list");
            auctionListcrawler.setData(formData, true);
            auctionListDoc = auctionListcrawler.getDoc();
            GosReestrParser parser = new GosReestrParser(auctionListDoc, databaseManager);
            parser.PurchasesDo();
            parsers.add(parser);
        }

        parsers.stream().filter((parser) -> !(parser == null)).forEach((parser) -> {
            GosReestrSaver saver = new GosReestrSaver(databaseManager);
            saver.Do(parser.getData());
            saver.Close();
            parser.Close();
        });
    }
}
