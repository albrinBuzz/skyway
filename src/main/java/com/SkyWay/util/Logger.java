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

    // Detección y estado de soporte ANSI
    private static boolean enableColors = checkAnsiSupport();

    // --- FORMATO Y COLORES ANSI SOBRIOS ---
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static final String TIME_COLOR = "\u001B[90m";        // Gris tenue
    private static final String CLASS_COLOR = "\u001B[38;5;39m";  // Azul corporativo
    private static final String METHOD_COLOR = "\u001B[38;5;76m"; // Verde sobrio
    private static final String LINE_COLOR = "\u001B[38;5;214m";  // Naranja atenudao
    private static final String MSG_COLOR = "\u001B[97m";         // Blanco tenue

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
     * Evalúa la compatibilidad de la terminal con secuencias de colores ANSI.
     */
    private static boolean checkAnsiSupport() {


        // 1. Variable de Entorno Directa
        String ansiEnv = System.getenv("LOGGING_ANSI_ENABLED");
        if (ansiEnv != null) {
            boolean res = Boolean.parseBoolean(ansiEnv.trim());
            return res;
        }
        return false;
    }

    public static void setEnableColors(boolean enable) {
        enableColors = enable;
    }

    public static synchronized void log(LogLevel level, String message) {
        StackTraceElement ste = getCallerStackTraceElement();
        String time = dtf.format(LocalDateTime.now());
        String fullClassName = ste.getClassName();

        // Salida formateada a Consola
        String consoleLog = formatForConsole(level, time, fullClassName, ste, message);
        System.out.println(consoleLog);

        // Registro transparente en Archivo plano
        String fileLog = formatForFile(level, time, fullClassName, ste, message);
        saveToFile(fileLog);
    }

    /**
     * Formato formateado con resaltado ANSI para consolas interactiva.
     */
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

        return String.format("%s%s%s %s%s%s %s%s%s%s%s%s::%s%s%s(%sL:%d%s) %s %s%s%s",
                TIME_COLOR, time, RESET,
                lvl.color + BOLD, lvl.label, RESET,
                TIME_COLOR, packageName, RESET,
                BOLD + CLASS_COLOR, className, RESET,
                METHOD_COLOR, ste.getMethodName(), RESET,
                LINE_COLOR, ste.getLineNumber(), RESET,
                BOLD + ">",
                MSG_COLOR, msg, RESET);
    }

    /**
     * Formato estándar limpio sin secuencias de escape ANSI.
     */
    private static String formatForFile(LogLevel lvl, String time, String fullCls, StackTraceElement ste, String msg) {
        return String.format("[%s] %s %s::%s(L:%d) > %s",
                time, lvl.label, fullCls, ste.getMethodName(), ste.getLineNumber(), msg);
    }

    /**
     * Obtiene el StackTraceElement de la clase llamante fuera del Logger.
     */
    private static StackTraceElement getCallerStackTraceElement() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 2; i < elements.length; i++) {
            String className = elements[i].getClassName();
            if (!className.equals(Logger.class.getName())) {
                return elements[i];
            }
        }
        return elements[3];
    }

    private static void saveToFile(String fullLog) {
        File file = new File(LOG_FILE);
        if (file.exists() && file.length() > MAX_FILE_SIZE) {
            rotateLogs(file);
        }
        try (FileWriter fw = new FileWriter(LOG_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.println(fullLog);
        } catch (IOException e) {
            System.err.println("Error al escribir log en archivo: " + e.getMessage());
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