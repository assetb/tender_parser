/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.bo.Method;
import com.altaik.bo.ProcessedPurchase;
import com.altaik.bo.Purchase;
import com.altaik.bo.Region;
import com.altaik.db.IDatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aset
 */
public class Postprocessing {
    private static final int DEFAULT_STATUS = 1;
    private final IDatabaseManager dbManager;
    private Logger logger;
    private List<Method> methods;
    private List<Region> regions;

    public Postprocessing(IDatabaseManager dbManager) {
        this.logger = Logger.getLogger(getClass().getName());
        this.dbManager = dbManager;
        updateMethods();
        updateRegions();
    }

    private void updateRegions() {
        regions = dbManager.getRegions();
    }

    private void updateMethods() {
        methods = dbManager.getMethods();
    }

    public void Do() {
        int lastprocessed = dbManager.getLastProcessedPurchaseId("post");
        int maxid = dbManager.getMaxPurchaseId();

        logger.log(Level.INFO, "Postprocessing: lastprocessed = {0}", lastprocessed);
        logger.log(Level.INFO, "PostProcessing: max id = {0}", maxid);

        int currentid = lastprocessed;
        for (int i = lastprocessed + 1; i <= maxid; i++) {
            Purchase purchase = dbManager.getPurchase(i);

            if (purchase == null) {
                logger.log(Level.WARNING, "Purchase by id={0} not found", i);
                continue;
            }

            ProcessedPurchase proccessePurchase = dbManager.getProccessePurchase(purchase.getSource(), purchase.getNumber());

            if (proccessePurchase != null) {
                String status = purchase.getStatus();

                if (status != null && status.matches("(?i)(завершен|отменен|итоги|вскрыто|окончен)")) {
                    dbManager.updateProcessedPurchaseStatus(proccessePurchase.getId(), 2, status);
                    logger.log(Level.INFO, "Updated status | PurchaseId={0}", proccessePurchase.getId());
                }

                logger.log(Level.INFO, "Skip | PurchaseId{0}", proccessePurchase.getId());
            } else {
                proccessePurchase = createProcessedPurchase(purchase);
                dbManager.addProcessedPurchase(proccessePurchase);
                logger.log(Level.INFO, "Added processed purchase | PurchaseId={0}", proccessePurchase.getId());
            }
            logger.log(Level.INFO, "CurrentId={0} | LastId={1} | Difference={2}", new Object[]{i, maxid, maxid - i});
            currentid = i;
        }

        dbManager.updateLastProcessedPurchaseId(currentid);
        dbManager.removeFinishedPurchases();
    }

    /**
     * Создание обработанного объявление
     *
     * @param purchase Объявление
     * @return Обработанное объявление
     */
    private ProcessedPurchase createProcessedPurchase(Purchase purchase) {
        ProcessedPurchase processedPurchase = new ProcessedPurchase();

        setId(processedPurchase, purchase);
        setNumber(processedPurchase, purchase);
        setSource(processedPurchase, purchase);
        setRuName(processedPurchase, purchase);
        setKzName(processedPurchase, purchase);
        setStatus(processedPurchase, purchase);
        setType(processedPurchase, purchase);
        setCustomer(processedPurchase, purchase);
        setOrganizer(processedPurchase, purchase);
        setRegion(processedPurchase, purchase);
        setMethod(processedPurchase, purchase);
        setAdditionalInformation(processedPurchase, purchase);
        setPublishDay(processedPurchase, purchase);
        setStartDay(processedPurchase, purchase);
        setEndDay(processedPurchase, purchase);
        setLoadDate(processedPurchase, purchase);
        setAttribute(processedPurchase, purchase);
        setPriceSuggestion(processedPurchase, purchase);
        setLink(processedPurchase, purchase);
        setLotsSum(processedPurchase, purchase);

        return processedPurchase;
    }

    private void setId(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setId(purchase.getId());
    }

    private void setLotsSum(ProcessedPurchase processedPurchase, Purchase purchase) {
        float sumLots = dbManager.getSumLots(purchase.getNumber(), purchase.getSource());
        processedPurchase.setiSum(sumLots);
    }

    private void setLink(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setLink(purchase.getLink());
    }

    private void setNumber(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setNumber(purchase.getNumber());
    }

    private void setSource(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setSourceId(purchase.getSource());
    }

    private void setRuName(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setRuName(purchase.getRuName());
    }

    private void setKzName(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setKzName(purchase.getKzName());
    }

    private void setStatus(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setStatusInt(DEFAULT_STATUS);
        processedPurchase.setStatus(purchase.getStatus());
    }

    private void setType(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setType(purchase.getType());
    }

    private void setCustomer(ProcessedPurchase processedPurchase, Purchase purchase) {
        String customerName = purchase.getCustomer();
        int sourceId = purchase.getSource();
        int customerId = 0;

        if (customerName != null && !customerName.isEmpty()) {
            customerId = dbManager.findCustomerId(sourceId, customerName);

            if (customerId == 0) {
                customerId = dbManager.addCustomer(sourceId, customerName);
            }
        }

        processedPurchase.setCustomer(customerName);
        processedPurchase.setCustomerId(customerId);
    }

    private void setOrganizer(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setOrganizer(purchase.getOrganizer());
    }

    private void setRegion(ProcessedPurchase processedPurchase, Purchase purchase) {
        String venue = purchase.getVenue();
        int regionId = 0;

        if (venue != null && !venue.isEmpty()) {

            for (Region region : regions) {
                String s = String.format("(%s|%s)", region.getName(), region.getKeyword());
                Pattern pattern = Pattern.compile(s);
                Matcher matcher = pattern.matcher(venue);

                if (matcher.find()) {
                    regionId = region.getId();
                    break;
                }
            }
        }

        processedPurchase.setRegionId(regionId);
    }

    private void setMethod(ProcessedPurchase processedPurchase, Purchase purchase) {
        int sourceId = purchase.getSource();
        String methodName = purchase.getMethod();
        Method method = methods.stream()
                .filter(m -> m.getSiteId() == sourceId && m.getName().equalsIgnoreCase(methodName))
                .findFirst()
                .orElse(null);

        if (method == null)
            method = createMethod(sourceId, methodName);

        processedPurchase.setMethodId(method.getId());
    }

    private void setAdditionalInformation(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setAdditionalInformation(purchase.getAdditionalinformation());
    }

    private void setPublishDay(ProcessedPurchase processedPurchase, Purchase purchase) {
        String date = purchase.getPublishDay();

        if (date != null && !date.isEmpty()) {
            String dateView = formatStringToDateViewFormat(date);

            if (dateView != null && !dateView.isEmpty()) {
                processedPurchase.setPublishDay(dateView);
                processedPurchase.setdPublishDay(parseStringToDate(dateView));
            }

        }
    }

    private void setStartDay(ProcessedPurchase processedPurchase, Purchase purchase) {
        String date = purchase.getStartDay();

        if (date != null && !date.isEmpty()) {
            String dateView = formatStringToDateViewFormat(date);

            if (dateView != null && !dateView.isEmpty()) {
                processedPurchase.setStartDay(dateView);
                processedPurchase.setdStartDay(parseStringToDate(dateView));
            }

        }
    }


    private void setEndDay(ProcessedPurchase processedPurchase, Purchase purchase) {
        String date = purchase.getEndDay();

        if (date != null && !date.isEmpty()) {
            String dateView = formatStringToDateViewFormat(date);

            if (dateView != null && !dateView.isEmpty()) {
                processedPurchase.setEndDay(dateView);
                processedPurchase.setdEndDay(parseStringToDate(dateView));
            }

        }
    }

    private void setLoadDate(ProcessedPurchase processedPurchase, Purchase purchase) {
        Date date = new Date();
        processedPurchase.setLoadDay(new java.sql.Date(date.getTime()));
    }

    private void setAttribute(ProcessedPurchase processedPurchase, Purchase purchase) {
        processedPurchase.setAttribute(purchase.getAttribute());
    }

    private void setPriceSuggestion(ProcessedPurchase priceSuggestion, Purchase purchase) {
        priceSuggestion.setPriceSuggestion(purchase.getPriceSuggestion());
    }

    private String formatStringToDateViewFormat(String st) {
        String replacement = "${day}.${month}.${year}";
        Pattern pattern1 = Pattern.compile("(?<day>\\d{2})(?<separator>[-.])(?<month>\\d{2})\\k<separator>(?<year>\\d{4})");
        Matcher matcher1 = pattern1.matcher(st);

        if (matcher1.find()) {
            return matcher1.replaceAll(replacement);
        }

        Pattern pattern2 = Pattern.compile("(?<year>\\d{4})(?<separator>[-.])(?<month>\\d{2})\\k<separator>(?<day>\\d{2})");
        Matcher matcher2 = pattern2.matcher(st);

        if (matcher2.find()) {
            return matcher2.replaceAll(replacement);
        }

        return null;
    }

    private java.sql.Date parseStringToDate(String dateView) {
        java.sql.Date date = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            date = new java.sql.Date(dateFormat.parse(dateView).getTime());
        } catch (ParseException e) {
            logger.log(Level.WARNING, "Parse error publish day. Error: {0}", e);
        }

        return date;
    }

    public void DocsProcessing() {
        try {
            ResultSet maxidwithdocsSet = dbManager.Execute("select max(id) as maxid from procpurchase where isdocs=1");
            while (maxidwithdocsSet.next()) {
                String maxid = maxidwithdocsSet.getString("maxid");
                if (null != maxid && !maxid.isEmpty() && !maxid.equals("0")) {
                    ResultSet purchaseSet = dbManager.Execute("select id, docszip from purchase where id > " + maxid + " and isdocs = 1");
                    while (purchaseSet.next()) {
                        try {
                            String docszip = purchaseSet.getString("docszip");
//                            String path = purchaseSet.getString("isdoc");
                            String id = purchaseSet.getString("id");
                            if (null != docszip && !docszip.isEmpty() && null != id && !id.isEmpty()) {
                                dbManager.Update("update procpurchase set isdocs=1,docszip='" + docszip + "' where id='" + id + "'");
                            }
                        } catch (SQLException ex) {
                            logger.log(Level.SEVERE, ex.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }

    }

    private Method createMethod(int sourceId, String name) {
        Method method = new Method();
        method.setName(name);
        method.setSiteId(sourceId);
        dbManager.addMethod(method);
        updateMethods();

        return method;
    }


    public void Close() {

    }

}
