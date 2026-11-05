package com.example.projectandroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    DBHelper helper;
    SQLiteDatabase db;

    // 체중 구간 데이터 (CSV 기준)
    private final double[] BRACKETS = {48, 59, 70, 81, 92};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        helper = new DBHelper(this);
        db = helper.getReadableDatabase();

        // 1. XML의 ID들과 연결
        LinearLayout layoutStep1 = findViewById(R.id.layoutStep1);
        LinearLayout layoutStep2 = findViewById(R.id.layoutStep2);

        TextView txtFoodLog = findViewById(R.id.txtFoodLog);
        TextView txtTotalKcal = findViewById(R.id.txtTotalKcal);

        TextView txtName = findViewById(R.id.txtSelectedName);
        TextView txtResult = findViewById(R.id.txtSelectedResult);
        ImageView imgSelected = findViewById(R.id.imgSelected);

        // [새로 추가된 변수 연결]
        EditText edtTime = findViewById(R.id.edtTimeInput);
        Button btnCalc = findViewById(R.id.btnDoCalc);

        Button btnNextStep = findViewById(R.id.btnNextStep);
        Button btnClose = findViewById(R.id.btnClose);

        // 오늘 날짜
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // [STEP 1] 식단 데이터 로드
        double totalKcal = loadDietData(txtFoodLog, today);
        txtTotalKcal.setText("오늘 총 섭취: " + (int)totalKcal + " kcal");

        // 체중 가져오기
        double weight = getUserWeight();

        // [다음 버튼] 클릭 -> 2번 화면으로 이동
        btnNextStep.setOnClickListener(v -> {
            if (weight <= 0) {
                Toast.makeText(this, "체중 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);

            // 초기 멘트 설정
            txtName.setText("");
            txtResult.setText("운동할 시간을 입력하고 [결과 보기]를 누르세요.");
        });

        // [결과 보기 버튼] 클릭 -> 시간 입력받아서 계산
        btnCalc.setOnClickListener(v -> {
            String timeStr = edtTime.getText().toString().trim();
            if(timeStr.isEmpty()) {
                Toast.makeText(this, "시간(분)을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double inputMinutes = Double.parseDouble(timeStr);
                if (inputMinutes <= 0) {
                    Toast.makeText(this, "0보다 큰 시간을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 계산 시작
                recommendExercise(weight, totalKcal, inputMinutes, txtName, txtResult, imgSelected);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // [종료 버튼]
        btnClose.setOnClickListener(v -> finish());
    }

    // 식단 로드 (DB)
    private double loadDietData(TextView txtView, String date) {
        Cursor c = db.rawQuery("SELECT meal_type, food_name, amount_g, calorie FROM meal_log WHERE date = ? ORDER BY id ASC", new String[]{date});
        StringBuilder sb = new StringBuilder();
        double total = 0;

        if (c.getCount() == 0) {
            sb.append("오늘 기록된 식단이 없습니다.");
        } else {
            while (c.moveToNext()) {
                String type = c.getString(0);
                String food = c.getString(1);
                double amt = c.getDouble(2);
                double cal = c.getDouble(3);
                total += cal;
                sb.append(String.format(Locale.getDefault(), "[%s] %s (%.0fg) : %.0f kcal\n", type, food, amt, cal));
            }
        }
        c.close();
        txtView.setText(sb.toString());
        return total;
    }

    // 체중 로드 (DB)
    private double getUserWeight() {
        Cursor wc = db.rawQuery("SELECT weight FROM user_info LIMIT 1", null);
        double w = 0;
        if (wc.moveToFirst() && !wc.isNull(0)) w = wc.getDouble(0);
        wc.close();
        return w;
    }

    // [핵심] 운동 추천 로직 (시간 고려)
    private void recommendExercise(double weight, double totalKcal, double minutes, TextView txtName, TextView txtResult, ImageView imgView) {
        ArrayList<ActivityRow> rows = new ArrayList<>();

        // DB에서 운동 데이터 가져오기
        Cursor ac = db.rawQuery("SELECT activity, intensity, kg48, kg59, kg70, kg81, kg92 FROM activity_master", null);
        while (ac.moveToNext()) {
            ActivityRow r = new ActivityRow();
            r.activity = ac.getString(0);
            r.intensity = ac.getString(1);
            r.k48 = ac.getDouble(2);
            r.k59 = ac.getDouble(3);
            r.k70 = ac.getDouble(4);
            r.k81 = ac.getDouble(5);
            r.k92 = ac.getDouble(6);
            rows.add(r);
        }
        ac.close();

        if (rows.isEmpty()) return;

        // 내 체중 구간 찾기
        double bracket = getWeightBracket(weight);

        // 목표: 입력한 시간 안에 칼로리를 다 태우려면 '시간당 소모량'이 얼마여야 하는가?
        // 공식: (총 섭취량) * (60분 / 입력시간)
        double targetPerHour = totalKcal * (60.0 / minutes);

        ActivityRow best = null;
        double minDiff = Double.MAX_VALUE;

        for (ActivityRow r : rows) {
            double dbKcalPerHour = r.getKcalForBracket(bracket);
            if (dbKcalPerHour <= 0) continue;

            // 목표치와 가장 비슷한 운동 찾기
            double diff = Math.abs(targetPerHour - dbKcalPerHour);

            if (diff < minDiff) {
                minDiff = diff;
                best = r;
            }
        }

        if (best != null) {
            String title = best.activity + (best.intensity != null ? " (" + best.intensity + ")" : "");
            txtName.setText(title);

            txtResult.setText(String.format(Locale.getDefault(),
                    "섭취한 %.0f kcal를 %.0f분 안에 소모하려면\n\n'%s' 강도로 운동하는 것이 가장 효과적입니다.",
                    totalKcal, minutes, title));

            Bitmap bmp = loadImageAssets(best.activity);
            if (bmp != null) imgView.setImageBitmap(bmp);
        } else {
            txtResult.setText("적절한 운동을 찾지 못했습니다.");
        }
    }

    private double getWeightBracket(double w) {
        for (double b : BRACKETS) {
            if (w <= b) return b;
        }
        return BRACKETS[BRACKETS.length - 1];
    }

    private Bitmap loadImageAssets(String activityName) {
        String filename = null;
        String s = activityName.toLowerCase();
        if (s.contains("run") || s.contains("달리")) filename = "running.png";
        else if (s.contains("walk") || s.contains("걷")) filename = "walking.png";

        if (filename == null) return null;
        try {
            InputStream is = getAssets().open(filename);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            return null;
        }
    }

    // 데이터 구조체 (이게 꼭 있어야 함)
    static class ActivityRow {
        String activity, intensity;
        double k48, k59, k70, k81, k92;

        double getKcalForBracket(double bracket) {
            if (bracket == 48) return k48;
            if (bracket == 59) return k59;
            if (bracket == 70) return k70;
            if (bracket == 81) return k81;
            if (bracket == 92) return k92;
            return 0;
        }
    }
}