package com.easemob.push.admin;

import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;

public class Entry extends NodeExtension {
    private String payLoad;

    public Entry() {
        super(PubSubElementType.ITEM);
    }

    public Entry(String payLoad) {
        // The element type is actually irrelevant since we override getNamespace() to return null
        super(PubSubElementType.ITEM);
        this.payLoad = payLoad;
    }

    public String getPayLoad() {
        return payLoad;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String toXML() {
        return "<entry xmlns='easemob:pubsub'>" + payLoad + "</entry>";
    }

    @Override
    public String toString() {
        return getClass().getName() + " | Content [" + toXML() + "]";
    }
}
