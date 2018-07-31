package com.altaik.parser.auctionya.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;


public class HibernateUtil {
    private static Logger logger = Logger.getLogger(HibernateUtil.class.getName());
    private static boolean isFail = false;
    private static SessionFactory sessionFactory = null;


    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }

    public static SessionFactory getSessionFactory() {
        if (!isFail && sessionFactory == null) {
            try {
                sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Initial SessionFactory creation failed. {0} \n {1}",
                        new Object[]{ex.getMessage(), ex.getStackTrace()});
                isFail = true;
//                throw new ExceptionInInitializerError(ex);
            }
        }

        return sessionFactory;
    }

}
