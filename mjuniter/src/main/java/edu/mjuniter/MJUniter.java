package edu.mjuniter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import edu.mjuniter.models.Microservice;
import edu.mjuniter.models.UnitedSystem;
import edu.mjuniter.repositories.NeoRepository;
import spoon.Launcher;
import spoon.compiler.SpoonResource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

public class MJUniter {

    public static void main(String... args) {

        System.out.println("Starting MJUniter\n");

        /*get mjuniter.properties to configure mjuniter*/
        String path;
        List<String> serviceNames = new ArrayList<String>();
        Properties prop = new Properties();
        try(InputStream fis = MJUniter.class.getClassLoader().getResourceAsStream("mjuniter.properties");) {
            prop.load(fis);
            path = prop.getProperty("path").replace('\\', '/');
            serviceNames = Arrays.asList(prop.getProperty("microserviceNames").split(","));
        }
        catch(Exception e) {
            System.out.println("Error loading mjuniter.properties");
            return;
        }

        //start connection to the neo4j repository and clear it
        NeoRepository neo = NeoRepository.inst();
        neo.clearDB();

        /*analyze each microservice while neo4j connects the 
            endpoints and creates the system dependency graph*/
        List<Microservice> services = new ArrayList<Microservice>();
        for (String serviceName:serviceNames){
            Microservice newService = new Microservice(serviceName, path);
            newService.analyze();
            services.add(newService);
        }

        //build the modular monolith
        UnitedSystem unitedSystem = new UnitedSystem(services, path);
        unitedSystem.build();

        
        neo.close(); //close the neo4j connection
        for (File file : new File("./../temp").listFiles()) 
            deleteDirectory(file);
    }



    static void deleteDirectory(File dir){
        File[] files = dir.listFiles();
        if (files != null){
            for (File file : files){
                deleteDirectory(file);
            }
        }
        dir.delete();
    }
}
