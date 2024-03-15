package edu.mjuniter.models;

import java.io.File;

import edu.mjuniter.repositories.NeoRepository;

public class Microservice {
    String name;
    String path;
    NeoRepository neo;
    
    public Microservice(String name, String path){
        this.name = name;
        this.path = path;
        neo = NeoRepository.inst();

        neo.addMicroservice(name);
    }

    //must recursively search through the microservice's /src/main/java directory looking for Controllers and Clients
    public void analyze(){
        File curFile = new File(path);
        recursiveSearch(curFile);
    }

    private void recursiveSearch(File curFile){
        if (curFile.isDirectory()) {
            File[] subFiles = curFile.listFiles();
            for (File subFile : subFiles) {
                recursiveSearch(subFile);
            }
        } else {
            if (curFile.getName().endsWith(".java")) {
                analyzeJavaFile(curFile);
            }
        }
    }

    //analyze a java file to see if it contains a controller and or client
    private void analyzeJavaFile(File javaFile){
        boolean controller = false;
        boolean client = false;

        //open it up in spoon and check for controller or client



        if (controller){
            //create a controller object? - let it analyze it automatically

        }
        if (client){
            //create a client object? - let it analyze it automatically

        }
    }
}
