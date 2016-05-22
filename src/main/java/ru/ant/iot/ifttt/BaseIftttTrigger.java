package ru.ant.iot.ifttt;

import ru.ant.common.App;

/**
 * Created by ant on 22.05.2016.
 */
public abstract class BaseIftttTrigger extends IftttTrigger {
    private String iftttMakerKey;
    protected IftttMessage preveousMsg = new IftttMessage();

    @Override
    protected String getIftttMakerKey() {
        if(iftttMakerKey!=null) return iftttMakerKey;
        return iftttMakerKey = App.getProperty("ifttt.maker.key");
    }

    @Override
    protected boolean triggerConditionIsMet() {
        if(msg.equals(preveousMsg)) return false;
        preveousMsg = msg;
        if(preveousMsg.isEmpty()) return false;
        return true;
    }

}
