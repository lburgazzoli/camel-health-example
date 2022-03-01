package io.github.lburgazzoli.camel.health;

import org.apache.camel.quarkus.main.CamelMainApplication;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class ExampleMain {
    public static void main(String[] args) throws Exception {
        Quarkus.run(CamelMainApplication.class, args);
    }
}
