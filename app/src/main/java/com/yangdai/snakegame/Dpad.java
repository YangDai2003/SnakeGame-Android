package com.yangdai.snakegame;

import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Dpad {
    final static int UP = 0;
    final static int LEFT = 1;
    final static int RIGHT = 2;
    final static int DOWN = 3;
    final static int CENTER = 4;

    int directionPressed = -1;

    public int getDirectionPressed(InputEvent event) {
        if (!isDpadDevice(event)) {
            return -1;
        }
        // If the input event is a MotionEvent, check its hat axis values.
        if (event instanceof MotionEvent) {
            // Use the hat axis value to find the D-pad direction
            MotionEvent motionEvent = (MotionEvent) event;
            float xAxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yAxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);
            // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
            // LEFT and RIGHT direction accordingly.
            if (Float.compare(xAxis, -1.0f) == 0) {
                directionPressed = Dpad.LEFT;
            } else if (Float.compare(xAxis, 1.0f) == 0) {
                directionPressed = Dpad.RIGHT;
            }
            // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
            // UP and DOWN direction accordingly.
            else if (Float.compare(yAxis, -1.0f) == 0) {
                directionPressed = Dpad.UP;
            } else if (Float.compare(yAxis, 1.0f) == 0) {
                directionPressed = Dpad.DOWN;
            }
        }
        // If the input event is a KeyEvent, check its key code.
        else if (event instanceof KeyEvent) {

            // Use the key code to find the D-pad direction.
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                directionPressed = Dpad.LEFT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                directionPressed = Dpad.RIGHT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                directionPressed = Dpad.UP;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                directionPressed = Dpad.DOWN;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                directionPressed = Dpad.CENTER;
            }
        }
        return directionPressed;
    }

    public static boolean isDpadDevice(InputEvent event) {
        // Check that input comes from a device with directional pads.
        return (event.getSource() & InputDevice.SOURCE_DPAD)
                != InputDevice.SOURCE_DPAD;
    }
}

