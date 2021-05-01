package com.example.cse_4095_final_project.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cse_4095_final_project.db.Stock;

@Dao
public interface StockDao {

    @Insert
    void insert(Stock stock);

    @Delete
    void delete(Stock stock);

    @Update
    void update(Stock stock);

    @Query("SELECT * FROM Stock")
    LiveData<List<Stock>> getAll();

    @Query("DELETE FROM Stock")
    void deleteAll();

    @Query("SELECT * FROM Stock where name = :name")
    Stock isStockInDatabase(String name);
}