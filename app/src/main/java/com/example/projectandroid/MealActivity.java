package com.example.projectandroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MealActivity extends AppCompatActivity {

    DBHelper helper;
    SQLiteDatabase db;
    AutoCompleteTextView acBreakfast, acLunch, acDinner;
    EditText edtBreakfastAmount, edtLunchAmount, edtDinnerAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        // UI 요소 연결
        acBreakfast = findViewById(R.id.acBreakfastFood);
        acLunch = findViewById(R.id.acLunchFood);
        acDinner = findViewById(R.id.acDinnerFood);
        edtBreakfastAmount = findViewById(R.id.edtBreakfastAmount);
        edtLunchAmount = findViewById(R.id.edtLunchAmount);
        edtDinnerAmount = findViewById(R.id.edtDinnerAmount);
        Button btnSaveMeals = findViewById(R.id.btnSaveMeals);

        // 자동완성을 위한 음식 리스트 로드
        ArrayList<String> foods = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT food_name FROM food_master", null);
        while (c.moveToNext()) {
            foods.add(c.getString(0));
        }
        c.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foods);
        acBreakfast.setAdapter(adapter);
        acLunch.setAdapter(adapter);
        acDinner.setAdapter(adapter);

        // 저장 버튼 로직
        btnSaveMeals.setOnClickListener(v -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            saveSingleMeal("아침", acBreakfast.getText().toString(), edtBreakfastAmount.getText().toString(), today);
            saveSingleMeal("점심", acLunch.getText().toString(), edtLunchAmount.getText().toString(), today);
            saveSingleMeal("저녁", acDinner.getText().toString(), edtDinnerAmount.getText().toString(), today);

            Toast.makeText(this, "식단이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 개별 식단 저장 헬퍼 함수
    private void saveSingleMeal(String mealType, String foodName, String amountStr, String date) {
        foodName = foodName.trim();
        amountStr = amountStr.trim();
        if (foodName.isEmpty() || amountStr.isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            // DB에서 해당 음식의 100g당 칼로리 조회
            Cursor kc = db.rawQuery("SELECT kcal FROM food_master WHERE food_name = ?", new String[]{foodName});
            if (kc.moveToFirst()) {
                double kcalPer100 = kc.getDouble(0);
                double totalKcal = kcalPer100 * (amount / 100.0); // 섭취량에 비례한 칼로리 계산

                db.execSQL("INSERT INTO meal_log(date, meal_type, food_name, amount_g, calorie) VALUES(?,?,?,?,?)",
                        new Object[]{date, mealType, foodName, amount, totalKcal});
            }
            kc.close();
        } catch (NumberFormatException ignored) { }
    }
}