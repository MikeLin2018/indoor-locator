package com.github.pwittchen.reactivewifi.app;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ApDataScanActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ap_data_scan);
    Button btn = findViewById(R.id.button2);
    btn.setOnClickListener(
            new Button.OnClickListener(){
              public void onClick(View v){
                Intent intent = new Intent(v.getContext(), ApDataScan.class);
                startActivity(intent);
              }

            });
  }
}