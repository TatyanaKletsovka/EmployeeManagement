package com.syberry.bakery.exception;

public class InvalidRoleTypeException extends RuntimeException{
    public InvalidRoleTypeException(String message){
        super(message);
    }
}
