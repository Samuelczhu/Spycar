package com.elec3907.spycar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager; //sensor manager for the accelerometer
    private Sensor accelerometer; //accelerometer sensor

    private RadioButton buttonMode, wheelMode, gravityMode; //radio buttons
    private TextView displayX, displayY, displayZ; //text views for accelerometer display
    private Button okButton, cancelButton; //buttons for ok and cancel

    private int previousControlMode;
    private int newControlMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //receive the intent from MainActivity
        previousControlMode = getIntent().getIntExtra(MainActivity.CONTROL_MODE_KEY, MainActivity.BUTTON_CONTROL_MODE);

        //setting up the accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //setup the sensor manager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(SettingActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //find views
        //find the radio buttons
        buttonMode = (RadioButton) findViewById(R.id.button_mode);
        wheelMode = (RadioButton) findViewById(R.id.wheel_mode);
        gravityMode = (RadioButton) findViewById(R.id.gravity_mode);
        //find the accelerometer display
        displayX = (TextView) findViewById(R.id.display_x);
        displayY = (TextView) findViewById(R.id.display_y);
        displayZ = (TextView) findViewById(R.id.display_z);
        //find the ok and cancel button
        okButton = (Button) findViewById(R.id.button_ok);
        cancelButton = (Button) findViewById(R.id.button_cancel);

        //onclick listener for ok and cancel button
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the new control mode
                if (wheelMode.isChecked()) {
                    newControlMode = MainActivity.WHEEL_CONTROL_MODE;
                } else if (gravityMode.isChecked()) {
                    newControlMode = MainActivity.GRAVITY_CONTROL_MODE;
                } else { //default to button control mode
                    newControlMode = MainActivity.BUTTON_CONTROL_MODE;
                }
                //go to main activity with updated values
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.CONTROL_MODE_KEY, newControlMode);
                startActivity(intent);
                finish(); //finish this activity
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: fully implement this method
                //go back to main activity
                Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                intent.putExtra(MainActivity.CONTROL_MODE_KEY, previousControlMode);
                startActivity(intent);
                finish(); //finish this activity
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        displayX.setText("X: "+event.values[0]);
        displayY.setText("Y: "+event.values[1]);
        displayZ.setText("Z: "+event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
