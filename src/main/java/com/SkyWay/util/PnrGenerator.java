package com.SkyWay.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PnrGenerator {

    // Set de caracteres alfanuméricos legibles (32 caracteres = exactamente 5 bits de entropía por carácter)
    private static final String ALPHANUMERIC_CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int PNR_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generarPnr() {
        try {
            // 1. Recolectar fuentes de entropía externas del sistema
            byte[] seedBytes = new byte[32];
            SECURE_RANDOM.nextBytes(seedBytes);

            long nanoTime = System.nanoTime();
            long threadId = Thread.currentThread().getId();

            // 2. Aplicar función HASH (SHA-256) sobre la combinación de aleatoriedad + tiempo + hilo
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(seedBytes);
            digest.update(longToBytes(nanoTime));
            digest.update(longToBytes(threadId));
            byte[] hash = digest.digest();

            // 3. Mapear los bytes procesados al CHARSET de forma uniforme
            StringBuilder sb = new StringBuilder(PNR_LENGTH);
            for (int i = 0; i < PNR_LENGTH; i++) {
                // Tomar 4 bytes del hash + aleatoriedad para seleccionar el índice
                int randomInt = Math.abs((hash[i] << 24) ^ (hash[i + 6] << 16) ^ (hash[i + 12] << 8) ^ hash[i + 18] ^ SECURE_RANDOM.nextInt());
                int index = randomInt % ALPHANUMERIC_CHARSET.length();
                sb.append(ALPHANUMERIC_CHARSET.charAt(index));
            }

            // 4. Aplicar Fisher-Yates Shuffle para mezclar aleatoriamente el resultado final
            char[] pnrChars = sb.toString().toCharArray();
            for (int i = pnrChars.length - 1; i > 0; i--) {
                int j = SECURE_RANDOM.nextInt(i + 1);
                char temp = pnrChars[i];
                pnrChars[i] = pnrChars[j];
                pnrChars[j] = temp;
            }

            return new String(pnrChars);

        } catch (NoSuchAlgorithmException e) {
            // Fallback seguro si SHA-256 no estuviera disponible en la JVM
            return generarPnrFallback();
        }
    }

    private static String generarPnrFallback() {
        StringBuilder sb = new StringBuilder(PNR_LENGTH);
        for (int i = 0; i < PNR_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(ALPHANUMERIC_CHARSET.length());
            sb.append(ALPHANUMERIC_CHARSET.charAt(index));
        }
        return sb.toString();
    }

    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}