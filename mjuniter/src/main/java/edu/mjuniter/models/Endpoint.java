package edu.mjuniter.models;

// import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;

public record Endpoint (String url, CtMethod<?> method, boolean internal){}
