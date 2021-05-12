package com.airqualitysensors.Utilities.Database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TypeWithData {
    @Embedded
    public Type type;
    @Relation(
            parentColumn = "typeId",
            entityColumn = "typeId"
    )
    public List<TimedData> data;
}
