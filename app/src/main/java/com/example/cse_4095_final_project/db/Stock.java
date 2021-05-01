package com.example.cse_4095_final_project.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;

@Entity
public class Stock {

    public Stock(@NonNull String name, double price) {
        this.name = name;
        this.price = price;
    }

    @PrimaryKey @NonNull
    public String name;

    @ColumnInfo
    public double price;

    @Ignore
    public DatabaseOperations databaseOperations;
}