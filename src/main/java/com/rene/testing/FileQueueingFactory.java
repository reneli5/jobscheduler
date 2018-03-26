package com.rene.testing;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileQueueingFactory {

    private static BlockingQueue<String> messageQueue;

    public static BlockingQueue<String> getMessageQueue(){

        if(messageQueue == null){
            messageQueue =  new ArrayBlockingQueue<>(10);
        }

        return messageQueue;
    }
}
