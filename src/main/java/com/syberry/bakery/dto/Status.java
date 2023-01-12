package com.syberry.bakery.dto;

public enum Status {
    ENABLED(true),
    DISABLED(false);

    private boolean value;
    private Status(boolean value){
        this.value = value;
    }

    public boolean getValue(){
        return this.value;
    }
}
