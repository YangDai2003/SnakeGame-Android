package com.yangdai.snakegame;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    //障碍物位置
    private final Set<SnakePoints> barriers = new HashSet<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;
    private SurfaceHolder surfaceHolder;
    //起始方向
    private String movingPosition = "right";
    //分数
    private int score = 0;
    // 绘制大小
    private int pointSize = 50;
    //起始长度
    private static final int defaultTalePoints = 3;
    //蛇的颜色
    private static final int snakeColor = Color.YELLOW;
    //移动速度
    private int snakeMovingSpeed = 800;
    //食物位置
    private int positionX, positionY;
    //障碍物数量
    private int barrierNum = 8;
    private Timer timer;
    private Canvas canvas = null;
    //蛇和食物颜色
    private Paint pointColor = null;
    private GestureDetector gestureDetector;
    private boolean isPaused = false, gameOver = false, started = false;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    SharedPreferences sharedPreferences, sharedPreferences1;
    private int sound;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            if (started && !isPaused) pauseGame();
            Intent intent = new Intent(this, SettingsActivity.class);
            intentActivityResultLauncher.launch(intent);
        } else if (item.getItemId() == R.id.info) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.Info))
                    .setMessage("")
                    .setPositiveButton(getString(R.string.confirm), null)
                    .setCancelable(true)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!started) init();
                else if (isPaused) resumeGame();
                else pauseGame();
                return true;
            }

            @Override
            public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX != 0) {
                    if (deltaX > 0) {
                        if (!movingPosition.equals("left")) {
                            movingPosition = "right";
                        }
                    } else {
                        if (!movingPosition.equals("right")) {
                            movingPosition = "left";
                        }
                    }
                } else if (Math.abs(deltaY) > Math.abs(deltaX) && deltaY != 0) {
                    if (deltaY > 0) {
                        if (!movingPosition.equals("top")) {
                            movingPosition = "bottom";
                        }
                    } else {
                        if (!movingPosition.equals("bottom")) {
                            movingPosition = "top";
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    private void pauseGame() {
        isPaused = true;
        MusicServer.stop();
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        showDialog();
    }

    private void resumeGame() {
        isPaused = false;
        if (sound == 0) MusicServer.play(this, R.raw.background);
        moveSnake();
    }

    private void showDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.pause))
                .setMessage(getString(R.string.pauseContent))
                .setPositiveButton(getString(R.string.continueGame), (dialog, which) -> resumeGame())
                .setNeutralButton(getString(R.string.cancel), null)
                .setNegativeButton(getString(R.string.restart), (dialog, which) -> {
                    if (timer != null) {
                        timer.purge();
                        timer.cancel();
                    }
                    init();
                })
                .setCancelable(false)
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        sharedPreferences1 = getSharedPreferences("score", MODE_PRIVATE);


        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                if (res.getData() != null) {
                    int difficulty = res.getData().getIntExtra("difficulty", 0);
                    if (difficulty == 1) barrierNum = 5;
                    else if (difficulty == 2) barrierNum = 10;
                    else barrierNum = 0;
                    int size = res.getData().getIntExtra("size", 1);
                    if (size == 0) pointSize = 60;
                    else if (size == 2) pointSize = 40;
                    else pointSize = 50;
                    int speed = res.getData().getIntExtra("speed", 1);
                    if (speed == 0) snakeMovingSpeed = 750;
                    else if (speed == 1) snakeMovingSpeed = 850;
                    else snakeMovingSpeed = 950;
                    sound = res.getData().getIntExtra("sound", 0);
                    if (sound == 0) MusicServer.play(this, R.raw.background);
                    else MusicServer.stop();
                }
            }
        });

        int difficulty = sharedPreferences.getInt("difficulty", 0);
        int size = sharedPreferences.getInt("size", 1);
        int speed = sharedPreferences.getInt("speed", 1);
        sound = sharedPreferences.getInt("sound", 0);
        if (sound == 0) MusicServer.play(this, R.raw.background);
        else MusicServer.stop();
        if (difficulty == 1) barrierNum = 5;
        else if (difficulty == 2) barrierNum = 10;
        else barrierNum = 0;
        if (size == 0) pointSize = 60;
        else if (size == 2) pointSize = 40;
        else pointSize = 50;
        if (speed == 0) snakeMovingSpeed = 750;
        else if (speed == 1) snakeMovingSpeed = 850;
        else snakeMovingSpeed = 950;

        initGestureDetector();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                pauseGame();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);
        surfaceView.getHolder().addCallback(this);

        surfaceView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused && !gameOver && started) pauseGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isPaused && !gameOver && started) pauseGame();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(getColor(R.color.green));
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void init() {
        gameOver = false;
        started = true;
        isPaused = false;

        if (sound == 0) MusicServer.play(this, R.raw.background);
        snakePointsList.clear();

        scoreTV.setText("0");
        score = 0;
        movingPosition = "right";

        int startPositionX = (pointSize) * defaultTalePoints;

        for (int i = 0; i < defaultTalePoints; i++) {
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);
            startPositionX = startPositionX - (pointSize * 2);
        }

        addBarriers();
        addPoints();
        moveSnake();
    }

    private void addBarriers() {
        barriers.clear();
        for (int i = 0; i < barrierNum; ++i) {
            //获取区域大小
            int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
            int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

            int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
            int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

            //确保偶数
            if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
            if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;

            boolean notContained = barriers.add(new SnakePoints((pointSize * randomXPosition) + pointSize
                    , (pointSize * randomYPosition) + pointSize));
            if (!notContained) --i;
        }
    }

    private void addPoints() {
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);
        if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
        if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;

        //确保食物不在蛇的身体上//确保食物不在墙上
        while (snakePointsList.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                (pointSize * randomYPosition) + pointSize)) ||
                barriers.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                        (pointSize * randomYPosition) + pointSize))) {
            randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
            randomYPosition = new Random().nextInt(surfaceHeight / pointSize);
            if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
            if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;
        }

        positionX = (pointSize * randomXPosition) + pointSize;
        positionY = (pointSize * randomYPosition) + pointSize;
    }

    private void moveSnake() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //头部
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                //检查是否吃到食物
                if (headPositionX == positionX && positionY == headPositionY) {
                    if (sound != 2) MusicServer.playOneTime(MainActivity.this, R.raw.eat);
                    //长大
                    growSnake();
                    //增加食物
                    addPoints();
                }

                // 改变移动方向
                switch (movingPosition) {
                    case "right":
                        snakePointsList.get(0).setPositionX(headPositionX + (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "left":
                        snakePointsList.get(0).setPositionX(headPositionX - (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "top":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY - (pointSize * 2));
                        break;
                    case "bottom":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY + (pointSize * 2));
                        break;
                }

                //判断结束条件
                if (checkGameOver(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY())) {
                    timer.purge();
                    timer.cancel();

                    MusicServer.stop();
                    if (sound != 2) MusicServer.playOneTime(MainActivity.this, R.raw.dead);
                    started = false;
                    isPaused = true;
                    if (score > sharedPreferences1.getInt("highest", 0)) {
                        SharedPreferences.Editor editor = sharedPreferences1.edit();
                        editor.putInt("highest", score);
                        editor.apply();
                    }
                    runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                            .setMessage(getString(R.string.score) + score + "\n" + "最高纪录: " + sharedPreferences1.getInt("highest", 0))
                            .setTitle(getString(R.string.end))
                            .setCancelable(false)
                            .setNeutralButton(getString(R.string.cancel), null)
                            .setPositiveButton(getString(R.string.restart), (dialogInterface, i) -> init())
                            .show());
                } else {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        //清除画布
                        canvas.drawColor(getColor(R.color.green));
                        //画蛇头位置
                        canvas.drawCircle(snakePointsList.get(0).getPositionX(),
                                snakePointsList.get(0).getPositionY(), pointSize, createPointColor());
                        //画随机食物
                        canvas.drawCircle(positionX, positionY, pointSize, createPointColor());
                        //画墙
                        Iterator<SnakePoints> iterator = barriers.iterator();
                        int x, y;
                        while (iterator.hasNext()) {
                            SnakePoints temp = iterator.next();
                            x = temp.getPositionX();
                            y = temp.getPositionY();
                            drawBarrier(canvas, x, y);
                        }

                        //画蛇身
                        for (int i = 1; i < snakePointsList.size(); i++) {
                            int getTempPositionX = snakePointsList.get(i).getPositionX();
                            int getTempPositionY = snakePointsList.get(i).getPositionY();
                            //依次向前移动一格
                            snakePointsList.get(i).setPositionX(headPositionX);
                            snakePointsList.get(i).setPositionY(headPositionY);
                            canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointSize, createPointColor());

                            headPositionX = getTempPositionX;
                            headPositionY = getTempPositionY;

                        }
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growSnake() {
        SnakePoints snakePoints = new SnakePoints(0, 0);
        // 加长一格
        snakePointsList.add(snakePoints);
        score++;
        //加分
        runOnUiThread(() -> scoreTV.setText(String.valueOf(score)));
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        // 检查是否撞到边缘
        if (headPositionX < 0 || headPositionY < 0
                || headPositionX >= surfaceView.getWidth()
                || headPositionY >= surfaceView.getHeight()) {
            return gameOver = true;
        } else {
            // 检查是否撞到身体
            SnakePoints snakePoints = new SnakePoints(headPositionX, headPositionY);
            for (int i = 1; i < snakePointsList.size(); i++) {
                if (snakePointsList.get(i).equals(snakePoints)) {
                    return gameOver = true;
                }
            }

            // 检查是否撞到障碍物
            Iterator<SnakePoints> iterator = barriers.iterator();
            int x, y;
            while (iterator.hasNext()) {
                SnakePoints temp = iterator.next();
                x = temp.getPositionX();
                y = temp.getPositionY();
                if (x == headPositionX && y == headPositionY) {
                    return gameOver = true;
                }
            }
        }
        return gameOver = false;
    }

    private Paint createPointColor() {
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true);
        }
        return pointColor;
    }

    private void drawBarrier(Canvas canvas, int x, int y) {
        Paint paint = new Paint();
        paint.setColor(getColor(R.color.brown));
        Rect rect = new Rect(x - pointSize, y - pointSize, x + pointSize, y + pointSize);
        canvas.drawRect(rect, paint);
    }
}