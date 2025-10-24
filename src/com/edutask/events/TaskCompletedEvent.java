package com.edutask.events;

import com.edutask.model.Task;

public class TaskCompletedEvent {
    private Task task;

    public TaskCompletedEvent(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
