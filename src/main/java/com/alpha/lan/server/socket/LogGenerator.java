package com.alpha.lan.server.socket;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alpha.lan.utils.TextUtils;


public class LogGenerator {
    private static final String LOG_DIR = "./log/";
    private static final String DEFAULT_LOG_FILE = "stb.log";
    private static final String SPLIT = "*****************************************************************************\n";

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File logFile;

    static {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public static void saveLogFile(String log) {
        saveLogFile(null, log);
    }

    public static void saveLogFile(String fileName, String log) {
        String logFilePath = null;

        if (TextUtils.isEmpty(fileName)) {
            logFilePath = LOG_DIR + DEFAULT_LOG_FILE;
        } else {
            logFilePath = LOG_DIR + fileName;
            logFile = new File(logFilePath);
        }

        if (logFile == null) {
            logFile = new File(logFilePath);
        }

        BufferedWriter writer = null;
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            log = formatLogContent(log);

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
            writer.write(log);
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String formatLogContent(String logContent) {
        String logTag = SPLIT + dateFormatter.format(new Date()) + "\n";
        logContent = logTag + logContent + "\n";
        return logContent;
    }
}
