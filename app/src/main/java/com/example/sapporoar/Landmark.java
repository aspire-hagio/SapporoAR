package com.example.sapporoar;

import android.hardware.SensorManager;
import android.opengl.Matrix;

public class Landmark {
    private String name;
    private double latitude;
    private double longitude;
    private double screenX;
    private double screenY;
    private boolean visible;

    public Landmark(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setScreenXY(double currentLatitude, double currentLongitude, float[] r2) {
        float[] targetV = new float[]{(float) ((latitude - currentLatitude) * 30.82), (float) ((longitude - currentLongitude) * 25.11), 0, 0};
        float[] inv = new float[16];
        Matrix.invertM(inv, 0, r2, 0);
        float[] target1 = new float[4];
        //
        Matrix.multiplyMV(target1, 0, inv, 0, targetV, 0);

        float[] val = new float[3];
        SensorManager.getOrientation(r2, val);

        double d = target1[0];
        screenX = target1[1] / d;
        if (val[1] > 0) {
            screenY = -1 * Math.abs(target1[2] / d);
        } else {
            screenY = Math.abs(target1[2] / d);
        }
    }

    public double getScreenX() {
        return screenX;
    }

    public void setVisible(float[] v, double currentLatitude, double currentLongitude) {
        float[] targetV = new float[]{(float) ((latitude - currentLatitude) * 30.82), (float) ((longitude - currentLongitude) * 25.11), 0, 0};
        float r = MainActivity.getR(v, targetV);
        if (r < Math.PI / 2) {
            visible = true;
        } else {
            visible = false;
        }
    }

    public boolean getVisible() {
        return visible;
    }

    public double getScreenY() {
        return screenY;
    }
}
