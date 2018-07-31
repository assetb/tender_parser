package com.altaik.parser.processes;

import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.IParser;
import com.altaik.parser.Saver;
import com.altaik.parser.gosreestr.GosReestrCrawler;
import com.altaik.parser.gosreestr.GosReestrParser;
import com.altaik.parser.gosreestr.GosReestrSaver;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class GosReestrProcess extends ParserProcess {
    private IDatabaseManager databaseManager;
    private GosReestrCrawler crawler = new GosReestrCrawler();
    private GosReestrCrawler auctionListcrawler = new GosReestrCrawler();
    private List<IParser> parsers = new ArrayList<>();

    public GosReestrProcess(Properties properties) {
        super(properties);
        databaseManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
    }

    @Override
    protected void onClose() {
        databaseManager.close();
        crawler.close();
        auctionListcrawler.close();
        parsers.forEach(IParser::Close);
    }

    @Override
    protected void onStart() {
        int countPages = getCount();
        crawler.setUrlPath("/p/ru/auction-guest-list");
        Document auctionListDoc = crawler.getDoc();

        for (int i = 0; i < countPages; i++) {

            if (auctionListDoc == null) {
                break;
            }

            Map formData = crawler.getFormData(auctionListDoc, "#GuestAllAuctionList");
            formData.put("pager-page-index_auction-guest-list", String.format("%s", i));
            auctionListcrawler.setUrlPath("/p/ru/auction-guest-list");
            auctionListcrawler.setData(formData, true);
            auctionListDoc = auctionListcrawler.getDoc();
            IParser parser = new GosReestrParser(auctionListDoc, databaseManager);
            parser.PurchasesDo();
            parsers.add(parser);
        }

        parsers.stream().filter((parser) -> !(parser == null)).forEach((parser) -> {
            Saver saver = new GosReestrSaver(databaseManager);
            saver.Do(parser.getData());
            saver.Close();
        });
    }
}
