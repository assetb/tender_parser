/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

/**
 *
 * @author Aset
 */
public enum ZakupTypesEnum {
    GosBuys("buys", 1), 
    GosOpenBuys("openbuys", 1), 
    GosAucs("aucs", 1), 
    Samruk("samruk", 2), 
    Nadloc("nadloc", 3), 
    PostProcess("post", null), 
    Nightly("nightly", null), 
    NEWGOS("newgos", 1), 
    ETS("ets", 4), 
    SIRIUS("sirius", 5), 
    KSGOV("ksgov", 6),
    GOSREESTR("gosreestr", 7),
    AUCYA("auctionya", 8),
    CASPY("caspy", 10),
    KAZETS("kazets", 11),
    EAUC("eauc",9);

    private final String text;
    private final Integer source;

    /**
     * @param text
     */
    private ZakupTypesEnum(final String text, Integer source) {
        this.text = text;
        this.source = source;
    }

    public Integer getSource() {
        return source;
    }

    @Override
    public String toString() {
        return text;
    }
}
