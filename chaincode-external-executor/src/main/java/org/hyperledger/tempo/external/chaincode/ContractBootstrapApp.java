package org.hyperledger.tempo.external.chaincode;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = {"org.hyperledger.tempo"})
@Configuration
public class ContractBootstrapApp {
    private static ApplicationContext applicationContext;


    public static void main(String[] args) {

        applicationContext =
                new AnnotationConfigApplicationContext(ContractBootstrapApp.class);
    }
}
