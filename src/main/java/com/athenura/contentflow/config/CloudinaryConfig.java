package com.athenura.contentflow.config;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dwfqsavsx",
                "api_key", "911959791416542",
                "api_secret", "IbfJ3lzDkXhYonTIAqxAe51tvQ8"
        ));
    }
}
