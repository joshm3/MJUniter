package edu.mjuniter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.util.Properties;

import edu.mjuniter.models.Microservice;
import edu.mjuniter.models.UnitedSystem;
import edu.mjuniter.repositories.NeoRepository;

public class MJUniter {

    public static void main(String... args) {

        System.out.println("Starting MJUniter");

        /*get mjuniter.properties to configure mjuniter*/
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

        //start connection to the neo4j repository and clear it
        NeoRepository neo = NeoRepository.inst();
        neo.clearDB();

        /*analyze each microservice while neo4j makes connects the 
            endpoints and creates the system dependency graph*/
        List<Microservice> services = new ArrayList<Microservice>();
        for (String serviceName:serviceNames){
            Microservice newService = new Microservice(serviceName, path);
            newService.analyze();
            services.add(newService);
        }

        //analyze the system and visualize the system dependency graph
        UnitedSystem unitedSystem = new UnitedSystem(services);
        unitedSystem.build();

        //close the neo4j connection
        neo.close();
    }
}
