package com.mobsoft.library;

public class InitializedMobSoftResult<T> {
    private T value;
    private Exception error;

    public InitializedMobSoftResult(T value) {
        this.value = value;
    }

    public InitializedMobSoftResult(Exception error) {
        this.error = error;
    }

    public T getValue() {
        return value;
    }

    public Exception getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

}
