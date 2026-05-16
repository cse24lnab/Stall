package org.lab.stall_manage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 为了密钥的安全性。为了解耦
@Component
@ConfigurationProperties(prefix = "lab.jwt")
@Data
public class JwtProperties {
    private String secretKey;
    private long time;
}
