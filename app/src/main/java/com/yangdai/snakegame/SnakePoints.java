package com.yangdai.snakegame;

import androidx.annotation.Nullable;

public class SnakePoints {
    private int positionX, positionY;
    public SnakePoints cameFrom;

    public SnakePoints(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    @Override
    public int hashCode() {
        return positionX + positionY;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj != null) {
            return (this.hashCode() == obj.hashCode())
                    && (positionX == ((SnakePoints) obj).positionX)
                    && (positionY == ((SnakePoints) obj).positionY);
        } else return false;
    }
}
