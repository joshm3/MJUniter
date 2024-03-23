package edu.mjuniter.models;

import java.util.List;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.annotation.AnnotationFilter;

import java.util.ArrayList;
import java.util.HashSet;

import edu.mjuniter.models.Endpoint;
import edu.mjuniter.models.Microservice;
import edu.mjuniter.repositories.NeoRepository;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.compiler.Environment.PRETTY_PRINTING_MODE;
import spoon.reflect.CtModel;
import spoon.reflect.CtModelImpl.CtRootPackage;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.factory.Factory;

//this class will take in all of the microservice objects (use names and paths)
//  and uses it along with the neo4j repository's analysis to build the united service
public class UnitedSystem {
    List<Microservice> services;
    Launcher launcher;
    CtModel model;
    Factory factory;
    String path;
    
    public UnitedSystem(List<Microservice> services, String path){
        this.services = services;
        this.path = path;
        launcher = new Launcher();
        launcher.addInputResource("./../temp");
        model = launcher.buildModel();
        factory = launcher.getFactory();
    }

    //builds the modular monolith
    public void build(){
        CtPackage modPack = model.getRootPackage().getPackage("modules");

        //need to create a new main class to start everything up
        buildMainClass(modPack);

        //need to create api functions for all of the internally used functions
        for (Microservice service : services){
            CtPackage servPack = modPack.getPackage(service.getPackName());
            removeMainClass(servPack);
            buildApiClass(modPack, service); //************************* */
        }

        //for all of the endpoints that are internally used, 
        //  create a public method in that module api class with the same logic as the endpoint
        //  change calls to that endpoint from the clients to call this new public api method
        //also create these public api classes for all of the service because why not


        //probably need to modify things to be protected or normal instead of public
        //ask dr t about this though because that might cause issues
        //really just want to enforce coupling

        //need to prettyPrint this thing out
        launcher.setSourceOutputDirectory(path + "/../united-monolith");
        launcher.prettyprint();
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //use an annotation filter to find the class with @SpringBootApplication
    private void removeMainClass(CtPackage servPack){
        //AnnotationFilter filter = new AnnotationFilter//
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! finish
    private void buildApiClass(CtPackage servPack, Microservice service){
        CtClass<?> apiClass = factory.createClass(servPack, "Api");

        // CtTypeReference<SpringBootApplication> sbaRef = launcher.getFactory().Type().createReference(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        // CtAnnotation<?> sbaAnn = factory.createAnnotation(sbaRef);
        // mainClass.addAnnotation(sbaAnn);

        // CtTypeReference<EnableDiscoveryClient> edcRef = launcher.getFactory().Type().createReference(org.springframework.cloud.client.discovery.EnableDiscoveryClient.class);
        // CtAnnotation<?> edcAnn = factory.createAnnotation(edcRef);
        // mainClass.addAnnotation(edcAnn);

        // CtMethod<?> mainMeth = launcher.getFactory().createMethod();
        // mainClass.addMethod(mainMeth);
        // mainMeth.setSimpleName("main");
        // mainMeth.setType(factory.Type().voidPrimitiveType());
        
        // mainMeth.addModifier(ModifierKind.PUBLIC);
        // mainMeth.addModifier(ModifierKind.STATIC);
        // CtParameter<String[]> args = factory.createParameter().setType(factory.Type().createArrayReference(factory.Type().STRING));
        // args.setSimpleName("args");
        // mainMeth.addParameter(args);
        // //setname??

        // CtBlock<Void> body = factory.createBlock();
        // body.addStatement(factory.createCodeSnippetStatement("SpringApplication.run(DepartmentServiceApplication.class, args)"));
        // mainMeth.setBody(body);
    }


    private void buildMainClass(CtPackage modPack){
        CtClass<?> mainClass = factory.createClass(modPack, "Application");

        CtTypeReference<SpringBootApplication> sbaRef = launcher.getFactory().Type().createReference(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        CtAnnotation<?> sbaAnn = factory.createAnnotation(sbaRef);
        mainClass.addAnnotation(sbaAnn);

        CtTypeReference<EnableDiscoveryClient> edcRef = launcher.getFactory().Type().createReference(org.springframework.cloud.client.discovery.EnableDiscoveryClient.class);
        CtAnnotation<?> edcAnn = factory.createAnnotation(edcRef);
        mainClass.addAnnotation(edcAnn);

        CtMethod<?> mainMeth = launcher.getFactory().createMethod();
        mainClass.addMethod(mainMeth);
        mainMeth.setSimpleName("main");
        mainMeth.setType(factory.Type().voidPrimitiveType());
        
        mainMeth.addModifier(ModifierKind.PUBLIC);
        mainMeth.addModifier(ModifierKind.STATIC);
        CtParameter<String[]> args = factory.createParameter().setType(factory.Type().createArrayReference(factory.Type().STRING));
        args.setSimpleName("args");
        mainMeth.addParameter(args);
        //setname??

        CtBlock<Void> body = factory.createBlock();
        body.addStatement(factory.createCodeSnippetStatement("SpringApplication.run(DepartmentServiceApplication.class, args)"));
        mainMeth.setBody(body);
    }


    private void debugState(){
        for (CtPackage pack : model.getAllPackages()){
            System.out.println(pack.toString());
        } 

        model.filterChildren((element) -> element instanceof CtClass<?>)
            .forEach((CtClass<?> ctClass) -> System.out.println(ctClass.getSimpleName()));
    }
}
