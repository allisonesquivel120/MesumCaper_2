package edu.up.cs301.museumcaper;

import java.io.Serializable;

public class Room implements Serializable
{
    private static final long serialVersionUID = 7737393762469851826L;

    private int id;
    private boolean hasPainting;
    private boolean hasCamera;
    private boolean alarmTriggered;
    private boolean powerOn;


    public Room(int id)
    {
        this.id = id;
        this.hasPainting = false;
        this.hasCamera = false;
        this.powerOn = true;
    }
    public Room (Room orig)
    {
        this.id = orig.id;
        this.hasCamera = orig.hasCamera;;
        this.hasPainting = orig.hasPainting;
        this.alarmTriggered = orig.alarmTriggered;
        this.powerOn = orig.powerOn;
    }

    // getters
    public int getId()
    {
        return id;
    }
    public boolean hasItem()
    {
        return hasPainting;
    }
    public boolean hasCamera()
    {
        return hasCamera;
    }
    public boolean isAlarmTriggered()
    {
        return alarmTriggered;
    }
    public boolean isPowerOn(){ return powerOn; }

    // setters
    public void setHasPainting(boolean hasPainting)
    {
        this.hasPainting = hasPainting;
    }
    public void setHasCamera(boolean hasCamera)
    {
        this.hasCamera = hasCamera;
    }
    public void setAlarmTriggered(boolean alarmTriggered) { this.alarmTriggered = alarmTriggered;}
    public void setPowerOn(boolean powerOn) { this.powerOn = powerOn; }

}


