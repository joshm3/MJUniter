package edu.mjuniter.models;

import java.io.File;
import java.text.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mjuniter.repositories.NeoRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import spoon.Launcher;
import spoon.refactoring.Refactoring;
import spoon.reflect.CtModel;
import spoon.reflect.CtModelImpl.CtRootPackage;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.Filter;
import spoon.reflect.factory.Factory;
import spoon.reflect.declaration.CtInterface;


public class Microservice {
    private String name;
    private String packName;
    private String path;
    private NeoRepository neo;
    private Launcher launcher;
    private CtModel model;

    public Microservice(String name, String basePath){
        this.name = name;
        this.path = basePath + "/" + name;
        packName = name.replace("-", "");

        neo = NeoRepository.inst();
        neo.addMicroservice(name);

        launcher = new Launcher();
    }

    public Set<CtPackage> getBasePackages() {
        return model.getRootPackage().getPackages();
    }

    public String getName(){
        return name;
    }

    public String getPackName(){
        return packName;
    }

    //must recursively search through the microservice's /src/main/java directory looking for Controllers and Clients
    public void analyze(){
        System.out.println("Beginning analysis of " + name);

        

        String javaPath = path + "/src/main/java";

        launcher.addInputResource(javaPath);
        model = launcher.buildModel();

        Factory factory = launcher.getFactory();
        CtRootPackage rootPack = (CtRootPackage)model.getRootPackage();
        Set<CtPackage> basePacks = rootPack.getPackages();
        CtPackage modPack = factory.createPackage().setSimpleName("modules");
        rootPack.addPackage(modPack);
        CtPackage servPack = factory.createPackage().setSimpleName(packName);
        modPack.addPackage(servPack);
        for (CtPackage pack : basePacks){
            servPack.addPackage(pack.clone());
            rootPack.removePackage(pack);
        }
        launcher.setSourceOutputDirectory("./../temp");
        launcher.prettyprint();
        
        
        //look for all controllers
        for (CtElement ctElement : model.getElements(new ControllerFilter())) {
            //ControllerAnnotationFilter only matches instances of CtClass currently
            CtClass<?> ctClass = (CtClass<?>)ctElement;
            System.out.println("\tController class: " + ctClass.getSimpleName());
            neo.addController(name, ctClass.getSimpleName());

            //now look for the endpoint urls
            List<String> endpoints = extractEndpoints(ctClass);
            if (!endpoints.isEmpty())for (String uri : endpoints){
                System.out.println("\t\tEndpoint: " + uri);
                neo.addEndpointFromController(uri, name, ctClass.getSimpleName());
            }
        }

        //look for all clients
        for (CtElement ctElement : model.getElements(new ClientFilter())) {
            //Client filter only matches instances of CtInterface
            CtInterface<?> ctInter = (CtInterface<?>)ctElement;
            System.out.println("\tClient interface: " + ctInter.getSimpleName());
            neo.addClient(name, ctInter.getSimpleName());

            //now look for the endpoint
            List<String> endpoints = extractEndpoints(ctElement);
            if (!endpoints.isEmpty())for (String uri : endpoints){
                System.out.println("\t\tEndpoint: " + uri);
                neo.addEndpointFromClient(uri, ctInter.getSimpleName(), name);
            } 
        }
    }


    //extracts the uri endpoints out of any CtElement
    //only supports extraction from a limited amount of annotations
    private List<String> extractEndpoints(CtElement element){
        List<String> endpoints = new ArrayList<String>();

        if (element instanceof CtClass){
            CtClass<?> ctClass = (CtClass<?>)element;
            
            
            CtTypeReference<RequestMapping> reqMapRef = launcher.getFactory().Type().createReference(org.springframework.web.bind.annotation.RequestMapping.class);

            CtAnnotation<?> baseAnn;
            CtAnnotation<?> curAnn;
            if ((baseAnn = ctClass.getAnnotation(reqMapRef)) != null){
                String baseUri = baseAnn.getValue("value").toString();
                if (baseUri.contains("/")) baseUri = baseUri.replaceAll("\"", "");
                else baseUri = "";
                
                CtTypeReference<GetMapping> getMapRef = launcher.getFactory().Type().createReference(org.springframework.web.bind.annotation.GetMapping.class);
                CtTypeReference<PostMapping> posMapRef = launcher.getFactory().Type().createReference(org.springframework.web.bind.annotation.PostMapping.class);
                String curUri;
                for (CtMethod<?> method : ctClass.getAllMethods()){
                    curAnn = method.getAnnotation(getMapRef);
                    if (curAnn == null) curAnn = method.getAnnotation(posMapRef);
                    if (curAnn != null) {
                        curUri = curAnn.getValue("value").toString();
                        if (curUri.contains("/")) endpoints.add(baseUri + curUri.replaceAll("\"", ""));
                        else { endpoints.add(baseUri); }
                    }
                }
            }
        }
        if (element instanceof CtInterface){
            
            CtInterface<?> ctInt = (CtInterface<?>)element;

            CtTypeReference<HttpExchange> hExRef = launcher.getFactory().Type().createReference(org.springframework.web.service.annotation.HttpExchange.class);

            CtAnnotation<?> curAnn;
            if (ctInt.getAnnotation(hExRef) != null){
                CtTypeReference<GetExchange> getExRef = launcher.getFactory().Type().createReference(org.springframework.web.service.annotation.GetExchange.class);
                String curUri;

                for (CtMethod<?> method : ctInt.getAllMethods()){
                    if ((curAnn = method.getAnnotation(getExRef)) != null) {
                        curUri = curAnn.getValue("value").toString();
                        if (curUri.contains("/")) endpoints.add(curUri.replaceAll("\"", ""));
                    }
                }
            }
        }

        return endpoints;
    }


    // Custom filter to search for classes with either @RestController or @Controller annotation
    static class ControllerFilter implements Filter<CtElement> {
        @Override
        public boolean matches(CtElement element) {
            if (element instanceof CtClass) {
                CtClass<?> ctClass = (CtClass<?>) element;
                return ctClass.hasAnnotation(org.springframework.web.bind.annotation.RestController.class) || 
                    ctClass.hasAnnotation(org.springframework.stereotype.Controller.class);
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
                return inter.hasAnnotation(org.springframework.web.service.annotation.HttpExchange.class);
            }
            return false;
        }
    }
}