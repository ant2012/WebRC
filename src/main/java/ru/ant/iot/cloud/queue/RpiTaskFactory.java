package ru.ant.iot.cloud.queue;

import org.apache.commons.lang.NotImplementedException;

import javax.json.JsonObject;

/**
 * Created by ant on 18.05.2016.
 */
public class RpiTaskFactory extends JsonTaskFactory {
    @Override
    public JsonTask getTask(JsonObject json) {
        String taskClass = json.getString("class");
        switch(taskClass){
            case "reboot":
                return new RebootTask();
            case "wol":
                return new WolTask(json);
            default:
                log.error("Init task error", new NotImplementedException("Task class " + taskClass + " is not supported"));
                return null;
        }

    }
}
