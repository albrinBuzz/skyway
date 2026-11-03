package com.SkyWay.util;

import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BarcodePDF417;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BarcodeGeneratorUtil {

    private BarcodeGeneratorUtil() {}

    /**
     * Genera un código de barras 2D PDF417 (Estándar Oficial IATA Res. 792 para Bar Coded Boarding Passes)
     * retornando un Data URL Base64 listo para utilizar directamente en etiquetas &lt;img src="..." /&gt;
     * sin necesidad de APIs externas ni conexión a internet.
     */
    public static String generarPdf417DataUrl(String data) {
        if (data == null || data.isBlank()) {
            return "";
        }

        try {
            BarcodePDF417 pdf417 = new BarcodePDF417();
            pdf417.setText(data.getBytes(StandardCharsets.UTF_8));
            java.awt.Image awtImg = pdf417.createAwtImage(Color.BLACK, Color.WHITE);

            int w = Math.max(1, awtImg.getWidth(null));
            int h = Math.max(1, awtImg.getHeight(null));

            BufferedImage bImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bImage.createGraphics();
            g2d.drawImage(awtImg, 0, 0, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            Logger.logError("Error generando código de barras PDF417: " + e.getMessage());
            return "";
        }
    }

    /**
     * Genera un código de barras 1D Code128 en Base64 Data URL como alternativa/respaldo.
     */
    public static String generarCode128DataUrl(String data) {
        if (data == null || data.isBlank()) {
            return "";
        }

        try {
            Barcode128 code128 = new Barcode128();
            code128.setCode(data);
            code128.setCodeType(Barcode128.CODE128);
            java.awt.Image awtImg = code128.createAwtImage(Color.BLACK, Color.WHITE);

            int w = Math.max(1, awtImg.getWidth(null));
            int h = Math.max(1, awtImg.getHeight(null));

            BufferedImage bImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bImage.createGraphics();
            g2d.drawImage(awtImg, 0, 0, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            Logger.logError("Error generando código de barras Code128: " + e.getMessage());
            return "";
        }
    }
}
