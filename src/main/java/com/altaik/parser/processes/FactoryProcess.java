package com.altaik.parser.processes;

import com.altaik.parser.processes.gos.GosProcess;

import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class FactoryProcess {
    private static final String ETS = "ets";
    private static final String SAMRUK = "samruk";
    private static final String EAUC = "eauc";
    private static final String POST = "post";
    private static final String REESTR = "reestr";
    private static final String CASPY = "caspy";
    private static final String GOS = "gos";
    private static final String KS_GOV = "ksgov";

    public static ParserProcess newInstance(String type, Properties properties) {
        ParserProcess process = null;

        switch (type) {
            case ETS:
                process = new EtsProcess(properties);
                break;
            case SAMRUK:
                process = new SamrukProcess(properties);
                break;
            case EAUC:
                process = new EaucProcess(properties);
                break;
            case POST:
                process = new PostProcess(properties);
                break;
            case REESTR:
                process = new GosReestrProcess(properties);
                break;
            case CASPY:
                process = new CaspyProcess(properties);
                break;
            case GOS:
                process = new GosProcess(properties);
                break;
            case KS_GOV:
                process = new KsGovProcess(properties);
                break;
        }

        return process;
    }
}
