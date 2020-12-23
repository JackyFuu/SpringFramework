package com.jacky.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jacky
 * @time 2020-12-21 17:16
 * @discription 通过一个简单的JavaBean持有所有的配置。
 */
@Component
public class SmtpConfig {

    @Value("${smtp.host: localhost}")
    private String host;

    @Value("${smtp.port:25}")
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
