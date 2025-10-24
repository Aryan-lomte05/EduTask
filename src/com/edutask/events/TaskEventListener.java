package com.edutask.events;

public interface TaskEventListener<T> {
    void onEvent(T event);
}