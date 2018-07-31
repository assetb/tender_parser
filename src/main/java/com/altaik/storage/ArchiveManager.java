package com.altaik.storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 13.04.2018
 */
public abstract class ArchiveManager implements Closeable {
    private Properties properties;
    private List<ArchiveSaver> savers = new ArrayList<>();

    public ArchiveManager(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    abstract boolean upload(String[] documents);

    public ArchiveSaver getSaver(int source, String purchaseNumber) {
        ArchiveSaver saver = new ArchiveSaver(source, purchaseNumber, this);
        savers.add(saver);

        return saver;
    }

    @Override
    public void close() throws IOException {
        savers.stream().forEach(s -> {
            if (!s.isSave()) {
                s.save();
            }
        });
        savers.clear();
    }
}
