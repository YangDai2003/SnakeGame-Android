package com.yangdai.snakegame;

import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SnakeAi {
    private final SurfaceView surfaceView;
    private final Set<SnakePoints> barriers;
    private final int pointSize;

    public SnakeAi(SurfaceView surfaceView, Set<SnakePoints> barriers, int pointSize) {
        this.surfaceView = surfaceView;
        this.barriers = barriers;
        this.pointSize = pointSize;
    }

    public String moveAi(int headPositionX, int headPositionY, int positionX, int positionY, String movingDirection, List<SnakePoints> aiPointsList) {
        // 判断蛇头和食物的相对位置，确定下一步移动的方向
        return getNextMovingDirection(headPositionX, headPositionY, positionX, positionY, movingDirection, aiPointsList);
    }

    private boolean canMoveTo(int x, int y, List<SnakePoints> aiPointsList) {
        if (x < 0 || y < 0 || x >= surfaceView.getWidth() || y >= surfaceView.getHeight()) {
            return false;
        }
        return !barriers.contains(new SnakePoints(x, y)) && !aiPointsList.contains(new SnakePoints(x, y));
    }

    private List<String> getPossibleDirections(int headPositionX, int headPositionY, List<SnakePoints> aiPointsList) {
        List<String> possibleMove = new ArrayList<>();
        List<SnakePoints> checkList = new ArrayList<>();
        int checkMoveCount = 4;
        boolean willCrash = false;
        for (int i = 1; i < checkMoveCount; i++) {
            checkList.add(new SnakePoints(headPositionX, headPositionY - i * (pointSize * 2)));
        }
        for (SnakePoints snakePoints : checkList) {
            if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                willCrash = true;
                break;
            }
        }
        if (willCrash) willCrash = false;
        else possibleMove.add("up");
        checkList.clear();
        for (int i = 1; i < checkMoveCount; i++) {
            checkList.add(new SnakePoints(headPositionX, headPositionY + i * (pointSize * 2)));
        }
        for (SnakePoints snakePoints : checkList) {
            if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                willCrash = true;
                break;
            }
        }
        if (willCrash) willCrash = false;
        else possibleMove.add("down");
        checkList.clear();
        for (int i = 1; i < checkMoveCount; i++) {
            checkList.add(new SnakePoints(headPositionX - i * (pointSize * 2), headPositionY));
        }
        for (SnakePoints snakePoints : checkList) {
            if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                willCrash = true;
                break;
            }
        }
        if (willCrash) willCrash = false;
        else possibleMove.add("left");
        checkList.clear();
        for (int i = 1; i < checkMoveCount; i++) {
            checkList.add(new SnakePoints(headPositionX + i * (pointSize * 2), headPositionY));
        }
        for (SnakePoints snakePoints : checkList) {
            if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                willCrash = true;
                break;
            }
        }
        if (!willCrash) possibleMove.add("right");
        return possibleMove;
    }

    private String getBestDirection(int headPositionX, int headPositionY, List<String> directions, List<SnakePoints> aiPointsList) {
        int bestCount = 0;
        String bestMove = directions.get(0);

        List<SnakePoints> checkList = new ArrayList<>();
        for (String str : directions) {
            int temp = 0;
            switch (str) {
                case "up":
                    for (int i = 1; ; i++) {
                        checkList.add(new SnakePoints(headPositionX, headPositionY - i * (pointSize * 2)));
                        if ((headPositionY - i * (pointSize * 2)) < 0) break;
                    }
                    for (SnakePoints snakePoints : checkList) {
                        if (!aiPointsList.contains(snakePoints) && !barriers.contains(snakePoints)) {
                            temp++;
                        } else break;
                    }
                    if (temp > bestCount) {
                        bestCount = temp;
                        bestMove = "up";
                    }
                    checkList.clear();
                    break;
                case "down":
                    for (int i = 1; ; i++) {
                        checkList.add(new SnakePoints(headPositionX, headPositionY + i * (pointSize * 2)));
                        if ((headPositionY + i * (pointSize * 2)) >= surfaceView.getHeight())
                            break;
                    }
                    for (SnakePoints snakePoints : checkList) {
                        if (!aiPointsList.contains(snakePoints) && !barriers.contains(snakePoints)) {
                            temp++;
                        } else break;
                    }
                    if (temp > bestCount) {
                        bestCount = temp;
                        bestMove = "down";
                    }
                    checkList.clear();
                    break;
                case "left":
                    for (int i = 1; ; i++) {
                        checkList.add(new SnakePoints(headPositionX - i * (pointSize * 2), headPositionY));
                        if ((headPositionX - i * (pointSize * 2)) < 0) break;
                    }
                    for (SnakePoints snakePoints : checkList) {
                        if (!aiPointsList.contains(snakePoints) && !barriers.contains(snakePoints)) {
                            temp++;
                        } else break;
                    }
                    if (temp > bestCount) {
                        bestCount = temp;
                        bestMove = "left";
                    }
                    checkList.clear();
                    break;
                case "right":
                    for (int i = 1; ; i++) {
                        checkList.add(new SnakePoints(headPositionX + i * (pointSize * 2), headPositionY));
                        if ((headPositionX + i * (pointSize * 2)) >= surfaceView.getWidth())
                            break;
                    }
                    for (SnakePoints snakePoints : checkList) {
                        if (!aiPointsList.contains(snakePoints) && !barriers.contains(snakePoints)) {
                            temp++;
                        } else break;
                    }
                    if (temp > bestCount) {
                        bestCount = temp;
                        bestMove = "right";
                    }
                    checkList.clear();
                    break;
            }
        }
        return bestMove;
    }

    private String getNextMovingDirection(int headPositionX, int headPositionY, int positionX, int positionY, String movingDirection, List<SnakePoints> aiPointsList) {
        // 判断食物的位置相对于蛇头的位置关系
        boolean foodIsOnRight = positionX >= headPositionX;
        boolean foodIsOnBottom = positionY >= headPositionY;
        boolean canMoveDown = canMoveTo(headPositionX, headPositionY + (pointSize * 2), aiPointsList) && !movingDirection.equals("up");
        boolean canMoveUp = canMoveTo(headPositionX, headPositionY - (pointSize * 2), aiPointsList) && !movingDirection.equals("down");
        boolean canMoveRight = canMoveTo(headPositionX + (pointSize * 2), headPositionY, aiPointsList) && !movingDirection.equals("left");
        boolean canMoveLeft = canMoveTo(headPositionX - (pointSize * 2), headPositionY, aiPointsList) && !movingDirection.equals("right");
        List<String> possibleDirections = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        // 如果蛇头和食物的水平或垂直距离相等
        if (headPositionX == positionX || headPositionY == positionY) {
            if (headPositionX == positionX) {
                if (foodIsOnBottom) {
                    if (!movingDirection.equals("up")) {
                        // 如果蛇头不向上移动，食物在下方
                        if (canMoveDown) {
                            return "down";
                        }
                    }
                } else {
                    if (!movingDirection.equals("down")) {
                        // 如果蛇头不向下移动，食物在上方
                        if (canMoveUp) {
                            return "up";
                        }
                    }
                }
            } else {
                if (foodIsOnRight) {
                    if (!movingDirection.equals("left")) {
                        // 如果蛇头不向左移动，食物在右方
                        if (canMoveRight) {
                            return "right";
                        }
                    }
                } else {
                    if (!movingDirection.equals("right")) {
                        // 如果蛇头不向右移动，食物在左方
                        if (canMoveLeft) {
                            return "left";
                        }
                    }
                }
            }
        }

        // 判断蛇头和食物的相对位置，确定下一步移动的方向
        if (foodIsOnRight && (positionX != headPositionX)) {
            if (!movingDirection.equals("left")) {
                // 如果蛇头其他位置移动，但食物在右侧
                if (canMoveRight && getPossibleDirections(headPositionX, headPositionY, aiPointsList).contains("right")) {
                    return "right";
                }
            } else {
                // 如果蛇头向左移动，但食物在右侧
                if (foodIsOnBottom && getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList).equals("down")) {
                    return "down";
                } else if (!foodIsOnBottom && getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList).equals("up")) {
                    return "up";
                }
            }
        } else if (!foodIsOnRight){
            if (!movingDirection.equals("right")) {
                // 如果蛇头其它位置移动，但食物在左侧
                if (canMoveLeft && getPossibleDirections(headPositionX, headPositionY, aiPointsList).contains("left")) {
                    return "left";
                }
            } else {
                if (foodIsOnBottom && getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList).equals("down")) {
                    return "down";
                } else if (!foodIsOnBottom && getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList).equals("up")) {
                    return "up";
                }
            }
        }

        return getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList);
    }
}
