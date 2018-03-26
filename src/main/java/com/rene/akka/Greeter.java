package com.rene.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Greeter extends AbstractActor {

    private final String message;
    private final ActorRef printerActor;
    private String greeting = "";

    static public Props props(String message, ActorRef printerActor) {
        return Props.create(Greeter.class, () -> new Greeter(message, printerActor));
    }

    public Greeter(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }



}
