package com.altaik.parser.processes;

import com.altaik.bp.BaseProcesses;

import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 27.03.2018
 */
public abstract class ParserProcess extends BaseProcesses {
    public static final String SPEC_PARAMETER_COUNT = "process.pages";

    private final Properties properties;
    private int count = 0;

    public ParserProcess(Properties properties) {
        this.properties = properties;
        initialize();
    }

    private void initialize() {
        try {
            String s = properties.getProperty(SPEC_PARAMETER_COUNT);

            if (s != null && !s.isEmpty())
                count = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parse count parameter");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
