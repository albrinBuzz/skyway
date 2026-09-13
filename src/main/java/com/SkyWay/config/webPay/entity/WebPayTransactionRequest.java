package com.SkyWay.config.webPay.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebPayTransactionRequest {

    @JsonProperty("buy_order")
    private String buyOrder;

    @JsonProperty("session_id")
    private String sessionId;

    private int amount;

    @JsonProperty("return_url")
    private String returnUrl;

    // Constructor
    public WebPayTransactionRequest(String buyOrder, String sessionId, int amount, String returnUrl) {
        this.buyOrder = buyOrder;
        this.sessionId = sessionId;
        this.amount = amount;
        this.returnUrl = returnUrl;
    }

    // Getters and Setters
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
