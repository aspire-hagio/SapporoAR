package com.example.sapporoar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends ActionBarActivity implements LocationListener, SensorEventListener {
    private SurfaceView mySurfaceView;
    private Camera myCamera;
    private SensorManager manager;
    private float x, y, z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = mLocationManager.getBestProvider(criteria, true);

        mLocationManager.requestLocationUpdates(provider, 0, 0, this);

        mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        SurfaceHolder holder = mySurfaceView.getHolder();

        holder.addCallback(callback);


        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView tv_provider = (TextView) findViewById(R.id.text1);
        tv_provider.setText(location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //コールバック
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

            //CameraOpen
            myCamera = Camera.open();

            //出力をSurfaceViewに設定
            try {
                myCamera.setPreviewDisplay(surfaceHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

            //プレビュースタート（Changedは最初にも1度は呼ばれる）
            myCamera.startPreview();

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            //片付け
            myCamera.release();
            myCamera = null;
        }
    };

    float[] _aVal;
    float[] _mVal;

    @Override
    public void onSensorChanged(SensorEvent event) {

        //センサー値の取得
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度センサー
                _aVal = new float[3];
                _aVal = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD: //磁気センサー
                _mVal = new float[3];
                _mVal = event.values;
                break;
        }

        StringBuilder sb = new StringBuilder();

        if (_aVal != null && _mVal != null) {
            float[] R1 = new float[16];
            float[] R2 = new float[16];
            float[] I = new float[16];
            float[] val = new float[3];

            SensorManager.getRotationMatrix(R1, I, _aVal, _mVal);
            SensorManager.remapCoordinateSystem(R1, SensorManager.AXIS_X, SensorManager.AXIS_Z, R2);
            SensorManager.getOrientation(R2, val);
            //ラジアンを角度に変換
            for (int i = 0; i < 3; i++) {
                val[i] = (float) (val[i] * 180 / Math.PI);
            }

            sb.append(String.format("磁気センサー + 加速度センサー\n"));
            sb.append(String.format("方位角:%f\n", (val[0] < 0) ? val[0] + 360 : val[0]));
            sb.append(String.format("傾斜角:%f\n", val[1]));
            sb.append(String.format("回転角:%f\n", val[2]));
        }

        TextView tv = (TextView) findViewById(R.id.text1);
        tv.setText(sb.toString());

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // Listenerの登録
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }


        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

    }
}
