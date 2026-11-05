package com.example.projectandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

// CSV 파일 데이터를 SQLite DB로 파싱하여 넣는 클래스
public class CSVImporter {

    private static final String TAG = "CSVImporter";

    public static void importCSV(Context ctx, SQLiteDatabase db, String fileName, String table) {
        BufferedReader br = null;
        try {
            InputStream is = ctx.getAssets().open(fileName);
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String header = br.readLine(); // 첫 줄(헤더) 건너뛰기
            if (header == null) return;

            db.beginTransaction(); // 트랜잭션 시작 (속도 향상)

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] t = line.split(",", -1);

                // 빈 값은 0으로 처리 및 공백 제거
                for (int i = 0; i < t.length; i++) {
                    if (t[i] == null || t[i].trim().isEmpty()) t[i] = "0";
                    else t[i] = t[i].trim();
                }

                if (table.equals("food_master")) {
                    if (t.length < 5) continue;
                    db.execSQL("INSERT INTO food_master(food_name, kcal, protein, fat, carb) VALUES(?,?,?,?,?)",
                            new Object[]{t[0], parseDoubleSafe(t[1]), parseDoubleSafe(t[2]), parseDoubleSafe(t[3]), parseDoubleSafe(t[4])});
                } else if (table.equals("activity_master")) {
                    if (t.length < 8) continue;
                    db.execSQL("INSERT INTO activity_master(activity, intensity, kg48, kg59, kg70, kg81, kg92, cpk) VALUES(?,?,?,?,?,?,?,?)",
                            new Object[]{t[0], t[1], parseDoubleSafe(t[2]), parseDoubleSafe(t[3]), parseDoubleSafe(t[4]), parseDoubleSafe(t[5]), parseDoubleSafe(t[6]), parseDoubleSafe(t[7])});
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "importCSV error", e);
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception ignored) { }
            try {
                db.endTransaction();
            } catch (Exception ignored) { }
        }
    }

    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }
}


