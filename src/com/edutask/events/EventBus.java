package com.edutask.events;

import java.util.*;

public class EventBus {
    private Map<Class<?>, List<TaskEventListener<?>>> listeners = new HashMap<>();

    public <T> void subscribe(Class<T> eventType, TaskEventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<TaskEventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (TaskEventListener<?> listener : eventListeners) {
                ((TaskEventListener<T>) listener).onEvent(event);
            }
        }
    }
}
