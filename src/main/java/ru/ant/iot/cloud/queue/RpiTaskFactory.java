package ru.ant.iot.cloud.queue;

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
            default:
                return null;
        }

    }
}
