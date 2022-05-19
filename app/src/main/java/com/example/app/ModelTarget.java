package com.example.app;

public class ModelTarget {
    public String typeA, descA, startdate, own, state, ownName, enddate;

    public ModelTarget() {
    }

    public ModelTarget(String typeA, String descA, String startdate, String own, String state, String ownName, String enddate) {
        this.typeA = typeA;
        this.descA = descA;
        this.startdate = startdate;
        this.own = own;
        this.state = state;
        this.ownName = ownName;
        this.enddate = enddate;
    }

    public String getTypeA() {
        return typeA;
    }

    public void setTypeA(String typeA) {
        this.typeA = typeA;
    }

    public String getDescA() {
        return descA;
    }

    public void setDescA(String descA) {
        this.descA = descA;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getOwn() {
        return own;
    }

    public void setOwn(String own) {
        this.own = own;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOwnName() {
        return ownName;
    }

    public void setOwnName(String ownName) {
        this.ownName = ownName;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
}
