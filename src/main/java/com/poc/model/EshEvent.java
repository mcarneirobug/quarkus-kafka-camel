package com.poc.model;

public class EshEvent {
    public String correlationId;
    public String name;
    public String label;
    public String tag;
    public String value1;
    public String value2;
    public String value3;

    public boolean isComplete() {
        return name != null && label != null && tag != null &&
                value1 != null && value2 != null && value3 != null;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    @Override
    public String toString() {
        return "EshEvent{" +
                "correlationId='" + correlationId + '\'' +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", tag='" + tag + '\'' +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                ", value3='" + value3 + '\'' +
                '}';
    }
}