package com.mwb.raymenudemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RayMenu menu = (RayMenu) findViewById(R.id.ray_menu);
        menu.setOnMenuItemClickListener(new RayMenu.OnMenuItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Toast.makeText(MainActivity.this, "pos = " + pos, Toast.LENGTH_SHORT).show();
            }
        });
    }
}