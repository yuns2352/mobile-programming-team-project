package com.example.projectandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "fitapp.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. 식품 데이터 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS food_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "food_name TEXT," +
                "kcal REAL," +
                "protein REAL," +
                "fat REAL," +
                "carb REAL" +
                ");");

        // 2. 운동 데이터 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS activity_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "activity TEXT," +
                "intensity TEXT," +
                "kg48 REAL," +
                "kg59 REAL," +
                "kg70 REAL," +
                "kg81 REAL," +
                "kg92 REAL," +
                "cpk REAL" +
                ");");

        // 3. 사용자 식단 기록 테이블 (리셋 대상)
        db.execSQL("CREATE TABLE IF NOT EXISTS meal_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "meal_type TEXT," +
                "food_name TEXT," +
                "amount_g REAL," +
                "calorie REAL" +
                ");");

        // 4. 사용자 정보(체중) 테이블 (리셋 대상)
        db.execSQL("CREATE TABLE IF NOT EXISTS user_info (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "weight REAL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS food_master");
        db.execSQL("DROP TABLE IF EXISTS activity_master");
        db.execSQL("DROP TABLE IF EXISTS meal_log");
        db.execSQL("DROP TABLE IF EXISTS user_info");
        onCreate(db);
    }
}