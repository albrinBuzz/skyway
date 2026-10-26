package com.SkyWay.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "bitBridge.log";
    private static final long MAX_FILE_SIZE = 500L * 1024L * 1024L; // 500 MB

    // Detección automática o configurable de ANSI
    //private static boolean enableColors = checkAnsiSupport();

    private static boolean enableColors = false;

    // --- COLORES ANSI INTENSOS Y NEGRITAS ---
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static final String TIME_COLOR = "\u001B[90m";       // Gris oscuro
    private static final String CLASS_COLOR = "\u001B[38;5;39m"; // Azul brillante
    private static final String METHOD_COLOR = "\u001B[38;5;76m";// Verde lima
    private static final String LINE_COLOR = "\u001B[38;5;214m"; // Naranja suave
    private static final String MSG_COLOR = "\u001B[97m";        // Blanco intenso

    public enum LogLevel {
        DEBUG("\u001B[1;34m", "[DEBUG]"),
        INFO("\u001B[1;32m",  "[INFO ]"),
        WARN("\u001B[1;33m",  "[WARN ]"),
        ERROR("\u001B[1;31m", "[ERROR]");

        final String color;
        final String label;

        LogLevel(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Evalúa si la terminal o entorno soporta ANSI.
     */
    /**
     * Evalúa si la terminal o entorno soporta ANSI de forma dinámica y estricta.
     */
    private static boolean checkAnsiSupport() {
        // 1. Revisar Variable de Entorno directa (LOGGING_ANSI_ENABLED)
        String ansiEnv = System.getenv("LOGGING_ANSI_ENABLED");
        if (ansiEnv != null) {
            return Boolean.parseBoolean(ansiEnv.trim());
        }

        // 2. Revisar Propiedad de Sistema Java (-Dlogging.ansi.enabled=false o -Dspring.output.ansi.enabled=NEVER)
        String customProp = System.getProperty("logging.ansi.enabled");
        if (customProp != null) {
            return Boolean.parseBoolean(customProp.trim());
        }

        String springAnsi = System.getProperty("spring.output.ansi.enabled");
        if (springAnsi != null) {
            if ("ALWAYS".equalsIgnoreCase(springAnsi) || "DETECT".equalsIgnoreCase(springAnsi)) {
                return true;
            }
            if ("NEVER".equalsIgnoreCase(springAnsi) || "false".equalsIgnoreCase(springAnsi)) {
                return false;
            }
        }

        // 3. Si no hay variable ni propiedad definida explícitamente:
        // Desactivar por defecto en entorno sin consola asignada
        return false;
    }

    public static void setEnableColors(boolean enable) {
        enableColors = enable;
    }

    public static synchronized void log(LogLevel level, String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String time = dtf.format(LocalDateTime.now());
        String fullClassName = ste.getClassName();

        System.out.println(formatForConsole(level, time, fullClassName, ste, message));
    }

    private static String formatForConsole(LogLevel lvl, String time, String fullCls, StackTraceElement ste, String msg) {
        if (!enableColors) {
            return formatForFile(lvl, time, fullCls, ste, msg);
        }

        String packageName = "";
        String className = fullCls;
        if (fullCls.contains(".")) {
            packageName = fullCls.substring(0, fullCls.lastIndexOf('.') + 1);
            className = fullCls.substring(fullCls.lastIndexOf('.') + 1);
        }

        return String.format("%s%s%s %s%s%s %s%s%s%s%s%s::%s%s%s(%s%d%s) %s %s%s%s",
                TIME_COLOR, time, RESET,
                lvl.color, lvl.label, RESET,
                TIME_COLOR, packageName, RESET,
                BOLD + CLASS_COLOR, className, RESET,
                METHOD_COLOR, ste.getMethodName(), RESET,
                LINE_COLOR, ste.getLineNumber(), RESET,
                BOLD + "»",
                MSG_COLOR, msg, RESET);
    }

    private static String formatForFile(LogLevel lvl, String time, String fullCls, StackTraceElement ste, String msg) {
        return String.format("[%s] %s %s::%s(L:%d) » %s",
                time, lvl.label, fullCls, ste.getMethodName(), ste.getLineNumber(), msg);
    }

    private static void saveToFile(String fullLog) {
        File file = new File(LOG_FILE);
        if (file.exists() && file.length() > MAX_FILE_SIZE) {
            rotateLogs(file);
        }
        try (FileWriter fw = new FileWriter(LOG_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.println(fullLog);
        } catch (IOException e) {
            System.err.println("Error al escribir log: " + e.getMessage());
        }
    }

    private static void rotateLogs(File currentFile) {
        File backup = new File(LOG_FILE + ".bak");
        if (backup.exists()) {
            backup.delete();
        }
        currentFile.renameTo(backup);
    }

    public static void logInfo(String m) { log(LogLevel.INFO, m); }
    public static void logError(String m) { log(LogLevel.ERROR, m); }
    public static void logWarn(String m) { log(LogLevel.WARN, m); }
    public static void logDebug(String m) { log(LogLevel.DEBUG, m); }
}