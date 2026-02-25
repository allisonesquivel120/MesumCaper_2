package edu.up.cs301.museumcaper;

import java.io.Serializable;

public class Room implements Serializable
{
    private static final long serialVersionUID = 7737393762469851826L;

    private int id;
    private boolean hasItem;
    private boolean hasAlarm;

    public Room(int id)
    {
        this.id = id;
        this.hasItem = false;
        this.hasAlarm = false;
    }
    public Room (Room orig)
    {
        this.id = orig.id;
        this.hasAlarm = orig.hasAlarm;;
        this.hasItem = orig.hasItem;

    }

    // getters
    public int getId()
    {
        return id;
    }
    public boolean hasItem()
    {
        return hasItem;
    }
    public boolean hasAlarm()
    {
        return hasAlarm;
    }

    // setters
    public void setHasItem(boolean hasItem)
    {
        this.hasItem = hasItem;
    }
    public void setHasAlarm(boolean hasAlarm)
    {
        this.hasAlarm = hasAlarm;
    }
}


