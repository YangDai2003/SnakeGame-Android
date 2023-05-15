package com.yangdai.snakegame;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class SnakeAi2 {
    private final SurfaceView surfaceView;
    private final Set<SnakePoints> barriers;
    private final int pointSize;

    public SnakeAi2(SurfaceView surfaceView, Set<SnakePoints> barriers, int pointSize) {
        this.surfaceView = surfaceView;
        this.barriers = barriers;
        this.pointSize = pointSize;
    }

    public String moveAi(int headPositionX, int headPositionY, int positionX, int positionY, String movingDirection, List<SnakePoints> aiPointsList) {
        // 使用A*算法寻找最短路径
        List<SnakePoints> path = findPath(new SnakePoints(headPositionX, headPositionY), new SnakePoints(positionX, positionY), aiPointsList);
        if (path == null || path.size() < 2) {
            // 如果找不到路径，或者路径长度小于2，返回当前移动方向
            return movingDirection;
        } else {
            // 获取下一步移动的方向
            SnakePoints nextPoint = path.get(1);
            if (nextPoint.getPositionX() == headPositionX && nextPoint.getPositionY() == headPositionY - pointSize) {
                return "up";
            } else if (nextPoint.getPositionX() == headPositionX && nextPoint.getPositionY() == headPositionY + pointSize) {
                return "down";
            } else if (nextPoint.getPositionX() == headPositionX - pointSize && nextPoint.getPositionY() == headPositionY) {
                return "left";
            } else if (nextPoint.getPositionX() == headPositionX + pointSize && nextPoint.getPositionY() == headPositionY) {
                return "right";
            } else {
                // 如果下一步移动的方向不是上下左右中的任意一个，返回当前移动方向
                return movingDirection;
            }
        }
    }

    private List<SnakePoints> findPath(SnakePoints start, SnakePoints end, List<SnakePoints> aiPointsList) {
        // 初始化openSet和closedSet
        PriorityQueue<SnakePoints> openSet = new PriorityQueue<>();
        Set<SnakePoints> closedSet = new HashSet<>();

        // 将起点加入openSet
        openSet.add(start);

        // 初始化gScore和fScore
        int[][] gScore = new int[surfaceView.getWidth() / pointSize][surfaceView.getHeight() / pointSize];
        int[][] fScore = new int[surfaceView.getWidth() / pointSize][surfaceView.getHeight() / pointSize];
        for (int i = 0; i < surfaceView.getWidth() / pointSize; i++) {
            for (int j = 0; j < surfaceView.getHeight() / pointSize; j++) {
                gScore[i][j] = Integer.MAX_VALUE;
                fScore[i][j] = Integer.MAX_VALUE;
            }
        }
        gScore[start.getPositionX() / pointSize][start.getPositionY() / pointSize] = 0;
        fScore[start.getPositionX() / pointSize][start.getPositionY() / pointSize] = heuristicCostEstimate(start, end);

        // 开始寻路
        while (!openSet.isEmpty()) {
            // 从openSet中取出fScore最小的点
            SnakePoints current = openSet.poll();

            // 如果当前点是终点，返回路径
            if (current.equals(end)) {
                return reconstructPath(current);
            }

            // 将当前点加入closedSet
            closedSet.add(current);

            // 遍历当前点的邻居
            for (SnakePoints neighbor : getNeighbors(current)) {
                // 如果邻居已经在closedSet中，跳过
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // 计算从起点到邻居的距离
                int tentativeGScore = gScore[current.getPositionX() / pointSize][current.getPositionY() / pointSize] + 1;

                // 如果邻居不在openSet中，或者从起点到邻居的距离更短，更新gScore和fScore，并将邻居加入openSet
                if (!openSet.contains(neighbor) || tentativeGScore < gScore[neighbor.getPositionX() / pointSize][neighbor.getPositionY() / pointSize]) {
                    gScore[neighbor.getPositionX() / pointSize][neighbor.getPositionY() / pointSize] = tentativeGScore;
                    fScore[neighbor.getPositionX() / pointSize][neighbor.getPositionY() / pointSize] = tentativeGScore + heuristicCostEstimate(neighbor, end);
                    neighbor.cameFrom = current;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // 如果找不到路径，返回null
        return null;
    }

    private int heuristicCostEstimate(SnakePoints start, SnakePoints end) {
        // 使用曼哈顿距离作为启发式函数
        return Math.abs(start.getPositionX() - end.getPositionX()) + Math.abs(start.getPositionY() - end.getPositionY());
    }

    private List<SnakePoints> getNeighbors(SnakePoints point) {
        List<SnakePoints> neighbors = new ArrayList<>();
        if (point.getPositionX() > 0) {
            SnakePoints left = new SnakePoints(point.getPositionX() - pointSize, point.getPositionY());
            if (!barriers.contains(left)) {
                neighbors.add(left);
            }
        }
        if (point.getPositionX() < surfaceView.getWidth() - pointSize) {
            SnakePoints right = new SnakePoints(point.getPositionX() + pointSize, point.getPositionY());
            if (!barriers.contains(right)) {
                neighbors.add(right);
            }
        }
        if (point.getPositionY() > 0) {
            SnakePoints up = new SnakePoints(point.getPositionX(), point.getPositionY() - pointSize);
            if (!barriers.contains(up)) {
                neighbors.add(up);
            }
        }
        if (point.getPositionY() < surfaceView.getHeight() - pointSize) {
            SnakePoints down = new SnakePoints(point.getPositionX(), point.getPositionY() + pointSize);
            if (!barriers.contains(down)) {
                neighbors.add(down);
            }
        }
        return neighbors;
    }

    private List<SnakePoints> reconstructPath(SnakePoints current) {
        List<SnakePoints> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.cameFrom;
        }
        return path;
    }
}