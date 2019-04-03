package com.locateme.indoor_locator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;


public class UserPreferenceFragment extends Fragment {

    private Button applyChanges;
    private Button cancel;
    private EditText oldPass;
    private EditText newPass;
    private EditText confirmPass;
    private TextView errorMessage;
    private OkHttpClient client = new OkHttpClient();
    private Intent in;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_preference, container, false);
        oldPass = (EditText) v.findViewById(R.id.oldPassword);
        newPass = (EditText) v.findViewById(R.id.newPassword);
        confirmPass = (EditText) v.findViewById(R.id.confirmPassword);
        applyChanges = (Button) v.findViewById(R.id.apply);
        cancel = (Button) v.findViewById(R.id.cancel);
        errorMessage = (TextView) v.findViewById(R.id.errorMessage);

        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old = oldPass.getText().toString();
                String newP = newPass.getText().toString();
                String confirm = confirmPass.getText().toString();
                if(old.length() == 0 || newP.length() == 0 || confirm.length() == 0){
                    errorMessage.setText(R.string.field_empty);
                    errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                if(!correctOldPass(old)){
                    errorMessage.setText(R.string.wrong_oldpass);
                    errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                if(!newAndConfirmMatch(newP,confirm)){
                    errorMessage.setText(R.string.passwords_dont_match);
                    errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                updatePassword(newP, KeyValueDB.getEmail(getContext()));
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in = new Intent(getActivity(),HomeActivity.class);
                startActivity(in);
            }
        });
        return v;
    }
    public boolean correctOldPass(String pass){
        return KeyValueDB.getPassword(getActivity()).equals(pass);
    }
    public boolean newAndConfirmMatch(String s1, String s2){
        return s1.equals(s2);
    }
    public void updatePassword(String newPass, String userEmail){
        //Set up post body with provided password and email
        final String updatedPass = newPass;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("password", newPass)
                .addFormDataPart("email", userEmail)
                .build();

        //Send HTTP Request to new/user resource
        Request request = new Request.Builder()
                .url(getString(R.string.update_password_URL))
                .put(requestBody)
                .build();

            client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //On failure log error
                call.cancel();
                Log.d(TAG, "Change password call failure");
                Log.d(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Create and parse responseJSON
                final String responseText = response.body().string();
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(responseText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final JSONObject finalResponseJSON = responseJSON;

                // Update UI by JSON response
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseJSON != null) {
                            try {
                                if (finalResponseJSON.getBoolean("success")) {
                                    // If server resposne "success" get the users email and store in sharedPreferences
                                    JSONObject data = finalResponseJSON.getJSONObject("data");
                                    int mID = data.getInt("id");
                                    //String mName = data.getString("name");
                                    //String mEmail = data.getString("email");
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Log.d(TAG,message.getString(0));

                                    Activity a = getActivity();
                                    KeyValueDB.setPassword(getContext(),updatedPass);
                                    in = new Intent(getActivity(),HomeActivity.class);
                                    startActivity(in);
                                } else {
                                    // If server response "fail"
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Log.d(TAG, message.getString(0));

                                    // Tell user no User exists matching credentials provided
                                    errorMessage.setText("Password Not Updated");
                                    errorMessage.setVisibility(View.VISIBLE);
                                }
                                Log.d(TAG, responseText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

            }
        });
}


}
