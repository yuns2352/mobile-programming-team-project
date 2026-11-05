package com.example.projectandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        // [초기 세팅] 앱 최초 실행 시 CSV 데이터를 DB로 임포트
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean imported = prefs.getBoolean("csv_imported", false);

        if (!imported) {
            CSVImporter.importCSV(this, db, "food_master.csv", "food_master");
            CSVImporter.importCSV(this, db, "activity_master.csv", "activity_master");
            prefs.edit().putBoolean("csv_imported", true).apply();
        }

        // 버튼 바인딩
        Button btnMeal = findViewById(R.id.btnMeal);
        Button btnWeight = findViewById(R.id.btnWeight);
        Button btnReport = findViewById(R.id.btnReport);
        Button btnReset = findViewById(R.id.btnReset);

        // 각 화면으로 이동
        btnMeal.setOnClickListener(v -> startActivity(new Intent(this, MealActivity.class)));
        btnWeight.setOnClickListener(v -> startActivity(new Intent(this, WeightActivity.class)));
        btnReport.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));

        // [데이터 리셋] 사용자의 기록(식단, 체중)만 삭제
        btnReset.setOnClickListener(v -> {
            db.execSQL("DELETE FROM meal_log");
            db.execSQL("DELETE FROM user_info");
            Toast.makeText(this, "사용자 데이터가 모두 초기화되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}