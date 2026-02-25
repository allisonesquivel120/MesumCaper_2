package edu.up.cs301.museumcaper;

import java.io.Serializable;

public class Room implements Serializable
{
    private static final long serialVersionUID = 7737393762469851826L;

    private int id;
    private boolean hasItem;
    private boolean hasAlarm;
    private boolean alarmTriggered;

    public Room(int id)
    {
        this.id = id;
        this.hasItem = false;
        this.hasAlarm = false;
        this.alarmTriggered = false;
    }
    public Room (Room orig)
    {
        this.id = orig.id;
        this.hasAlarm = orig.hasAlarm;;
        this.hasItem = orig.hasItem;
        this.alarmTriggered = orig.alarmTriggered;
    }

    public int getId()
    {
        return id;
    }
    public boolean hasItem()
    {
        return hasItem;
    }
    public boolean HasAlarm()
    {
        return
    }
}
