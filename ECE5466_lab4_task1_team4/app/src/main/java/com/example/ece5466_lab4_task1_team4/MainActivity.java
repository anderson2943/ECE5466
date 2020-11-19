package com.example.ece5466_lab4_task1_team4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import java.util.Random;

//majority of this activity, including layout was copied from Jamey Weyenberg's Lab 1
//if you want to see the app freeze, uncomment line 39 in the onResume() method

public class MainActivity extends AppCompatActivity {
    //Used to keep track of the current counter
    private int counter = 0;
    private double aDouble = 0;
    private Thread mythread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
    public class myCompThread extends Thread{
        @Override
        public void run() {
            super.run();
            int i=0;
            Random random = new Random();
            while(i<999999) {
                double mynum = 5.51234+random.nextInt(5000);
                mynum = mynum * 512.2143 / 23554.0;
                mynum = Math.sqrt(mynum * mynum) * 61;
                double mynum2 = 23476325.345623+random.nextInt(5000);
                mynum = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                double mynum3 = 5.51234+random.nextInt(5000);
                mynum = mynum * 512.2143 / 23554.0;
                mynum3 = Math.sqrt(mynum * mynum) * 61;
                double mynum4 = 23476325.345623+random.nextInt(5000);
                mynum4 = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                double mynum5 = 5.51234+random.nextInt(5000);
                mynum = mynum5 * 512.2143 / 23554.0;
                mynum5 = Math.sqrt(mynum * mynum) * 61;
                double mynum6 = 23476325.345623+random.nextInt(5000);
                mynum6 = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                i++;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //heavyComp();
    }

    public void heavyComp(){
        int i=0;
        Random random = new Random();
        while(i<999999) {
                double mynum = 5.51234+random.nextInt(5000);
                mynum = mynum * 512.2143 / 23554.0;
                mynum = Math.sqrt(mynum * mynum) * 61;
                double mynum2 = 23476325.345623+random.nextInt(5000);
                mynum = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                double mynum3 = 5.51234+random.nextInt(5000);
                mynum = mynum * 512.2143 / 23554.0;
                mynum3 = Math.sqrt(mynum * mynum) * 61;
                double mynum4 = 23476325.345623+random.nextInt(5000);
                mynum4 = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                double mynum5 = 5.51234+random.nextInt(5000);
                mynum = mynum5 * 512.2143 / 23554.0;
                mynum5 = Math.sqrt(mynum * mynum) * 61;
                double mynum6 = 23476325.345623+random.nextInt(5000);
                mynum6 = mynum2 * 5 / 23554.0;
                mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                i++;

        }
    }

    //called when the "Count" button is pressed.
    //Used to update the radioButtons to reflect the updated counter.
    public void count(View view){
        //Uncomment the following to see a lag in the UI
        //heavyComp();
        //Comment out the following line to see a lag in the ui
        mythread = new myCompThread();
        mythread.start();
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
        //Uncomment the following to see a lag in the UI
        //heavyComp();
        //Comment out the following line to see a lag in the ui
        mythread = new myCompThread();
        mythread.start();

        RadioButton rightButton = (RadioButton) findViewById(R.id.rb0);
        RadioButton midButton = (RadioButton) findViewById(R.id.rb1);
        RadioButton leftButton = (RadioButton) findViewById(R.id.rb2);
        counter=0;
        rightButton.setChecked(false);
        midButton.setChecked(false);
        leftButton.setChecked(false);
    }

}