package ru.ant.iot.ifttt;

import ru.ant.iot.cloud.queue.JsonTask;

/**
 * Created by Ant on 15.05.2016.
 */
public class TaskReportTrigger extends BaseIftttTrigger {

    private final Class<? extends JsonTask> cls;
    private final String taskAttributesDescription;

    public TaskReportTrigger(Class<? extends JsonTask> cls) {
        this(cls, null);
    }

    public TaskReportTrigger(Class<? extends JsonTask> cls, String taskAttributesDescription) {
        this.cls = cls;
        this.taskAttributesDescription = taskAttributesDescription;
    }

    @Override
    public String getIftttEventName() {
        return "task-report";
    }

    @Override
    protected IftttMessage initMessage() {
        return new IftttMessage(cls.getSimpleName(), taskAttributesDescription);
    }

}
