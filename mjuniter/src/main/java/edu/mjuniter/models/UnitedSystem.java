package edu.mjuniter.models;

import java.util.List;
import java.util.ArrayList;

import edu.mjuniter.models.Endpoint;
import edu.mjuniter.models.Microservice;
import edu.mjuniter.repositories.NeoRepository;

public class UnitedSystem {
    
    public UnitedSystem(List<Microservice> microservices){

    }

    public void analyze(boolean visualize){
        NeoRepository neo;
        if (visualize) {
            neo = new NeoRepository();
            neo.clearDB();
            neo.addService("Employee Service");
            neo.close();
        }
        return;
    }

    public void build(){
        return;
    }
}
