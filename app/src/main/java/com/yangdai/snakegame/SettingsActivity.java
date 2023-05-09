package com.yangdai.snakegame;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;

public class SettingsActivity extends AppCompatActivity {
    MaterialButtonToggleGroup materialButtonToggleGroup1, materialButtonToggleGroup2,
            materialButtonToggleGroup3, materialButtonToggleGroup4;
    private int difficulty, size, speed, sound;
    SharedPreferences sharedPreferences;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_settiings);
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        materialButtonToggleGroup1 = findViewById(R.id.difficulty);
        materialButtonToggleGroup2 = findViewById(R.id.size);
        materialButtonToggleGroup3 = findViewById(R.id.speed);
        materialButtonToggleGroup4 = findViewById(R.id.sound);

        difficulty = sharedPreferences.getInt("difficulty", 0);
        size = sharedPreferences.getInt("size", 1);
        speed = sharedPreferences.getInt("speed", 1);
        sound = sharedPreferences.getInt("sound", 0);

        if (difficulty == 0) materialButtonToggleGroup1.check(R.id.easy);
        else if (difficulty == 1) materialButtonToggleGroup1.check(R.id.medium);
        else materialButtonToggleGroup1.check(R.id.hard);
        if (size == 0) materialButtonToggleGroup2.check(R.id.tiny);
        else if (size == 1) materialButtonToggleGroup2.check(R.id.normal);
        else materialButtonToggleGroup2.check(R.id.wide);
        if (speed == 0) materialButtonToggleGroup3.check(R.id.slow);
        else if (speed == 1) materialButtonToggleGroup3.check(R.id.normalSpeed);
        else materialButtonToggleGroup3.check(R.id.qiuck);
        if (sound == 0) materialButtonToggleGroup4.check(R.id.all);
        else if (sound == 1) materialButtonToggleGroup4.check(R.id.only);
        else materialButtonToggleGroup4.check(R.id.none);

        materialButtonToggleGroup1.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.easy) difficulty = 0;
                else if (checkedId == R.id.medium) difficulty = 1;
                else difficulty = 2;
            }
        });
        materialButtonToggleGroup2.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.tiny) size = 0;
                else if (checkedId == R.id.normal) size = 1;
                else size = 2;
            }
        });
        materialButtonToggleGroup3.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.slow) speed = 0;
                else if (checkedId == R.id.normalSpeed) speed = 1;
                else speed = 2;
            }
        });
        materialButtonToggleGroup4.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.all) sound = 0;
                else if (checkedId == R.id.only) sound = 1;
                else sound = 2;
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                send();
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            send();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void send() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("difficulty", difficulty);
        editor.putInt("size", size);
        editor.putInt("speed", speed);
        editor.putInt("sound", sound);
        editor.apply();
        Intent intent = new Intent();
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("size", size);
        intent.putExtra("speed", speed);
        intent.putExtra("sound", sound);
        setResult(RESULT_OK, intent);
    }
}