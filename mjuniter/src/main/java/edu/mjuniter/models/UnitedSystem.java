package edu.mjuniter.models;

import java.util.List;
import java.util.ArrayList;

import edu.mjuniter.models.Endpoint;
import edu.mjuniter.models.Microservice;
import edu.mjuniter.repositories.NeoRepository;

public class UnitedSystem {
    
    public UnitedSystem(List<Microservice> microservices){

    }

    public void analyze(){
        return;
    }

    public void visualize(){
        NeoRepository neo = new NeoRepository();
        neo.test();
        neo.close();
    }

    public void build(){
        return;
    }
}