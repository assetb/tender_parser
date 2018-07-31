package com.altaik.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 13.04.2018
 */
public class ArchiveSaver {
    private String purchaseNumber;
    private int source;
    private ArchiveManager parent;
    private boolean isZip = false;
    private boolean isSave = false;
    private List<String> documents = new ArrayList<>();

    ArchiveSaver(int source, String purchaseNumber, ArchiveManager parent) {
        this.purchaseNumber = purchaseNumber;
        this.source = source;
        this.parent = parent;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public int getSource() {
        return source;
    }

    public boolean isSave() {
        return isSave;
    }

    public ArchiveSaver addDocument(String path) {
        documents.add(path);
        return this;
    }

    public ArchiveSaver zip() {
        isZip = true;
        return this;
    }

    public boolean save() {
        isSave = parent.upload((String[]) documents.toArray());
        return isSave;
    }
}
