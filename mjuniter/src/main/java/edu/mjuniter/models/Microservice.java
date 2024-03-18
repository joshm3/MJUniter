package edu.mjuniter.models;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mjuniter.repositories.NeoRepository;
import org.apache.commons.lang3.tuple.Pair;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.Filter;
import spoon.reflect.declaration.CtInterface;


public class Microservice {
    String name;
    String path;
    NeoRepository neo;
    
    public Microservice(String name, String basePath){
        this.name = name;
        this.path = basePath + "/" + name; //not that sure about this
        //neo = NeoRepository.inst();

        //neo.addMicroservice(name);
    }

    //must recursively search through the microservice's /src/main/java directory looking for Controllers and Clients
    public void analyze(){
        System.out.println("Beginning analysis of " + name);
        String javaPath = path + "/src/main/java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(javaPath);
        CtModel model = launcher.buildModel();

        //look for all controllers
        for (CtElement ctElement : model.getElements(new ControllerFilter())) {
            //ControllerAnnotationFilter only matches instances of CtClass currently
            CtClass<?> ctClass = (CtClass<?>)ctElement;
            System.out.println("Controller class: " + ctClass.getQualifiedName());

            //now look for the endpoint urls
            var baseUri = AnnotationExtractor.getParams(ctClass, "org.springframework.web.bind.annotation.RequestMapping");
            System.out.println(baseUri);
        }

        //look for all clients
        for (CtElement ctElement : model.getElements(new ClientFilter())) {
            //Client filter only matches instances of CtInterface
            CtInterface<?> ctInter = (CtInterface<?>)ctElement;
            System.out.println("Client interface: " + ctInter.getQualifiedName());

            //now look for the endpoint
        }

        
    }

    static class AnnotationExtractor{
        public static String get(CtElement e, String annClassName) {
            for (CtAnnotation<?> annotation : e.getAnnotations()) {
                if (annotation.getAnnotationType().getQualifiedName()
                        .equals(annClassName)) {
                    String val = annotation.getValue("value").toString();
                    return val;
                }
            } //for
            return null;
        }
        public static Map<String, String> getParams(CtElement e, String annClassName) {
            for (CtAnnotation<?> annotation : e.getAnnotations()) {
                if (annotation.getAnnotationType().getQualifiedName()
                        .equals(annClassName)) {
                    Map<String, String> res = new HashMap<>();
                    Map<String, CtExpression> map =  annotation.getAllValues();
                    System.out.println("map is " + map);
                    for (Map.Entry<String, CtExpression> entry : map.entrySet())
                        if (entry.getValue() != null)
                            res.put(entry.getKey(), entry.getValue().toString());
                    return res;
                }
            } //for
            return null;
        }
    }

    // Custom filter to search for classes with either @RestController or @Controller annotation
    static class ControllerFilter implements Filter<CtElement> {
        @Override
        public boolean matches(CtElement element) {
            if (element instanceof CtClass) {
                CtClass<?> ctClass = (CtClass<?>) element;
                return ctClass.getAnnotation(org.springframework.web.bind.annotation.RestController.class) != null ||
                    ctClass.getAnnotation(org.springframework.stereotype.Controller.class) != null;
            }
            return false;
        }
    }

    // Custom filter to search for classes with either @RestController or @Controller annotation
    static class ClientFilter implements Filter<CtElement> {
        @Override
        public boolean matches(CtElement element) {
            if (element instanceof CtInterface) {
                CtInterface<?> inter = (CtInterface<?>) element;
                return inter.getAnnotation(org.springframework.web.service.annotation.HttpExchange.class) != null;
            }
            return false;
        }
    }

















// File curFile = new File(javaPath);
        // if (curFile.exists()) {
        //     recursiveSearch(curFile);
        // }
        // else {
        //     System.out.println("ERROR: file at path " + curFile.getAbsolutePath() + " does not exist");
        // }
        // System.out.println();


    // private void recursiveSearch(File curFile){
    //     if (curFile.isDirectory()) {
    //         File[] subFiles = curFile.listFiles();
    //         for (File subFile : subFiles) {
    //             recursiveSearch(subFile);
    //         }
    //     } else {
    //         if (curFile.getName().endsWith(".java")) {
    //             analyzeJavaFile(curFile);
    //         }
    //     }

    //     //close files in here
    // }

    // //analyze a java file to see if it contains a controller and or client
    // private void analyzeJavaFile(File javaFile){

    //     System.out.print(javaFile.getName() + " contains: ");

    //     boolean controller = false;
    //     boolean client = false;

    //     //open it up in spoon and check for controller or client
    //     // - it would be nice if they were in sub-packages named "controllers" and "clients" but this is uncommon
    //     // Launcher launcher = new Launcher();
    //     // launcher.addInputResource(javaFile.getAbsolutePath()); // Replace with your source code path
    //     // launcher.buildModel();
    //     // Model model = launcher.getModel();

    //     //spring controllers have @controller or @restcontroller annotations


    //     //spring clients have RestClient (could be feign client), WebClient, RestTemplate, or HTTP Interface, 



    //     if (controller){
    //         System.out.println("controller");
    //         //create a controller object? - let it analyze it automatically

    //     }
    //     if (client){
    //         System.out.println("client");
    //         //create a client object? - let it analyze it automatically

    //     } else {
    //         System.out.println("no controllers or clients");
    //     }
    // }
}
