package com.SkyWay.config.webPay.entity;

public class WebPayTransactionResponse {

    private String token;
    private String url;


    public WebPayTransactionResponse() {
        // Constructor vacío
    }


    // Constructor
    public WebPayTransactionResponse(String token, String url) {
        this.token = token;
        this.url = url;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "WebPayTransactionResponse{" +
                "token='" + token + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
