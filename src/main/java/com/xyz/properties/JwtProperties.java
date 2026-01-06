package com.xyz.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "trade.jwt")
@Data
public class JwtProperties {

    private String secretKey;
    private long expirationTime;

}
