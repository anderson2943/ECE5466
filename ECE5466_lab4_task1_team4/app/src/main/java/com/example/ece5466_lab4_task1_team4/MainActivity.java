package com.example.ece5466_lab4_task1_team4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

//majority of this activity, including layout was copied from Jamey Weyenberg's Lab 1
//if you want to see the app freeze, uncomment line 39 in the onResume() method

public class MainActivity extends AppCompatActivity {
    //Used to keep track of the current counter
    private int counter = 0;
    private double aDouble = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread mythread = new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    double mynum = 5;
                    mynum = mynum * 5 / 23554.0;
                    mynum = Math.sqrt(mynum * mynum) * 61;
                }
            }
        };
        mythread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //heavyComp();
    }

    public void heavyComp(){
        while(true) {
            double mynum = 5;
            mynum = mynum * 5 / 23554.0;
            mynum = Math.sqrt(mynum * mynum) * 61;
        }
    }

    //called when the "Count" button is pressed.
    //Used to update the radioButtons to reflect the updated counter.
    public void count(View view){
        RadioButton rightButton = (RadioButton) findViewById(R.id.rb0);
        RadioButton midButton = (RadioButton) findViewById(R.id.rb1);
        RadioButton leftButton = (RadioButton) findViewById(R.id.rb2);
        counter++;
        switch (counter){
            case 1: //001
                rightButton.setChecked(true);
                break;
            case 2: //010
                rightButton.setChecked(false);
                midButton.setChecked(true);
                break;
            case 3: //011
                rightButton.setChecked(true);
                break;
            case 4: //100
                rightButton.setChecked(false);
                midButton.setChecked(false);
                leftButton.setChecked(true);
                break;
            case 5: //101
                rightButton.setChecked(true);
                //midButton.setChecked(false);
                //leftButton.setChecked(true);
                break;
            case 6: //110
                rightButton.setChecked(false);
                midButton.setChecked(true);
                //leftButton.setChecked(true);
                break;
            case 7: //111
                rightButton.setChecked(true);
                //midButton.setChecked(true);
                //leftButton.setChecked(true);
                break;
            default: //0>=counter>=8 then buttons will be set to 000
                clear(view);
                break;
        }

    }

    //called when counter > 7 or when "Clear" button is pressed.
    public void clear (View view){
        RadioButton rightButton = (RadioButton) findViewById(R.id.rb0);
        RadioButton midButton = (RadioButton) findViewById(R.id.rb1);
        RadioButton leftButton = (RadioButton) findViewById(R.id.rb2);
        counter=0;
        rightButton.setChecked(false);
        midButton.setChecked(false);
        leftButton.setChecked(false);
    }

}