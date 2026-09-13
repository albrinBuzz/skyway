package com.SkyWay.config.webPay;

import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebpayConfiguration {

    @Value("${webpay.commerce-code:597055555532}")
    private String commerceCode;

    @Value("${webpay.api-key:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}")
    private String apiKey;

    @Value("${webpay.integration-type:TEST}")
    private String integrationType;

    @Bean
    public WebpayOptions webpayOptions() {
        IntegrationType type = IntegrationType.valueOf(integrationType.toUpperCase());
        return new WebpayOptions(commerceCode, apiKey, type);
    }
}