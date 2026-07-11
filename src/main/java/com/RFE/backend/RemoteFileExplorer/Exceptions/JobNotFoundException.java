package com.RFE.backend.RemoteFileExplorer.Exceptions;


public class JobNotFoundException extends RuntimeException{
    public JobNotFoundException(String message){
        super(message);
    }
}
