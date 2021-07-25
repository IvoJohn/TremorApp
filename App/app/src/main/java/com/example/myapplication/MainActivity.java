package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    //variables for views
    TextView text_x_value, text_y_value, text_z_value, text_timer;
    Button button_start;
    Button button_save;
    ProgressBar progress_bar;

    //variables for CountDown, timerStarted says if the timer is running
    boolean timerStarted;
    private int progress = 0;
    private CountDownTimer mCountDownTimer;
    private static final long start_time = 30000;
    //private static final long start_time = 3000;
    private long time_left = start_time;

    //variables for creating name of the saved file as current date and time
    Date currentTime = Calendar.getInstance().getTime();
    String pattern = "dd-MM-yyyy-HH:mm:ss";
    DateFormat df = new SimpleDateFormat(pattern);
    String currentTimeSt = df.format(currentTime);
    String currentTimeString = currentTimeSt.replaceAll("\\s+", "");

    //lists for collecting each of the axis of the accelerometer
    List<Float> x_list = new ArrayList<Float>();
    List<Float> y_list = new ArrayList<Float>();
    List<Float> z_list = new ArrayList<Float>();


    //define sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            text_x_value.setText("" + x);
            text_y_value.setText("" + y);
            text_z_value.setText("" + z);

            if (timerStarted){
                x_list.add(x);
                y_list.add(y);
                z_list.add(z);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_x_value = findViewById(R.id.text_x_value);
        text_y_value = findViewById(R.id.text_y_value);
        text_z_value = findViewById(R.id.text_z_value);
        text_timer = (TextView)findViewById(R.id.text_counter);
        button_start = findViewById(R.id.button_start);
        button_save = findViewById(R.id.button_save);
        progress_bar = findViewById(R.id.progress_bar);

        //initialize sensor variables which are defined above
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerStarted) {
                    resetTimer();
                } else {
                    x_list.clear();
                    y_list.clear();
                    z_list.clear();
                    startTimer();
                    button_save.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(time_left, 1000) {
            @Override
            public void onTick(long l) {
                time_left = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerStarted = false;
                button_start.setText("Start");
                button_save.setVisibility(View.VISIBLE);
                for(float x_value:x_list) {
                  Log.d("x_value", "" + x_value);
                };
                resetTimer();
            }
        }.start();

        timerStarted = true;
        button_start.setText("Reset");
    }

    private void resetTimer(){
        time_left = start_time;
        updateCountDownText();
        mCountDownTimer.cancel();
        button_start.setText("Start");
        timerStarted = false;
    }

    private void updateCountDownText(){
        // this method updates both the text as well as the progress bar
        int minutes = (int) (time_left/1000) / 60;
        int seconds = (int) (time_left/1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d", seconds);

        text_timer.setText(timeLeftFormatted);
        int percent_progress = 100 - (int)(time_left * 100 / start_time);
        progress_bar.setProgress(percent_progress);
    }

    public void saveMeasurements(View v) {
        //Method to save lists with measurements
        String x_text = x_list.toString();
        String y_text = y_list.toString();
        String z_text = z_list.toString();

        FileOutputStream fos_x = null;

        try {
            fos_x = openFileOutput("x_list-" + currentTimeString, MODE_PRIVATE);
            fos_x.write(x_text.getBytes());
            Toast.makeText(this, "File saved to: " + getFilesDir() + "/ " + "x_list-" +currentTimeString, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos_x != null){
                try {
                    fos_x.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileOutputStream fos_y = null;
        try {
            fos_y = openFileOutput("y_list-" + currentTimeString, MODE_PRIVATE);
            fos_y.write(y_text.getBytes());
            Toast.makeText(this, "File saved to: " + getFilesDir() + "/ " + "y_list-" +currentTimeString, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos_y != null){
                try {
                    fos_y.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileOutputStream fos_z = null;
        try {
            fos_z = openFileOutput("z_list-" + currentTimeString, MODE_PRIVATE);
            fos_z.write(z_text.getBytes());
            Toast.makeText(this, "File saved to: " + getFilesDir() + "/ " + "z_list-" +currentTimeString, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos_z != null){
                try {
                    fos_z.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        button_save.setVisibility(View.INVISIBLE);

    }


}