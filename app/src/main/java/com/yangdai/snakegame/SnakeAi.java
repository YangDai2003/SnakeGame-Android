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
        if (y < 0 || y >= surfaceView.getHeight()) {
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

    private boolean canMoveToFood(int headPositionX, int headPositionY, int positionX, int positionY, String direction, List<SnakePoints> aiPointsList) {
        List<SnakePoints> checkList = new ArrayList<>();
        switch (direction) {
            case "up":
                for (int i = 1; ; i++) {
                    if (headPositionX == positionX && (headPositionY - i * (pointSize * 2)) == positionY)
                        break;
                    checkList.add(new SnakePoints(headPositionX, headPositionY - i * (pointSize * 2)));
                }
                for (SnakePoints snakePoints : checkList) {
                    if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                        return false;
                    }
                }
                break;
            case "down":
                for (int i = 1; ; i++) {
                    if (headPositionX == positionX && (headPositionY + i * (pointSize * 2)) == positionY)
                        break;
                    checkList.add(new SnakePoints(headPositionX, headPositionY + i * (pointSize * 2)));
                }
                for (SnakePoints snakePoints : checkList) {
                    if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                        return false;
                    }
                }
                break;
            case "left":
                for (int i = 1; ; i++) {
                    if ((headPositionX - i * (pointSize * 2)) == positionX && headPositionY == positionY)
                        break;
                    checkList.add(new SnakePoints(headPositionX - i * (pointSize * 2), headPositionY));
                }
                for (SnakePoints snakePoints : checkList) {
                    if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                        return false;
                    }
                }
                break;
            case "right":
                for (int i = 1; ; i++) {
                    if ((headPositionX + i * (pointSize * 2)) == positionX && headPositionY == positionY)
                        break;
                    checkList.add(new SnakePoints(headPositionX + i * (pointSize * 2), headPositionY));
                }
                for (SnakePoints snakePoints : checkList) {
                    if (aiPointsList.contains(snakePoints) || barriers.contains(snakePoints)) {
                        return false;
                    }
                }
                break;
        }
        return true;
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
                        if ((headPositionY - i * (pointSize * 2)) < 0) break;
                        checkList.add(new SnakePoints(headPositionX, headPositionY - i * (pointSize * 2)));
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
                        if ((headPositionY + i * (pointSize * 2)) >= surfaceView.getHeight())
                            break;
                        checkList.add(new SnakePoints(headPositionX, headPositionY + i * (pointSize * 2)));
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
                        if ((headPositionX - i * (pointSize * 2)) < 0) {
                            int max = (surfaceView.getWidth() - (pointSize * 2)) / pointSize;
                            if (max % 2 != 0) max -= 1;
                            for (int j = 0; j < 5; j++) {
                                checkList.add(new SnakePoints(pointSize * (max - j) + pointSize, headPositionY));
                            }
                            break;
                        }
                        checkList.add(new SnakePoints(headPositionX - i * (pointSize * 2), headPositionY));
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
                        if ((headPositionX + i * (pointSize * 2)) >= surfaceView.getWidth()) {
                            for (int j = 0; j < 5; j++) {
                                checkList.add(new SnakePoints(pointSize + j * (pointSize * 2), headPositionY));
                            }
                            break;
                        }
                        checkList.add(new SnakePoints(headPositionX + i * (pointSize * 2), headPositionY));
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
        int distX = Math.abs(positionX - headPositionX);
        int distY = Math.abs(positionY - headPositionY);
        boolean canMoveDown = canMoveTo(headPositionX, headPositionY + (pointSize * 2), aiPointsList) && !movingDirection.equals("up");
        boolean canMoveUp = canMoveTo(headPositionX, headPositionY - (pointSize * 2), aiPointsList) && !movingDirection.equals("down");
        boolean canMoveRight = canMoveTo(headPositionX + (pointSize * 2), headPositionY, aiPointsList) && !movingDirection.equals("left");
        boolean canMoveLeft = canMoveTo(headPositionX - (pointSize * 2), headPositionY, aiPointsList) && !movingDirection.equals("right");
        List<String> possibleDirections = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        if (movingDirection.equals("up")) possibleDirections.remove("down");
        if (movingDirection.equals("down")) possibleDirections.remove("up");
        if (movingDirection.equals("right")) possibleDirections.remove("left");
        if (movingDirection.equals("left")) possibleDirections.remove("right");
        // 如果蛇头和食物的水平或垂直距离相等
        if (headPositionX == positionX || headPositionY == positionY) {
            if (headPositionX == positionX) {
                if (foodIsOnBottom) {
                    if (!movingDirection.equals("up")) {
                        // 如果蛇头不向上移动，食物在下方
                        if (canMoveDown && canMoveToFood(headPositionX, headPositionY, positionX, positionY, "down", aiPointsList)) {
                            return "down";
                        }
                    }
                } else {
                    if (!movingDirection.equals("down")) {
                        // 如果蛇头不向下移动，食物在上方
                        if (canMoveUp && canMoveToFood(headPositionX, headPositionY, positionX, positionY, "up", aiPointsList)) {
                            return "up";
                        }
                    }
                }
            } else {
                if (foodIsOnRight) {
                    if (!movingDirection.equals("left")) {
                        // 如果蛇头不向左移动，食物在右方
                        if (canMoveRight && canMoveToFood(headPositionX, headPositionY, positionX, positionY, "right", aiPointsList)) {
                            return "right";
                        }
                    }
                } else {
                    if (!movingDirection.equals("right")) {
                        // 如果蛇头不向右移动，食物在左方
                        if (canMoveLeft && canMoveToFood(headPositionX, headPositionY, positionX, positionY, "left", aiPointsList)) {
                            return "left";
                        }
                    }
                }
            }
        }

        List<String> possibleDirection = getPossibleDirections(headPositionX, headPositionY, aiPointsList);
        // 判断蛇头和食物的相对位置，确定下一步移动的方向
        if (foodIsOnRight && (positionX != headPositionX)) {
            if (!movingDirection.equals("left")) {
                // 如果蛇头其他位置移动，但食物在右侧
                if (canMoveRight && possibleDirection.contains("right")) {
                    return "right";
                }
            } else {
                // 如果蛇头向左移动，但食物在右侧
                if (foodIsOnBottom && possibleDirection.contains("down")) {
                    return "down";
                } else if (!foodIsOnBottom && possibleDirection.contains("up")) {
                    return "up";
                }
            }
        } else if (!foodIsOnRight) {
            if (!movingDirection.equals("right")) {
                // 如果蛇头其它位置移动，但食物在左侧
                if (canMoveLeft && possibleDirection.contains("left")) {
                    return "left";
                }
            } else {
                if (foodIsOnBottom && possibleDirection.contains("down")) {
                    return "down";
                } else if (!foodIsOnBottom && possibleDirection.contains("up")) {
                    return "up";
                }
            }
        }

        return getBestDirection(headPositionX, headPositionY, possibleDirections, aiPointsList);
    }
}
