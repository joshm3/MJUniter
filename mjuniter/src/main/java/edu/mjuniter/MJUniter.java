package edu.mjuniter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.util.Properties;

import edu.mjuniter.models.Microservice;
import edu.mjuniter.models.UnitedSystem;

public class MJUniter {

    public static void main(String... args) {

        System.out.println("Starting MJUniter");

        String path;
        List<String> serviceNames = new ArrayList<String>();
        boolean visualize;
        Properties prop = new Properties();
        try(InputStream fis = MJUniter.class.getClassLoader().getResourceAsStream("mjuniter.properties");) {
            prop.load(fis);
            path = prop.getProperty("path");
            serviceNames = Arrays.asList(prop.getProperty("microserviceNames").split(","));
            visualize = prop.getProperty("visualize").equals("true");
        }
        catch(Exception e) {
            System.out.println("Error loading mjuniter.properties");
            return;
        }

        //analyze each microservice
        List<Microservice> services = new ArrayList<Microservice>();
        for (String serviceName : serviceNames){
            Microservice newService = new Microservice(serviceName, path);
            newService.analyze();
            services.add(newService);
        }

        //analyze the system and visualize the system dependency graph
        UnitedSystem unitedSystem = new UnitedSystem(services);
        unitedSystem.analyze(visualize);

        //build the modular monolith
        unitedSystem.build();
    }
}
