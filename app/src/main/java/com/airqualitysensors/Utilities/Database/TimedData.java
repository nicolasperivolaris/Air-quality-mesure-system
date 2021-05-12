package com.airqualitysensors.Utilities.Database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.Date;


@Entity
public class TimedData{
    @PrimaryKey(autoGenerate = true)
    public int dataId;

    public int typeId;

    public Date time;

    public float value;
}

class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
