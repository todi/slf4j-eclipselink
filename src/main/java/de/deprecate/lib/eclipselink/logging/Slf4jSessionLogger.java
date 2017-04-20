/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.deprecate.lib.eclipselink.logging;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * In order to use SLF4J with Eclipselink, set <code>eclipselink.logging.logger</code>
 * to <code>de.deprecate.lib.eclipselink.logging.Slf4jSessionLogger</code>.
 * </p>
 * <p>
 * Eclipselink will then ignore the value of <code>eclipselink.logging.level</code>.
 * Instead, the logging level is controlled by your SLF4J implementation (logback, log4j, ...)
 * </p>
 * <p>
 * All other Eclipselink logging settings are still honored
 * (<code>eclipselink.logging.timestamp</code>, <code>eclipselink.logging.thread</code>,
 * <code>eclipselink.logging.session</code>, <code>eclipselink.logging.connection</code>
 * and <code>eclipselink.logging.parameters</code>)
 * </p>
 * <p>
 * The logging levels from Eclipselink are mapped to those from SLF4J as follows:
 * <ul>
 * <li><code>ALL,FINER,FINEST -> TRACE</code>
 * <li><code>FINE -> DEBUG</code>
 * <li><code>CONFIG,INFO -> INFO</code>
 * <li><code>WARNING -> WARN</code>
 * <li><code>SEVERE -> ERROR</code>
 * </ul>
 * </p>
 *
 * @author Miguel Angel Sosvilla Luis (https://github.com/slavb18)
 * @author Thomas Haider (https://github.com/todi)
 */
public class Slf4jSessionLogger extends AbstractSessionLog {

    public static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging";
    public static final String DEFAULT_CATEGORY = "default";

    public static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY;

    private Map<Integer, LogLevel> mapLevels;
    private Map<String, Logger> categoryLoggers = new HashMap<String, Logger>();

    public Slf4jSessionLogger() {
        super();
        createCategoryLoggers();
        initMapLevels();
    }

    @Override
    public void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }

        Logger logger = getLogger(entry.getNameSpace());
        LogLevel logLevel = getLogLevel(entry.getLevel());

        StringBuilder message = new StringBuilder();

        message.append(getSupplementDetailString(entry));
        message.append(formatMessage(entry));

        switch (logLevel) {
            case TRACE:
                logger.trace(message.toString());
                break;
            case DEBUG:
                logger.debug(message.toString());
                break;
            case INFO:
                logger.info(message.toString());
                break;
            case WARN:
                logger.warn(message.toString());
                break;
            case ERROR:
                logger.error(message.toString());
                break;
        }
    }

    @Override
    public boolean shouldLog(int level, String category) {
        Logger logger = getLogger(category);
        boolean resp = false;

        LogLevel logLevel = getLogLevel(level);

        switch (logLevel) {
            case TRACE:
                resp = logger.isTraceEnabled();
                break;
            case DEBUG:
                resp = logger.isDebugEnabled();
                break;
            case INFO:
                resp = logger.isInfoEnabled();
                break;
            case WARN:
                resp = logger.isWarnEnabled();
                break;
            case ERROR:
                resp = logger.isErrorEnabled();
                break;
        }

        return resp;
    }

    @Override
    public boolean shouldLog(int level) {
        return shouldLog(level, "default");
    }

    /**
     * Initialize loggers eagerly
     */
    private void createCategoryLoggers() {
        for (String category : SessionLog.loggerCatagories) {
            addLogger(category, ECLIPSELINK_NAMESPACE + "." + category);
        }

        // Logger default
        addLogger(DEFAULT_CATEGORY, DEFAULT_ECLIPSELINK_NAMESPACE);
    }

    /**
     * INTERNAL: Add Logger to the categoryLoggers.
     */
    private void addLogger(String loggerCategory, String loggerNameSpace) {
        categoryLoggers.put(loggerCategory, LoggerFactory.getLogger(loggerNameSpace));
    }

    /**
     * INTERNAL: Return the Logger for the given category
     */
    private Logger getLogger(String category) {

        if (category == null || category.isEmpty() || category.trim().isEmpty() || !categoryLoggers.containsKey(category)) {
            category = DEFAULT_CATEGORY;
        }

        return categoryLoggers.get(category);

    }

    /**
     * Return the corresponding Slf4j Level for a given EclipseLink level.
     */
    private LogLevel getLogLevel(Integer level) {
        LogLevel logLevel = mapLevels.get(level);

        if (logLevel == null)
            logLevel = LogLevel.OFF;

        return logLevel;
    }

    /**
     * SLF4J log levels.
     */
    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }

    /**
     * Mapping Eclipselink to SLF4J logging levels.
     */
    private void initMapLevels() {
        mapLevels = new HashMap<Integer, LogLevel>();

        mapLevels.put(SessionLog.ALL, LogLevel.TRACE);
        mapLevels.put(SessionLog.FINEST, LogLevel.TRACE);
        mapLevels.put(SessionLog.FINER, LogLevel.TRACE);
        mapLevels.put(SessionLog.FINE, LogLevel.DEBUG);
        mapLevels.put(SessionLog.CONFIG, LogLevel.INFO);
        mapLevels.put(SessionLog.INFO, LogLevel.INFO);
        mapLevels.put(SessionLog.WARNING, LogLevel.WARN);
        mapLevels.put(SessionLog.SEVERE, LogLevel.ERROR);
    }

}
