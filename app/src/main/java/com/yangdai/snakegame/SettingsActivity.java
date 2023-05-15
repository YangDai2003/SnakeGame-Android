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
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {
    private static final String SETTINGS_KEY = "settings";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String SIZE_KEY = "size";
    private static final String SPEED_KEY = "speed";
    private static final String SOUND_KEY = "sound";
    private static final String MODE_KEY = "mode";
    private int difficulty;
    private int size;
    private int speed;
    private int sound;
    private int mode;
    private SharedPreferences sharedPreferences;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);

        MaterialButtonToggleGroup sizeGroup = findViewById(R.id.size);
        MaterialButtonToggleGroup speedGroup = findViewById(R.id.speed);
        MaterialButtonToggleGroup soundGroup = findViewById(R.id.sound);
        MaterialButtonToggleGroup modeGroup = findViewById(R.id.mode);
        Slider slider = findViewById(R.id.slider);

        difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 0);
        if (difficulty % 2 != 0) difficulty -= 1;
        size = sharedPreferences.getInt(SIZE_KEY, 1);
        speed = sharedPreferences.getInt(SPEED_KEY, 1);
        sound = sharedPreferences.getInt(SOUND_KEY, 0);
        mode = sharedPreferences.getInt(MODE_KEY, 0);

        slider.setValue(difficulty);
        if (size == 0) sizeGroup.check(R.id.tiny);
        else if (size == 1) sizeGroup.check(R.id.normal);
        else sizeGroup.check(R.id.wide);
        if (speed == 0) speedGroup.check(R.id.slow);
        else if (speed == 1) speedGroup.check(R.id.normalSpeed);
        else speedGroup.check(R.id.quick);
        if (sound == 0) soundGroup.check(R.id.all);
        else if (sound == 1) soundGroup.check(R.id.only);
        else soundGroup.check(R.id.none);
        if (mode == 0) modeGroup.check(R.id.single);
        else modeGroup.check(R.id.pve);

        slider.addOnChangeListener((slider1, value, fromUser) -> difficulty = (int) value);
        sizeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.tiny) size = 0;
                else if (checkedId == R.id.normal) size = 1;
                else size = 2;
            }
        });
        speedGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.slow) speed = 0;
                else if (checkedId == R.id.normalSpeed) speed = 1;
                else speed = 2;
            }
        });
        soundGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.all) sound = 0;
                else if (checkedId == R.id.only) sound = 1;
                else sound = 2;
            }
        });
        modeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.single) mode = 0;
                else mode = 1;
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
        editor.putInt(DIFFICULTY_KEY, difficulty);
        editor.putInt(SIZE_KEY, size);
        editor.putInt(SPEED_KEY, speed);
        editor.putInt(SOUND_KEY, sound);
        editor.putInt(MODE_KEY, mode);
        editor.apply();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }
}