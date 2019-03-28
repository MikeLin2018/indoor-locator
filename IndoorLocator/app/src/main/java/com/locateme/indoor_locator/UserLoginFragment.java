package com.locateme.indoor_locator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UserLoginFragment extends Fragment {

    private TextView email;
    private EditText emailEntered;
    private TextView password;
    private EditText passwordEntered;
    Intent in;

    private final String TAG = "LOGIN";

    private Button login;
    private Button signup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_login, container, false);
        email = (TextView) v.findViewById(R.id.emailTextView);
        emailEntered = (EditText) v.findViewById(R.id.editText);
        password = (TextView) v.findViewById(R.id.passwordTextView);
        passwordEntered = (EditText) v.findViewById(R.id.editText2);
        login = (Button) v.findViewById(R.id.loginButton);
        signup = (Button) v.findViewById(R.id.signupButton);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login();
                //Create a new user with first name set to empty string.  Will set upon confirming login
                User u = new User(emailEntered.getText().toString(), passwordEntered.getText().toString(), "");
                Log.d(TAG, "email: " + u.getEmail());
                Log.d(TAG, "password: " + u.getPassword());

                KeyValueDB.setEmail(getActivity(), u.getEmail());

                in = new Intent(getActivity(), HomeActivity.class);
                startActivity(in);

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login();
                //Create a new user with first name set to empty string.  Will set upon confirming login
                User u = new User(emailEntered.getText().toString(), passwordEntered.getText().toString(), "");
                Log.d(TAG, "email: " + u.getEmail());
                Log.d(TAG, "password: " + u.getPassword());

                KeyValueDB.setEmail(getActivity(), u.getEmail());

                in = new Intent(getActivity(), HomeActivity.class);
                startActivity(in);

            }
        });

        return v;
    }


}