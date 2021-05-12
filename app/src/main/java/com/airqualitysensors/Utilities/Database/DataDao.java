package com.airqualitysensors.Utilities.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface DataDao {
    @Query("SELECT * FROM timeddata")
    List<TimedData> getAllData();

    @Query("SELECT * FROM timeddata WHERE typeId = :typeId")
    List<TimedData> getDataByType(int typeId);

    @Insert
    void insertAll(TimedData... data);

    @Insert
    void insertAll(Type... types);

    @Delete
    void delete(TimedData... data);

    @Query("DELETE FROM timeddata")
    void deleteData();

    @Query("DELETE FROM types")
    void deleteTypes();

    @Query("SELECT * FROM types")
    List<Type> getAllTypes();

    @Transaction
    @Query("SELECT * FROM types")
    public List<TypeWithData> getTypeWithData();
}
