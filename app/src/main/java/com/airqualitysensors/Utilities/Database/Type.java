package com.airqualitysensors.Utilities.Database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "types")
public class Type {
    @PrimaryKey(autoGenerate = true)
    public int typeId;

    public String name;
    public String unit;
}
