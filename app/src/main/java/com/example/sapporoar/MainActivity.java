package com.example.sapporoar;

import android.app.AlertDialog;
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
import android.opengl.Matrix;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LocationListener, SensorEventListener {
    private SurfaceView mySurfaceView;
    private Camera myCamera;
    private SensorManager manager;
    private float x, y, z;
    private List<Landmark> landmarks;
//    private double currentLatitude = 43.068084;
//    private double currentLongitude = 141.350601;
//    private Landmark targetLandmark = new Landmark("札幌ドーム", 43.015952, 141.409529);
    int sx = 0;
    int sy = 0;
    private double currentLatitude = 43.067561;
    private double currentLongitude = 141.498406;
    private Landmark targetLandmark = new Landmark("札幌ドーム", 43.072726, 141.497290);
    private List<Place> places;
    private boolean gpsFlag;

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

        landmarks = new ArrayList<>();
        landmarks.add(new Landmark("札幌ドーム", 43.015952, 141.409529));
        landmarks.add(new Landmark("テレビ塔", 43.061297, 141.356426));
        landmarks.add(new Landmark("プリンスホテル", 43.055921, 141.341151));
        landmarks.add(new Landmark("ローソン", 43.056440, 141.341220));

        places = new ArrayList<>();
        places.add(new Place("JRタワー", 43.068084, 141.350601));
        places.add(new Place("aspire", 43.055688, 141.342201));
        places.add(new Place("35", 43.067561, 141.498406));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ListView lv = new ListView(this);
        List<String> array = new ArrayList<>();
        for (Place place : places) {
            array.add(place.getName());
        }
        array.add("GPSから取得");
        String[] items = array.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        lv.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("現在地を選択してください").setView(lv);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alertDialog.dismiss();
                if (position < places.size()) {
                    Place place = places.get(position);
                    Toast.makeText(MainActivity.this, place.getName(), Toast.LENGTH_LONG).show();
                    currentLatitude = place.getLatitude();
                    currentLongitude = place.getLongitude();
                    gpsFlag = false;
                } else {
                    // GPSから取得
                    Toast.makeText(MainActivity.this, "GPSから取得" +
                            "", Toast.LENGTH_LONG).show();
                    gpsFlag = true;
                }
            }
        });
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
        if (gpsFlag) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
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

            myCamera.startPreview();

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            // 片付け
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
            SensorManager.remapCoordinateSystem(R1, SensorManager.AXIS_Y, SensorManager.AXIS_Z, R2);
            SensorManager.getOrientation(R2, val);
            //ラジアンを角度に変換
            for (int i = 0; i < 3; i++) {
                val[i] = (float) (val[i] * 180 / Math.PI);
            }
            sb.append(String.format("lat:%f\n", currentLatitude));
            sb.append(String.format("lng:%f\n", currentLongitude));
            sb.append(String.format("磁気センサー + 加速度センサー\n"));
            sb.append(String.format("方位角%f\n", (val[0] < 0) ? val[0] + 360 : val[0]));
            sb.append(String.format("傾斜角%f\n", val[1]));
            sb.append(String.format("回転角%f\n", val[2]));

            float[] result = new float[4];
            float[] v1 = new float[]{0, 1, 0, 0};
            Matrix.multiplyMV(result, 0, R2, 0, v1, 0);

            //  対象地点ベクトル
//            float[] targetV = new float[]{(float) (targetLandmark.getLatitude() - currentLatitude), (float) (targetLandmark.getLongitude() - currentLongitude), 0, 0};
            float[] targetV = new float[]{(float) (targetLandmark.getLatitude() - currentLatitude), 0, (float) (targetLandmark.getLongitude() - currentLongitude), 0};
            float[] c = new float[4];
            float r = (float) (getR(result, targetV) * 180 / Math.PI);
            float n = 0;
            for (int i = 0; i < 4; i++) {
                n += targetV[i] * result[i];
            }
            sb.append(String.format("%f\n", r));
//            sb.append(String.format("%f\n", targetV[0]));
//            sb.append(String.format("%f\n", targetV[1]));
//            sb.append(String.format("%f\n", targetV[2]));
//            sb.append(String.format("%f\n", targetV[3]));

//            sb.append(String.format("%f\n", c[0]));
//            sb.append(String.format("%f\n", c[1]));
//            sb.append(String.format("%f\n", c[2]));
//            sb.append(String.format("%f\n", c[3]));

//            sb.append(String.format("%f\n", result[0]));
//            sb.append(String.format("%f\n", result[1]));
//            sb.append(String.format("%f\n", result[2]));
//            sb.append(String.format("%f\n", result[3]));
        }

        TextView tv = (TextView) findViewById(R.id.text1);
        tv.setText(sb.toString());

    }

    static float getR(float[] v1, float[] v2) {
        return (float) Math.acos((v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]) / (Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]) * Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2])));
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
