package com.example.projectandroid;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class WeightActivity extends AppCompatActivity {

    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        EditText edtWeight = findViewById(R.id.edtWeight);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String s = edtWeight.getText().toString().trim();
            if (s.isEmpty()) {
                Toast.makeText(this, "체중을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double w = Double.parseDouble(s);
                // 항상 최신 체중 1개만 유지 (기존 삭제 -> 신규 등록)
                db.execSQL("DELETE FROM user_info");
                db.execSQL("INSERT INTO user_info(weight) VALUES(?)", new Object[]{w});

                Toast.makeText(this, "체중(" + w + "kg)이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "올바른 숫자를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}