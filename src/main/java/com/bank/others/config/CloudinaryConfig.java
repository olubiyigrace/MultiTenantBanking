package com.bank.others.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final Environment environment;

    @Bean
    public Cloudinary cloudinary() {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", environment.getProperty("cloudinary.cloud-name"),
                "api_key", environment.getProperty("cloudinary.api-key"),
                "api_secret", environment.getProperty("cloudinary.api-secret")
        ));
    }
}

