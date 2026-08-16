/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.minimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mycompany.minimarket", "controller", "service", "repository"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "com.mycompany.minimarket")
public class MiniMarket {

    public static void main(String[] args) {
        SpringApplication.run(MiniMarket.class, args);
    }
}