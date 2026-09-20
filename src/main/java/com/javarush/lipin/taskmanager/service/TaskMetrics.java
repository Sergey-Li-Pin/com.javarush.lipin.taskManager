package com.javarush.lipin.taskmanager.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TaskMetrics {

    private final Counter tasksCreatedCounter;
    private final Timer taskCreationTimer;

    public TaskMetrics(MeterRegistry registry) {
        this.tasksCreatedCounter = Counter.builder("taskmanager_tasks_created_total")
                .description("Total number of created tasks")
                .register(registry);
        this.taskCreationTimer = Timer.builder("taskmanager_task_creation_seconds")
                .description("Time spent creating a task")
                .register(registry);
    }

    public void onTaskCreated() {
        tasksCreatedCounter.increment();
    }

    public Timer.Sample startTimer(MeterRegistry registry) {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(taskCreationTimer);
    }
}