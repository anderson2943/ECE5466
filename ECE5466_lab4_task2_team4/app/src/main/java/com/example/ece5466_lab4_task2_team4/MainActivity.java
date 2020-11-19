package com.example.ece5466_lab4_task2_team4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity {


    ArrayList<RadioButton> buttonlist;
    //final ArrayList<Integer> queue = new ArrayList<>();
    //blocking queue that will do things in FIFO order
    ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(50, true);

    Thread1 thread1;
    Thread2 thread2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonlist = new ArrayList<>(Arrays.asList((RadioButton)findViewById(R.id.radioButton),
                (RadioButton)findViewById(R.id.radioButton2),(RadioButton)findViewById(R.id.radioButton3),
                (RadioButton)findViewById(R.id.radioButton4), (RadioButton)findViewById(R.id.radioButton5),(RadioButton)findViewById(R.id.radioButton6),
                (RadioButton)findViewById(R.id.radioButton7),(RadioButton)findViewById(R.id.radioButton8),
                (RadioButton)findViewById(R.id.radioButton9),(RadioButton)findViewById(R.id.radioButton10),(RadioButton)findViewById(R.id.radioButton11),
                (RadioButton)findViewById(R.id.radioButton12),(RadioButton)findViewById(R.id.radioButton13),
                (RadioButton)findViewById(R.id.radioButton14), (RadioButton)findViewById(R.id.radioButton15),
                (RadioButton)findViewById(R.id.radioButton16), (RadioButton)findViewById(R.id.radioButton17),(RadioButton)findViewById(R.id.radioButton18),
                (RadioButton)findViewById(R.id.radioButton19),(RadioButton)findViewById(R.id.radioButton20),(RadioButton)findViewById(R.id.radioButton21),
                (RadioButton)findViewById(R.id.radioButton22),(RadioButton)findViewById(R.id.radioButton23),
                (RadioButton)findViewById(R.id.radioButton24), (RadioButton)findViewById(R.id.radioButton25), (RadioButton)findViewById(R.id.radioButton26),
                (RadioButton)findViewById(R.id.radioButton27),(RadioButton)findViewById(R.id.radioButton28),
                (RadioButton)findViewById(R.id.radioButton29),(RadioButton)findViewById(R.id.radioButton30),(RadioButton)findViewById(R.id.radioButton31),
                (RadioButton)findViewById(R.id.radioButton32),(RadioButton)findViewById(R.id.radioButton33),
                (RadioButton)findViewById(R.id.radioButton34), (RadioButton)findViewById(R.id.radioButton35),(RadioButton)findViewById(R.id.radioButton36),
                (RadioButton)findViewById(R.id.radioButton37),(RadioButton)findViewById(R.id.radioButton38),
                (RadioButton)findViewById(R.id.radioButton39),(RadioButton)findViewById(R.id.radioButton40),
                (RadioButton)findViewById(R.id.radioButton41),(RadioButton)findViewById(R.id.radioButton42),(RadioButton)findViewById(R.id.radioButton43),
                (RadioButton)findViewById(R.id.radioButton44), (RadioButton)findViewById(R.id.radioButton45),(RadioButton)findViewById(R.id.radioButton46),
                (RadioButton)findViewById(R.id.radioButton47),(RadioButton)findViewById(R.id.radioButton48),
                (RadioButton)findViewById(R.id.radioButton49),(RadioButton)findViewById(R.id.radioButton50)));
        thread1 = new Thread1();
        thread2 = new Thread2();
        thread1.start();
        thread2.start();
        UIUpdateThread uiUpdate = new UIUpdateThread();
        uiUpdate.start();
    }


    private class UIUpdateThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (buttonlist){
                            int index = queue.size();
                            Log.i("updateUI: ", "updating ui with index "+index);
                            for(int i=0; i<buttonlist.size();i++){
                                if(i<index){
                                    buttonlist.get(i).setChecked(true);
                                }else{
                                    buttonlist.get(i).setChecked(false);
                                }
                            }
                        }
                    }
                });
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //int queueSize = queue.size();
    }

    public void higher(View view){
        thread1.setPriority(Thread.MAX_PRIORITY);
        thread2.setPriority(Thread.MIN_PRIORITY);

    }

    public void lower(View view){
        thread1.setPriority(Thread.MIN_PRIORITY);
        thread2.setPriority(Thread.MAX_PRIORITY);
    }

    private class Thread1 extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                //Heavy comp from task 1
                int i = 0;
                Random random = new Random();
                while (i < 919999) {
                    double mynum = 5.51234 + random.nextInt(5000);
                    mynum = mynum * 512.2143 / 23554.0;
                    mynum = Math.sqrt(mynum * mynum) * 61;
                    double mynum2 = 23476325.345623 + random.nextInt(5000);
                    mynum = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    double mynum3 = 5.51234 + random.nextInt(5000);
                    mynum = mynum * 512.2143 / 23554.0;
                    mynum3 = Math.sqrt(mynum * mynum) * 61;
                    double mynum4 = 23476325.345623 + random.nextInt(5000);
                    mynum4 = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    double mynum5 = 5.51234 + random.nextInt(5000);
                    mynum = mynum5 * 512.2143 / 23554.0;
                    mynum5 = Math.sqrt(mynum * mynum) * 61;
                    double mynum6 = 23476325.345623 + random.nextInt(5000);
                    mynum6 = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    i++;
                }
                //enqueue a number
                Random rand = new Random();
                queue.add(rand.nextInt(10));
                Log.i("thread1: ", "enqueing number");
            }
        }


    };


    private class Thread2 extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                //Heavy comp from task 1
                int i = 0;
                Random random = new Random();
                while (i < 919999) {
                    double mynum = 5.51234 + random.nextInt(5000);
                    mynum = mynum * 512.2143 / 23554.0;
                    mynum = Math.sqrt(mynum * mynum) * 61;
                    double mynum2 = 23476325.345623 + random.nextInt(5000);
                    mynum = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    double mynum3 = 5.51234 + random.nextInt(5000);
                    mynum = mynum * 512.2143 / 23554.0;
                    mynum3 = Math.sqrt(mynum * mynum) * 61;
                    double mynum4 = 23476325.345623 + random.nextInt(5000);
                    mynum4 = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    double mynum5 = 5.51234 + random.nextInt(5000);
                    mynum = mynum5 * 512.2143 / 23554.0;
                    mynum5 = Math.sqrt(mynum * mynum) * 61;
                    double mynum6 = 23476325.345623 + random.nextInt(5000);
                    mynum6 = mynum2 * 5 / 23554.0;
                    mynum2 = Math.sqrt(mynum2 * mynum) * 61;
                    i++;
                }
                //dequeue a number
                try {
                    queue.take();
                    Log.i("thread2: ", "dequeing number");
                } catch (InterruptedException e) {
                    Log.e("dequeue: ", e.toString());
                }
            }
        }
    };




}