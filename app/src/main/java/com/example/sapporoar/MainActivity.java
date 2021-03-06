package com.example.sapporoar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    //    private double currentLatitude = 43.068084;
//    private double currentLongitude = 141.350601;
//    private Landmark targetLandmark = new Landmark("札幌ドーム", 43.015952, 141.409529);
    int sx = 0;
    int sy = 0;
    float[] _aVal;
    float[] _mVal;
    private SurfaceView mySurfaceView;
    private SurfaceView surfaceViewOverlay;
    private Camera myCamera;
    private SensorManager manager;
    private float x, y, z;
    private List<Landmark> landmarks;
    private double currentLatitude = 43.067561;
    private double currentLongitude = 141.498406;
    private Landmark targetLandmark = new Landmark("札幌ドーム", 43.072726, 141.497290);
    private List<Place> places;
    private boolean gpsFlag;
    private boolean lowpassFlag;
    private float[] lowpassAVal;
    private boolean medianFlag;
    private ArrayList<Float> medianMValX = new ArrayList<>();
    private ArrayList<Float> medianMValY = new ArrayList<>();
    private ArrayList<Float> medianMValZ = new ArrayList<>();
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
    private SurfaceHolder.Callback callbackOverlay = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            DrawThread thread = new DrawThread(holder);
            Thread thread2 = new Thread(thread);
            thread2.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    static float getR(float[] v1, float[] v2) {
//        System.out.println("v1 x: " + v1[0]);
//        System.out.println("v1 y: " + v1[1]);
//        System.out.println("v1 z: " + v1[2]);
//        System.out.println("v1 ?: " + v1[3]);
//        System.out.println("v2 x: " + v2[0]);
//        System.out.println("v2 y: " + v2[1]);
//        System.out.println("v2 z: " + v2[2]);
//        System.out.println("v2 ?: " + v2[3]);
        return (float) Math.acos((v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]) / (Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]) * Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2])));
    }

    private float[] getMedianMVal() {
        float[] result = new float[3];
        ArrayList<Float> ary = (ArrayList<Float>) medianMValX.clone();
        Collections.sort(ary);
        result[0] = ary.get(ary.size() / 2);
        ary = (ArrayList<Float>) medianMValY.clone();
        Collections.sort(ary);
        result[1] = ary.get(ary.size() / 2);
        ary = (ArrayList<Float>) medianMValZ.clone();
        Collections.sort(ary);
        result[2] = ary.get(ary.size() / 2);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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

        surfaceViewOverlay = (SurfaceView) findViewById(R.id.surfaceView2);
        surfaceViewOverlay.setZOrderOnTop(true);
        SurfaceHolder holderOverlay = surfaceViewOverlay.getHolder();
        holderOverlay.setFormat(PixelFormat.TRANSPARENT);
        holderOverlay.addCallback(callbackOverlay);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);

        landmarks = new ArrayList<>();
        landmarks.add(new Landmark("札幌ドーム", 43.015952, 141.409529));
        landmarks.add(new Landmark("テレビ塔", 43.061297, 141.356426));
        landmarks.add(new Landmark("プリンスホテル", 43.056060, 141.341295));
        landmarks.add(new Landmark("ローソン", 43.056440, 141.341220));
        landmarks.add(new Landmark("札幌医学技術福祉歯科専門学校", 43.053556, 141.341375));
        landmarks.add(new Landmark("大麻駅", 43.072726, 141.497290));

        targetLandmark = landmarks.get(1);

        places = new ArrayList<>();
        places.add(new Place("JRタワー", 43.068084, 141.350601));
        places.add(new Place("aspire", 43.055688, 141.342201));
        places.add(new Place("35", 43.067561, 141.498406));

        Button btnMenu = (Button) findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        //センサー値の取得
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度センサー
                _aVal = new float[3];
                _aVal = event.values;
                if (lowpassAVal == null) {
                    lowpassAVal = new float[]{0, 0, 0};
                }
                for (int i = 0; i < 3; i++) {
                    lowpassAVal[i] = (float) (lowpassAVal[i] * 0.9 + _aVal[i] * 0.1);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD: //磁気センサー
                _mVal = new float[3];
                _mVal = event.values;
                medianMValX.add(_mVal[0]);
                medianMValY.add(_mVal[1]);
                medianMValZ.add(_mVal[2]);
                if (medianMValX.size() > 5) {
                    medianMValX.remove(0);
                    medianMValY.remove(0);
                    medianMValZ.remove(0);
                }
                break;
        }

        StringBuilder sb = new StringBuilder();

        if (_aVal != null && _mVal != null) {
            float[] R1 = new float[16];
            float[] R2 = new float[16];
            float[] I = new float[16];
            float[] val = new float[3];

            if (lowpassFlag && medianFlag) {
                SensorManager.getRotationMatrix(R1, I, lowpassAVal, getMedianMVal());
            } else if(lowpassFlag && !medianFlag) {
                SensorManager.getRotationMatrix(R1, I, lowpassAVal, _mVal);
            }else if (!lowpassFlag && medianFlag) {
                SensorManager.getRotationMatrix(R1, I, _aVal, getMedianMVal());
            } else {
                SensorManager.getRotationMatrix(R1, I, _aVal, _mVal);
            }
            SensorManager.remapCoordinateSystem(R1, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, R2);
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
            float[] v1 = new float[]{1, 0, 0, 0};
            Matrix.multiplyMV(result, 0, R2, 0, v1, 0);

            for (Landmark landmark : landmarks) {
                if (myCamera != null) {
                    landmark.setScreenXY(currentLatitude, currentLongitude, R2);
                    landmark.setVisible(result, currentLatitude, currentLongitude);
                }
            }
            //  対象地点ベクトル
//            float[] targetV = new float[]{(float) (targetLandmark.getLatitude() - currentLatitude), (float) (targetLandmark.getLongitude() - currentLongitude), 0, 0};
            float[] targetV = new float[]{(float) (targetLandmark.getLatitude() - currentLatitude), (float) (targetLandmark.getLongitude() - currentLongitude), 0, 0};
            float[] inv = new float[16];
            Matrix.invertM(inv, 0, R2, 0);
            float[] target1 = new float[4];
            Matrix.multiplyMV(target1, 0, inv, 0, targetV, 0);
            sb.append(String.format("a:%f\n", target1[0]));
            sb.append(String.format("b:%f\n", target1[1]));
            sb.append(String.format("c:%f\n", target1[2]));
            sb.append(String.format("d:%f\n", target1[3]));
//            float[] c = new float[4];
            float r = (float) (getR(result, targetV) * 180 / Math.PI);
//            float n = 0;
//            for (int i = 0; i < 4; i++) {
//                n += targetV[i] * result[i];
//            }
            sb.append(String.format("%f\n", r));
            if (myCamera != null) {
                sb.append(String.format("h:%f\n", myCamera.getParameters().getHorizontalViewAngle()));
                sb.append(String.format("v:%f\n", myCamera.getParameters().getVerticalViewAngle()));
            }
        }

        TextView tv = (TextView) findViewById(R.id.text1);
        tv.setTextColor(Color.RED);
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

    private void showMainMenu() {
        ListView lv = new ListView(this);
        List<String> array = new ArrayList<>();
        array.add("現在地");
        array.add("フィルタリング");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, array);
        lv.setAdapter(adapter);
        final AlertDialog dlg = new AlertDialog.Builder(this).setView(lv).create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                } else if (position == 1) {
                    showFilteringMenu();
                }
                dlg.dismiss();
            }
        });
        dlg.show();
    }

    private void showFilteringMenu() {
        ListView lv = new ListView(this);
        List<String> array = new ArrayList<>();
        array.add("なし");
        array.add("ローパスフィルタ");
        array.add("メディアンフィルタ");
        array.add("ローパス・メディアン");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, array);
        lv.setAdapter(adapter);
        final AlertDialog dlg = new AlertDialog.Builder(this).setView(lv).create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    lowpassFlag = false;
                    medianFlag = false;
                } else if (position == 1) {
                    lowpassFlag = true;
                    medianFlag = false;
                } else if (position == 2) {
                    lowpassFlag = false;
                    medianFlag = true;
                } else if (position == 3) {
                    lowpassFlag = true;
                    medianFlag = true;
                }
                dlg.dismiss();
            }
        });
        dlg.show();
    }

    private class DrawThread implements Runnable {
        SurfaceHolder holder;

        DrawThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            Paint paint = new Paint(Color.GREEN);

            while (true) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(24);
                    paint.setColor(Color.GREEN);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    for (Landmark landmark : landmarks) {
                        if (landmark.getVisible()) {
                            canvas.drawText(landmark.getName(), (float) (canvas.getWidth() * landmark.getScreenX() + canvas.getWidth() / 2), (float) (canvas.getHeight() * landmark.getScreenY() + canvas.getHeight() / 2), paint);
                        }
                    }
                    holder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
