package com.wrapp.example.floatlabelededittext;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wrapp.floatlabelededittext.FloatLabeledEditText;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatLabeledEditText labelEt = (FloatLabeledEditText) findViewById(R.id.labeledEditText);

        findViewById(R.id.buttonValid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelEt.setValid();
            }
        });

        findViewById(R.id.buttonPassif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelEt.setPassive();
            }
        });

        findViewById(R.id.buttonError).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelEt.setError();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
