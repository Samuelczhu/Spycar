package com.elec3907.spycar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //key for sending intent for control mode
    public static final String CONTROL_MODE_KEY = "control_mode_key";

    //constants for firebase key
    public static final String DIRECTION_KEY = "direction";
    public static final String SENSOR_DISTANCE_KEY = "sensor_distance";
    public static final String VIDEO_ON_KEY = "video_on";
    public static final String FRONT_LIGHT_ON_KEY = "front_light_on";
    //constants for control mode
    public static final int BUTTON_CONTROL_MODE = 0;
    public static final int WHEEL_CONTROL_MODE = 1;
    public static final int GRAVITY_CONTROL_MODE = 2;

    //parameter for wheel control mode sensor
    public static double WHEEL_Y_LEFT = -3.5; //go left if y less than this value
    public static double WHEEL_Y_RIGHT = 3.5; //go right if y greater than this value
    //parameter for gravity control mode sensor
    public static double GRAVITY_Y_FRONT = -3.5; //go front if y less than this value
    public static double GRAVITY_Y_BACK = 3.5;  //go back if y greater than this value
    public static double GRAVITY_X_RIGHT = -3.5; //go right if x less than this value
    public static double GRAVITY_X_LEFT = 3.5; //go left if x greater than this value

    //control mode of this activity
    public static int CONTROL_MODE = 0;

    //sensor manager for the wheel and gravity control mode
    private SensorManager sensorManager;
    private Sensor accelerometer; //sensor for accelerometer

    //setting button and info button
    private Button settingButton, infoButton;
    //text display for sensor data
    private TextView sensorDataDisplay;
    //text display for different control mode
    private TextView controlModeDisplay;
    //switches for video camera and front light
    private Switch switchVideo, switchLight;

    //hold the direction for the Spycar
    private Direction direction;

    //the DatabaseReference
    DatabaseReference databaseReference;

    //direction buttons for button control
    private Button buttonFront, buttonBack, buttonLeft, buttonRight;
    //direction radio buttons for wheel control
    private RadioButton radioButtonFront, radioButtonStop, radioButtonBack;
    //direction toggle button for gravity control
    private ToggleButton toggleButtonGoStop;

    //radio group for wheel mode
    private RadioGroup radioGroupWheel;

    //front back direction parameter for font back in wheel mode
    //constants to use
    public static final int WHEEL_STOP = 0;
    public static final int WHEEL_FRONT = 1;
    public static final int WHEEL_BACK = 2;
    private int wheelFrontBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); //hide the title bar
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //get the control mode if from SettingActivity, default to button control mode
        MainActivity.CONTROL_MODE = getIntent().getIntExtra(MainActivity.CONTROL_MODE_KEY, MainActivity.BUTTON_CONTROL_MODE);

        //get the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //setting up the accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        //initialize the direction of the Spycar
        direction = new Direction();

        //find the control mode display
        controlModeDisplay = (TextView) findViewById(R.id.control_mode_display);
        //find the sensor data display
        sensorDataDisplay = (TextView) findViewById(R.id.sensor_data_display);
        //find the toggle switches
        switchVideo = (Switch) findViewById(R.id.switch_videocam);
        switchLight = (Switch) findViewById(R.id.switch_light);
        //find the setting button and info button
        settingButton = (Button) findViewById(R.id.button_setting);
        infoButton = (Button) findViewById(R.id.button_info);

        //find the layout for controlling panel
        RelativeLayout button_control_panel = (RelativeLayout) findViewById(R.id.button_control_panel);
        RelativeLayout wheel_control_panel = (RelativeLayout) findViewById(R.id.wheel_control_panel);
        RelativeLayout gravity_control_panel = (RelativeLayout) findViewById(R.id.gravity_control_panel);

        //setup the control mode
        if (MainActivity.CONTROL_MODE == MainActivity.WHEEL_CONTROL_MODE) {
            controlModeDisplay.setText(getResources().getString(R.string.wheel)); //set the display for mode
            button_control_panel.setVisibility(View.GONE);
            wheel_control_panel.setVisibility(View.VISIBLE);
            gravity_control_panel.setVisibility(View.GONE);
        } else if (MainActivity.CONTROL_MODE == MainActivity.GRAVITY_CONTROL_MODE) {
            controlModeDisplay.setText(getResources().getString(R.string.gravity)); //set the display for mode
            button_control_panel.setVisibility(View.GONE);
            wheel_control_panel.setVisibility(View.GONE);
            gravity_control_panel.setVisibility(View.VISIBLE);
        } else { //default to button control mode
            controlModeDisplay.setText(getResources().getString(R.string.button)); //set the display for mode
            MainActivity.CONTROL_MODE = MainActivity.BUTTON_CONTROL_MODE;
            button_control_panel.setVisibility(View.VISIBLE);
            wheel_control_panel.setVisibility(View.GONE);
            gravity_control_panel.setVisibility(View.GONE);
        }

        //find the direction buttons for button control mode
        buttonFront = (Button) findViewById(R.id.button_front);
        buttonBack = (Button) findViewById(R.id.button_back);
        buttonLeft = (Button) findViewById(R.id.button_left);
        buttonRight = (Button) findViewById(R.id.button_right);
        //find the direction radio buttons for wheel control mode
        radioGroupWheel = (RadioGroup) findViewById(R.id.radio_group_wheel); //find the radio group for wheel mode control
        radioButtonFront = (RadioButton) findViewById(R.id.radio_button_front);
        radioButtonStop = (RadioButton) findViewById(R.id.radio_button_stop);
        radioButtonBack = (RadioButton) findViewById(R.id.radio_button_back);
        //find the direction toggle button for gravity control mode
        toggleButtonGoStop = (ToggleButton) findViewById(R.id.toggle_button_go_stop);

        //event listener for setting button
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to the setting button
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra(MainActivity.CONTROL_MODE_KEY, MainActivity.CONTROL_MODE);
                startActivity(intent);
                finish(); //finish this activity before go to setting activity
            }
        });
        //event listener for info button
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implement the info button event
            }
        });


        //event listener for direction radio button for wheel control mode
        radioGroupWheel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //TODO: delete this after debug
                Log.d("id",""+checkedId);
                if (checkedId == R.id.radio_button_stop) {
                    wheelFrontBack = MainActivity.WHEEL_STOP; //change to front
                } else if (checkedId == R.id.radio_button_front) {
                    wheelFrontBack = MainActivity.WHEEL_FRONT;
                } else if (checkedId == R.id.radio_button_back) {
                    wheelFrontBack = MainActivity.WHEEL_BACK;
                }
            }
        });
        //default to stop in front back position
        radioButtonStop.setChecked(true);

        //event listener for direction toggle button for gravity control mode
        toggleButtonGoStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //change the color to green for go
                    toggleButtonGoStop.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ripple_green));
                } else { //change the color to orange for stop
                    toggleButtonGoStop.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ripple_orange));
                }
            }
        });

        //event listeners for direction buttons for button control mode
        buttonFront.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    //go front if user press down and update to fireBase
                    updateDirection(direction.goFront());
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    //stop in front direction if user release and update to fireBase
                    updateDirection(direction.stopFrontBack());
                }
                return false;
            }
        });
        buttonBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    //go back if user press down and update to fireBase
                    updateDirection(direction.goBack());
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    //stop in back direction if user release and update to firebase
                    updateDirection(direction.stopFrontBack());
                }
                return false;
            }
        });
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    //go left if user press down and update to firebase
                    updateDirection(direction.goLeft());
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    //stop in left direction if user release and update to firebase
                    updateDirection(direction.stopLeftRight());
                }
                return false;
            }
        });
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    //go right if user press down and update to firebase
                    updateDirection(direction.goRight());
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    //stop in right direction if user release and update to firebase
                    updateDirection(direction.stopLeftRight());
                }
                return false;
            }
        });

        //event listener for switches
        switchVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //turn the video on or off and update the video_on value on firebase accordingly
                turnVideo(isChecked);
            }
        });
        switchLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //turn the light on or off and update the front_light_on value on firebase accordingly
                turnLight(isChecked);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //respond for wheel mode or gravity mode
        //response for wheel control mode
        if (MainActivity.CONTROL_MODE == MainActivity.WHEEL_CONTROL_MODE) {
            //generate a current direction object for comparison with the original direction object
            Direction currentDirection = new Direction();
            //check radio button for front back direction
            //TODO: delete this after debug
            Log.d("front back status",""+wheelFrontBack);
            if (wheelFrontBack == MainActivity.WHEEL_FRONT) {
                currentDirection.goFront();
            } else if (wheelFrontBack == MainActivity.WHEEL_BACK) {
                currentDirection.goBack();
            } else if (wheelFrontBack == MainActivity.WHEEL_STOP) { //default to stop in front back position
                currentDirection.stopFrontBack();
            }
            //check for Y axis of accelerometer for left right position
            double yVal = event.values[1];
            if (yVal < MainActivity.WHEEL_Y_LEFT) {
                currentDirection.goLeft();
            } else if (yVal > MainActivity.WHEEL_Y_RIGHT) {
                currentDirection.goRight();
            } else {
                currentDirection.stopLeftRight();
            }
            //compare current direction with original direction
            //if they are different, update the database
            if (!currentDirection.equals(direction)) {
                direction = currentDirection; //update the direction
                updateDirection(direction.toString()); //update the direction on database
            }
            //delete the currentDirection object after comparing
            currentDirection = null;
        }
        //response for gravity control mode
        else if (MainActivity.CONTROL_MODE == MainActivity.GRAVITY_CONTROL_MODE) {
            //generate a current direction object for comparison with the original direction object
            Direction currentDirection = new Direction();
            //check toggle button to see whether go or stop
            if (!toggleButtonGoStop.isChecked()) { //stop in all direction
                currentDirection.stopFrontBack();
                currentDirection.stopLeftRight();
            } else { //go to direction based on gravity sensor
                double xVal = event.values[0];
                double yVal = event.values[1];
                //deal with front back direction
                if (yVal<MainActivity.GRAVITY_Y_FRONT) {
                    currentDirection.goFront();
                } else if (yVal>MainActivity.GRAVITY_Y_BACK) {
                    currentDirection.goBack();
                } else {
                    currentDirection.stopFrontBack();
                }
                //deal with right left direction
                if (xVal<MainActivity.GRAVITY_X_RIGHT) {
                    currentDirection.goRight();
                } else if (xVal>MainActivity.GRAVITY_X_LEFT) {
                    currentDirection.goLeft();
                } else {
                    currentDirection.stopLeftRight();
                }
            }
            //compare current direction with original direction
            //if they are different, update the database
            if (!currentDirection.equals(direction)) {
                direction = currentDirection; //update the direction
                updateDirection(direction.toString()); //update the direction on database
            }
            //delete the currentDirection object after comparing
            currentDirection = null;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * This method is use to turn on or off the video and set the video_on on firebase accordingly
     * @param turnOn true to turn the video on, false to turn the video off
     */
    public void turnVideo(boolean turnOn) {
        //get the databaseReference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(VIDEO_ON_KEY);
        databaseReference.setValue(turnOn); //set the video_on value on firebase
    }

    /**
     * This method is use to turn on or off the front light of the Spycar and set the front_light_on on firebase accordingly
     * @param turnOn true to turn the front light on, false to turn the front light off
     */
    public void turnLight(boolean turnOn) {
        //get the databaseReference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FRONT_LIGHT_ON_KEY);
        databaseReference.setValue(turnOn); //set the front_light_on value on firebase
    }

    /**
     * Update the direction on the Firebase
     * @param direction the direction for the car expressed in String
     */
    public void updateDirection(String direction) {
        //get the database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(DIRECTION_KEY);
        //set the direction in database
        databaseReference.setValue(direction);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //reset the direction on firebase
        updateDirection("ss");
        //listen to change on the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //read the sensor data and update the text display in centimeter
                sensorDataDisplay.setText(dataSnapshot.child(SENSOR_DISTANCE_KEY).getValue().toString()+" cm");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //turn off the video
        switchVideo.setChecked(false);
        //turn off the front light
        switchLight.setChecked(false);
        //reset the direction on firebase
        updateDirection("ss");
    }
}
