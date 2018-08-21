package com.vunke.auth.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhuxi on 2017/10/20.
 */
public class AuthHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "auth.db";
    private final static int DATABASE_VERSION = 1;
    private String  CreateTable= "create table auth (_id integer not null primary key autoincrement,user_id varchar,auth_code integer,error_code varchar,error_info varchar,create_time varchar)";
    private String  DropTable= "drop table if exists auth";

    public AuthHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DropTable);
        db.execSQL(CreateTable);
    }
}
