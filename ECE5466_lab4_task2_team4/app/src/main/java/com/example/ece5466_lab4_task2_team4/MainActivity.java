package com.example.ece5466_lab4_task2_team4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ArrayList<RadioButton> buttonlist = new ArrayList<>(Arrays.asList((RadioButton)findViewById(R.id.radioButton),
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonlist = new ArrayList<>();
        buttonlist.add((RadioButton)findViewById(R.id.radioButton));

    }



}