package ru.ant.iot.ifttt;

import ru.ant.iot.cloud.queue.JsonTask;

/**
 * Created by Ant on 15.05.2016.
 */
public class TaskReportTrigger extends BaseIftttTrigger {

    private final Class<? extends JsonTask> cls;

    public TaskReportTrigger(Class<? extends JsonTask> cls) {
        this.cls = cls;
    }

    @Override
    public String getIftttEventName() {
        return "task-report";
    }

    @Override
    protected IftttMessage initMessage() {
        return new IftttMessage(cls.getSimpleName());
    }

}
