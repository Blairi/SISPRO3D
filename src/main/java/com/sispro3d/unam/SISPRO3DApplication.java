package com.sispro3d.unam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SISPRO3DApplication {
    public static void main(String[] args) {
        SpringApplication.run(SISPRO3DApplication.class, args);
    }
}
