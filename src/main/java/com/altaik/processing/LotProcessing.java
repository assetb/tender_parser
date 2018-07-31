package com.altaik.processing;

import com.altaik.bo.Lot;
import com.altaik.bp.BaseProcesses;
import com.altaik.db.IDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Kovalev (v.kovalev@com.altaik.db.altatender.kz) on 26.03.2018
 */
public class LotProcessing extends BaseProcesses {
    private IDatabaseManager dbManager;
    private List<Lot> lots = new ArrayList<>();

    public LotProcessing(IDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }


    @Override
    protected void onClose() {

    }

    @Override
    protected void onStart() {

    }

    private void loadNotProcessingsLots() {
//        dbManager
    }

}
