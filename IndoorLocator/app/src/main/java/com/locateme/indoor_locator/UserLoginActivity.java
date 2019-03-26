package com.locateme.indoor_locator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserLoginActivity extends SingleFragmentActivity{
    //private TextView userInfo;
    @Override
    protected Fragment createFragment(){
        return new UserLoginFragment();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(UserLoginActivity.this, UserLoginActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

        /*
            TEST FOR CONNECTIVITY
         */
    /*userInfo = findViewById(R.id.textView);
    userInfo.setText("changes");
    OkHttpClient client = new OkHttpClient();
    String url = "http://10.0.3.2:5000/building";

    Request request = new Request.Builder()
            .url(url)
            .build();
    client.newCall(request).enqueue(new Callback(){
        @Override
        public void onFailure(Call call, IOException e){
            Log.d("ERROR","call failed");
            e.printStackTrace();

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException{
            Log.d("ERROR","myResponse 1");

            if(response.isSuccessful()){
                final String myResponse = response.body().string();
                Log.d("ERROR",myResponse);

                UserLoginActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        userInfo.setText(myResponse);
                    }
                });
            }
        }
    });*/

}
