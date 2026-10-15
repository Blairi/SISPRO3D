package com.sispro3d.unam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SISPRO3DApplicationRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== SISPRO3D & Spring Boot =====");
    }
}
