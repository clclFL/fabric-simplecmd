package net.pineclone.simplecmd.utils;

import net.pineclone.simplecmd.SimpleCmdMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;

public class Utils {

    public static LoggerContext context = LogManager.getContext(false);
    public static Logger LOGGER = context.getLogger(SimpleCmdMod.MOD_ID);

}
