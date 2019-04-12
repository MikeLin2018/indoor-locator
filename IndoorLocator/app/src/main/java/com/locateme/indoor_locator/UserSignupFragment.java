package com.locateme.indoor_locator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

public class UserSignupFragment extends Fragment {
    private EditText emailEntered;
    private EditText usernameEntered;
    private EditText passwordEntered;
    private EditText confirmPasswordEntered;
    private Button signup;
    private TextView errorMessage;
    private OkHttpClient client = new OkHttpClient();
    private final String TAG = "SIGNUP";

    Intent in;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_signup, container, false);
        usernameEntered = (EditText) v.findViewById(R.id.signup_username);
        emailEntered = (EditText) v.findViewById(R.id.signup_email);
        passwordEntered = (EditText) v.findViewById(R.id.signup_password);
        confirmPasswordEntered= (EditText) v.findViewById(R.id.signup_confirmPassword);


        signup = (Button) v.findViewById(R.id.signupButton);
        errorMessage = (TextView) v.findViewById(R.id.errorMessage2);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login();
                //Create a new user with first name set to empty string.  Will set upon confirming login
                User u = new User(emailEntered.getText().toString(), passwordEntered.getText().toString(), usernameEntered.getText().toString());
                Log.d(TAG,"email: " + u.getEmail());
                Log.d(TAG,"password: " + u.getPassword());
                Log.d(TAG,"username: " + u.getFname());

                if(u.getPassword().length() == 0 || u.getEmail().length() == 0 || u.getFname().length() == 0){
                    errorMessage.setText(R.string.field_empty);
//                    errorMessage.setVisibility(View.VISIBLE);
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "Filed is empty");
                    FragmentManager manager = getFragmentManager();
                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();
                    fragment.setArguments(bundl);
                    if (manager != null) {
                        fragment.show(manager, "login_error");
                    }
                    return;
                }
                if (!validEmailForm(u.getEmail())) {
                    errorMessage.setText(R.string.error_invalid_email);
//                    errorMessage.setVisibility(View.VISIBLE);
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "Email must be of the form 'email@example.com");
                    FragmentManager manager = getFragmentManager();
                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();
                    fragment.setArguments(bundl);
                    if (manager != null) {
                        fragment.show(manager, "login_error");
                    }
                    return;
                }
                if(!u.getPassword().equals(confirmPasswordEntered.getText().toString())){
                    errorMessage.setText(R.string.error_invalid_password);
//                    errorMessage.setVisibility(View.VISIBLE);
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "Passwords did not  match");
                    FragmentManager manager = getFragmentManager();
                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();
                    fragment.setArguments(bundl);
                    if (manager != null) {
                        fragment.show(manager, "login_error");
                    }
                    return;
                }
                if (!validPasswordForm(u.getPassword())) {
                    errorMessage.setText(R.string.error_invalid_password);
//                    errorMessage.setVisibility(View.VISIBLE);
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "1) Password much contain > 5 characters 2) Atleast one numeric character");
                    FragmentManager manager = getFragmentManager();
                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();
                    fragment.setArguments(bundl);
                    if (manager != null) {
                        fragment.show(manager, "login_error");
                    }
                    return;
                }
                signup(u);
            }
        });
        return v;
    }
    public boolean validEmailForm(String email){
        //must have @ and .
        //@ is before . with at least one character in between
        //@ is not first character . is not last character
        int atIndex = email.indexOf('@');
        int dotIndex = email.indexOf('.', atIndex+2);
        //Log.d(TAG,dotIndex+"");
        //Log.d(TAG,atIndex+"");
        return (atIndex > 0) && (dotIndex != -1) && (dotIndex < email.length()-1);
    }

    public static boolean validPasswordForm(String password) {
        int charCount = 0;
        int numCount = 0;
        if (password.length() < 6)
            return false;
        else
            return true;
//        for (int i = 0; i < password.length(); i++) {
//            char ch = password.charAt(i);
//            if (is_Numeric(ch)) numCount++;
//            else if (is_Letter(ch)) charCount++;
//            else return false;
//        }
//        return (charCount >= 2 && numCount >= 2);
    }

    public static boolean is_Letter(char ch) {
        ch = Character.toUpperCase(ch);
        return (ch >= 'A' && ch <= 'Z');
    }

    public static boolean is_Numeric(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    public void signup(User u){
        final String mPass = u.getPassword();
        //Set up post body with provided password and email
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", u.getFname())
                .addFormDataPart("password", u.getPassword())
                .addFormDataPart("email", u.getEmail())
                .build();

        //Send HTTP Request to new/user resource
        Request request = new Request.Builder()
                .url(getString(R.string.create_user_URL))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //On failure log error
                call.cancel();
                Log.d(TAG, "Create new user call failure");
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
                                    String mName = data.getString("name");
                                    String mEmail = data.getString("email");
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Log.d(TAG,message.getString(0));

                                    Activity a = getActivity();
                                    KeyValueDB.setEmail(a, mEmail);
                                    KeyValueDB.setUserId(a, mID);
                                    KeyValueDB.setName(a, mName);
                                    KeyValueDB.setPassword(a, mPass);
                                    in = new Intent(getActivity(),HomeActivity.class);
                                    startActivity(in);
                                } else {
                                    // If server response "fail"
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Log.d(TAG, message.getString(0));

                                    // Tell user no User exists matching credentials provided
//                                    errorMessage.setText("User not created");
//                                    errorMessage.setVisibility(View.VISIBLE);
                                    Bundle bundl = new Bundle();
                                    bundl.putString("errLogin_msg", message.toString());
                                    FragmentManager manager = getFragmentManager();
                                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();

                                    fragment.setArguments(bundl);
                                    if (manager != null) {
                                        fragment.show(manager, "login_error");
                                    }
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
