package me.morpheus.lighteconomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LELog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightEconomy.ID);

    public static Logger getLogger() {
        return LOGGER;
    }

    private LELog() {}
}
