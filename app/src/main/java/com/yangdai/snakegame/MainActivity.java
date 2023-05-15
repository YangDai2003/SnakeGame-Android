package com.yangdai.snakegame;

import static com.yangdai.snakegame.Dpad.DOWN;
import static com.yangdai.snakegame.Dpad.LEFT;
import static com.yangdai.snakegame.Dpad.RIGHT;
import static com.yangdai.snakegame.Dpad.UP;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final List<SnakePoints> snakePointsList = new ArrayList<>(), aiPointsList = new ArrayList<>();
    //障碍物位置
    private final Set<SnakePoints> barriers = new HashSet<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;
    private SurfaceHolder surfaceHolder;
    //起始方向
    private String movingDirection = "right", aiMovingDirection = "left";
    //分数
    private static int score = 0, scoreAi = 0;
    // 绘制大小
    private static int pointSize = 50;
    //起始长度
    private static final int defaultLength = 3;
    //蛇的颜色
    private static final int snakeColor = Color.YELLOW, aiColor = Color.BLUE, specialFood = Color.RED;
    //移动速度
    private static int snakeMovingSpeed = 800;
    //食物位置
    private static int positionX, positionY;
    private static int tempX = -1, tempY = -1;
    //障碍物数量
    private static int barrierNum = 8;
    private Timer timer;
    private Canvas canvas = null;
    //蛇和食物颜色
    private Paint pointColor = null, pointAi = null, food = null;
    private GestureDetector gestureDetector;
    private boolean isPaused = false, gameOver = false, started = false;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    SharedPreferences sharedPreferences, sharedPreferences1;
    private static int sound = -1;
    private boolean bonus = false;
    private final Random random = new Random();
    private SnakeAi2 ai;
    private static int mode = 0;
    private final Dpad dpad = new Dpad();
    private ImageView imageView;
    private MaterialTextView textView;

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        // Check if this event if from a D-pad and process accordingly.
        if (Dpad.isDpadDevice(event)) {
            int press = dpad.getDirectionPressed(event);
            switch (press) {
                case LEFT:
                    if (!movingDirection.equals("right")) {
                        movingDirection = "left";
                    }
                    return true;
                case RIGHT:
                    if (!movingDirection.equals("left")) {
                        movingDirection = "right";
                    }
                    return true;
                case UP:
                    if (!movingDirection.equals("down")) {
                        movingDirection = "up";
                    }
                    return true;
                case DOWN:
                    if (!movingDirection.equals("up")) {
                        movingDirection = "down";
                    }
                    return true;
            }
        }
        return super.onGenericMotionEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            if (started) {
                isPaused = true;
                started = false;
                MusicServer.stop();
                if (timer != null) {
                    timer.purge();
                    timer.cancel();
                }
            }
            Intent intent = new Intent(this, SettingsActivity.class);
            intentActivityResultLauncher.launch(intent);
        } else if (item.getItemId() == R.id.info) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.Info))
                    .setMessage(getString(R.string.content))
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
                if (!started) start();
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
                        if (!movingDirection.equals("left")) {
                            movingDirection = "right";
                        }
                    } else {
                        if (!movingDirection.equals("right")) {
                            movingDirection = "left";
                        }
                    }
                } else if (Math.abs(deltaY) > Math.abs(deltaX) && deltaY != 0) {
                    if (deltaY > 0) {
                        if (!movingDirection.equals("up")) {
                            movingDirection = "down";
                        }
                    } else {
                        if (!movingDirection.equals("down")) {
                            movingDirection = "up";
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
        showPauseDialog();
    }

    private void resumeGame() {
        isPaused = false;
        if (sound == 0) MusicServer.play(this, R.raw.background);
        moveSnake();
    }

    private void showPauseDialog() {
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
                    start();
                })
                .setCancelable(false)
                .show();
    }

    private void getSettings() {
        barrierNum = sharedPreferences.getInt("difficulty", 0);
        int size = sharedPreferences.getInt("size", 1);
        int speed = sharedPreferences.getInt("speed", 1);
        mode = sharedPreferences.getInt("mode", 0);
        sound = sharedPreferences.getInt("sound", 0);
        if (size == 0) pointSize = 72;
        else if (size == 2) pointSize = 45;
        else pointSize = 54;
        if (speed == 0) snakeMovingSpeed = 750;
        else if (speed == 1) snakeMovingSpeed = 850;
        else snakeMovingSpeed = 900;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.start);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        sharedPreferences1 = getSharedPreferences("score", MODE_PRIVATE);

        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                getSettings();
                init();
            }
        });

        getSettings();

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

        init();
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

    @SuppressLint("SetTextI18n")
    private void init() {
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);

        if (sound == 0) MusicServer.play(this, R.raw.background);

        snakePointsList.clear();
        aiPointsList.clear();

        if (mode == 0) scoreTV.setText(getString(R.string.you) + "0");
        else {
            scoreTV.setText(getString(R.string.you) + "0" + " " + getString(R.string.ai) + "0");
            ai = new SnakeAi2(surfaceView, barriers, pointSize);
        }

        score = 0;
        scoreAi = 0;

        movingDirection = "right";
        int startPositionX = pointSize * defaultLength;

        for (int i = 0; i < defaultLength; i++) {
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);
            startPositionX = startPositionX - (pointSize * 2);
        }
        if (mode == 1) {
            aiMovingDirection = "right";
            int aiStartPositionX = (pointSize) * defaultLength;
            int aiStartPositionY = (surfaceView.getHeight() - (pointSize * 2)) / pointSize;
            if (aiStartPositionY % 2 != 0) aiStartPositionY += 1;
            aiStartPositionY = aiStartPositionY * pointSize + pointSize;
            for (int i = 0; i < defaultLength; i++) {
                SnakePoints snakePoints = new SnakePoints(aiStartPositionX, aiStartPositionY);
                aiPointsList.add(snakePoints);
                aiStartPositionX = aiStartPositionX - (pointSize * 2);
            }
        }
    }

    private void start() {
        gameOver = false;
        started = true;
        isPaused = false;
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
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

            int randomXPosition = random.nextInt(surfaceWidth / pointSize);
            int randomYPosition = random.nextInt(surfaceHeight / pointSize);

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

        int randomXPosition = random.nextInt(surfaceWidth / pointSize);
        int randomYPosition = random.nextInt(surfaceHeight / pointSize);

        if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
        if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;

        if (mode == 0) {
            //确保食物不在蛇的身体上//确保食物不在墙上
            while (snakePointsList.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                    (pointSize * randomYPosition) + pointSize)) ||
                    barriers.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                            (pointSize * randomYPosition) + pointSize))) {
                randomXPosition = random.nextInt(surfaceWidth / pointSize);
                randomYPosition = random.nextInt(surfaceHeight / pointSize);
                if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
                if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;
            }
        }

        if (mode == 1) {
            while (snakePointsList.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                    (pointSize * randomYPosition) + pointSize))
                    || barriers.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                    (pointSize * randomYPosition) + pointSize))
                    || aiPointsList.contains(new SnakePoints((pointSize * randomXPosition) + pointSize,
                    (pointSize * randomYPosition) + pointSize))) {
                randomXPosition = random.nextInt(surfaceWidth / pointSize);
                randomYPosition = random.nextInt(surfaceHeight / pointSize);
                if ((randomXPosition % 2) != 0) randomXPosition = randomXPosition + 1;
                if ((randomYPosition % 2) != 0) randomYPosition = randomYPosition + 1;
            }
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
//                //检查是否吃到食物
//                if (headPositionX == positionX && positionY == headPositionY) {
//                    if (sound != 2) MusicServer.playOneTime(MainActivity.this, R.raw.eat);
//                    if (!bonus) growSnake();
//                    else shrinkSnake();
//                    //增加食物
//                    addPoints();
//                }
//                // 改变移动方向
//                switch (movingDirection) {
//                    case "right":
//                        if ((headPositionX + (pointSize * 2)) >= surfaceView.getWidth()) {
//                            snakePointsList.get(0).setPositionX(pointSize);
//                        } else {
//                            snakePointsList.get(0).setPositionX(headPositionX + (pointSize * 2));
//                        }
//                        snakePointsList.get(0).setPositionY(headPositionY);
//                        break;
//                    case "left":
//                        if ((headPositionX - (pointSize * 2)) < 0) {
//                            int max = (surfaceView.getWidth() - (pointSize * 2)) / pointSize;
//                            if (max % 2 != 0) max -= 1;
//                            max = pointSize * max + pointSize;
//                            snakePointsList.get(0).setPositionX(max);
//                        } else {
//                            snakePointsList.get(0).setPositionX(headPositionX - (pointSize * 2));
//                        }
//                        snakePointsList.get(0).setPositionY(headPositionY);
//                        break;
//                    case "up":
//                        snakePointsList.get(0).setPositionX(headPositionX);
//                        snakePointsList.get(0).setPositionY(headPositionY - (pointSize * 2));
//                        break;
//                    case "down":
//                        snakePointsList.get(0).setPositionX(headPositionX);
//                        snakePointsList.get(0).setPositionY(headPositionY + (pointSize * 2));
//                        break;
//                }

                int aiHeadPositionX = 0;
                int aiHeadPositionY = 0;
                if (mode == 1) {
                    aiHeadPositionX = aiPointsList.get(0).getPositionX();
                    aiHeadPositionY = aiPointsList.get(0).getPositionY();
                    if (aiHeadPositionX == positionX && positionY == aiHeadPositionY) {
                        if (sound != 2) MusicServer.playOneTime(MainActivity.this, R.raw.eat);
                        if (!bonus) growAi();
                        else shrinkAi();
                        //增加食物
                        addPoints();
                    }
                    aiMovingDirection = ai.moveAi(aiHeadPositionX, aiHeadPositionY, positionX, positionY, aiMovingDirection, aiPointsList);
                    switch (aiMovingDirection) {
                        case "right":
                            if ((aiHeadPositionX + (pointSize * 2)) >= surfaceView.getWidth()) {
                                aiPointsList.get(0).setPositionX(pointSize);
                            } else {
                                aiPointsList.get(0).setPositionX(aiHeadPositionX + (pointSize * 2));
                            }
                            aiPointsList.get(0).setPositionY(aiHeadPositionY);
                            break;
                        case "left":
                            if ((aiHeadPositionX - (pointSize * 2)) < 0) {
                                int max = (surfaceView.getWidth() - (pointSize * 2)) / pointSize;
                                if (max % 2 != 0) max -= 1;
                                max = pointSize * max + pointSize;
                                aiPointsList.get(0).setPositionX(max);
                            } else {
                                aiPointsList.get(0).setPositionX(aiHeadPositionX - (pointSize * 2));
                            }
                            aiPointsList.get(0).setPositionY(aiHeadPositionY);
                            break;
                        case "up":
                            aiPointsList.get(0).setPositionX(aiHeadPositionX);
                            aiPointsList.get(0).setPositionY(aiHeadPositionY - (pointSize * 2));
                            break;
                        case "down":
                            aiPointsList.get(0).setPositionX(aiHeadPositionX);
                            aiPointsList.get(0).setPositionY(aiHeadPositionY + (pointSize * 2));
                            break;
                    }

                }

                boolean gameOver;
                if (mode == 0)
                    gameOver = checkGameOver(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY());
                else
                    gameOver = checkPveGameOver(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY(), aiPointsList.get(0).getPositionX(), aiPointsList.get(0).getPositionY());
                //判断结束条件
                if (gameOver) {
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
                            .setMessage(scoreTV.getText() + "\n" + getString(R.string.highest) + sharedPreferences1.getInt("highest", 0))
                            .setTitle(getString(R.string.end))
                            .setCancelable(false)
                            .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                isPaused = true;
                                started = false;
                                init();
                            })
                            .setPositiveButton(getString(R.string.restart), (dialogInterface, i) -> {
                                init();
                                start();
                            })
                            .show());
                } else {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        //清除画布
                        canvas.drawColor(getColor(R.color.green));

                        if (mode == 1) canvas.drawCircle(aiPointsList.get(0).getPositionX(),
                                aiPointsList.get(0).getPositionY(), pointSize, createAiColor());
                        //画蛇头位置
                        canvas.drawCircle(snakePointsList.get(0).getPositionX(),
                                snakePointsList.get(0).getPositionY(), pointSize, createPointColor());

                        //画随机食物
                        canvas.drawCircle(positionX, positionY, pointSize, createFoodColor());
                        //画墙
                        Iterator<SnakePoints> iterator = barriers.iterator();
                        int x, y;
                        while (iterator.hasNext()) {
                            SnakePoints temp = iterator.next();
                            x = temp.getPositionX();
                            y = temp.getPositionY();
                            drawBarrier(canvas, x, y);
                        }
                        if (mode == 1) {
                            for (int i = 1; i < aiPointsList.size(); i++) {
                                int getTempPositionX = aiPointsList.get(i).getPositionX();
                                int getTempPositionY = aiPointsList.get(i).getPositionY();
                                //依次向前移动一格
                                aiPointsList.get(i).setPositionX(aiHeadPositionX);
                                aiPointsList.get(i).setPositionY(aiHeadPositionY);
                                canvas.drawCircle(aiPointsList.get(i).getPositionX(), aiPointsList.get(i).getPositionY(), pointSize, createAiColor());

                                aiHeadPositionX = getTempPositionX;
                                aiHeadPositionY = getTempPositionY;
                            }
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
        updateText();
    }

    private void growAi() {
        SnakePoints snakePoints = new SnakePoints(0, 0);
        // 加长一格
        aiPointsList.add(snakePoints);
        scoreAi++;
        updateText();
    }

    private void shrinkSnake() {
        // 缩短一格
        snakePointsList.remove(snakePointsList.size() - 1);
        // 加三分
        score += 3;
        //加分
        updateText();
    }

    private void shrinkAi() {
        // 缩短一格
        aiPointsList.remove(aiPointsList.size() - 1);
        scoreAi += 3;
        updateText();
    }

    @SuppressLint("SetTextI18n")
    private void updateText() {
        runOnUiThread(() -> {
            if (mode == 0) scoreTV.setText(getString(R.string.you) + score);
            else
                scoreTV.setText(getString(R.string.you) + score + " " + getString(R.string.ai) + scoreAi);
        });
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        // 检查是否撞到边缘
        if (headPositionY < 0 || headPositionY >= surfaceView.getHeight()) {
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

    private boolean checkPveGameOver(int headPositionX, int headPositionY, int aiHeadPositionX, int aiHeadPositionY) {
        // 检查是否撞到边缘
        if (headPositionY < 0 || headPositionY >= surfaceView.getHeight()
                || aiHeadPositionY < 0 || aiHeadPositionY >= surfaceView.getHeight()) {
            return gameOver = true;
        } else {
//            // 检查是否撞到身体
//            SnakePoints snakePoints = new SnakePoints(headPositionX, headPositionY);
//            for (int i = 1; i < snakePointsList.size(); i++) {
//                if (snakePointsList.get(i).equals(snakePoints)) {
//                    return gameOver = true;
//                }
//            }
            SnakePoints aiPoints = new SnakePoints(aiHeadPositionX, aiHeadPositionY);
            for (int i = 1; i < aiPointsList.size(); i++) {
                if (aiPointsList.get(i).equals(aiPoints)) {
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
                if ((x == headPositionX && y == headPositionY) || (x == aiHeadPositionX && y == aiHeadPositionY)) {
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

    private Paint createAiColor() {
        if (pointAi == null) {
            pointAi = new Paint();
            pointAi.setColor(aiColor);
            pointAi.setStyle(Paint.Style.FILL);
            pointAi.setAntiAlias(true);
        }
        return pointAi;
    }

    private Paint createFoodColor() {
        if (tempX != positionX || tempY != positionY) {
            food = new Paint();
            int red = random.nextInt(100);
            boolean show;
            if (mode == 0) {
                show = red < 90 || snakePointsList.size() <= defaultLength;
            } else
                show = red < 90 || snakePointsList.size() <= defaultLength || aiPointsList.size() <= defaultLength;
            if (show) {
                food.setColor(snakeColor);
                bonus = false;
            } else {
                food.setColor(specialFood);
                bonus = true;
            }
            food.setStyle(Paint.Style.FILL);
            food.setAntiAlias(true);
            tempX = positionX;
            tempY = positionY;
        }
        return food;
    }

    private void drawBarrier(Canvas canvas, int x, int y) {
        Paint paint = new Paint();
        paint.setColor(getColor(R.color.brown));
        Rect rect = new Rect(x - pointSize, y - pointSize, x + pointSize, y + pointSize);
        canvas.drawRect(rect, paint);
    }
}