package com.SkyWay.config.webPay.entity;

public class WebPayTransactionStatusResponse {

    private String buyOrder;
    private String sessionId;
    private int amount;
    private String status;  // Estado de la transacción (por ejemplo, "AUTHORIZED", "DECLINED", etc.)
    private String vci;     // Valor del código de la transacción

    // Constructor vacío necesario para Jackson
    public WebPayTransactionStatusResponse() {
        // Constructor vacío
    }

    // Constructor con parámetros
    public WebPayTransactionStatusResponse(String buyOrder, String sessionId, int amount, String status, String vci) {
        this.buyOrder = buyOrder;
        this.sessionId = sessionId;
        this.amount = amount;
        this.status = status;
        this.vci = vci;
    }

    // Getters y Setters
    public String getBuyOrder() {
        return buyOrder;
    }

    public void setBuyOrder(String buyOrder) {
        this.buyOrder = buyOrder;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVci() {
        return vci;
    }

    public void setVci(String vci) {
        this.vci = vci;
    }
}
