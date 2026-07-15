package com.sispro3d.unam.core.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String entityName, Object id) {
        return new ResourceNotFoundException(entityName + " not founded with id: " + id);
    }
}