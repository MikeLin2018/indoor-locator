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


public class UserLoginFragment extends Fragment {

    private TextView email;
    private EditText emailEntered;
    private TextView password;
    private EditText passwordEntered;
    private TextView errorMessage;
    private OkHttpClient client = new OkHttpClient();
    Intent in;
    private final String TAG = "LOGIN";
    private Button login;
    private Button signup;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Hide the navigation bar
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_login, container, false);
        email = (TextView) v.findViewById(R.id.emailTextView);
        emailEntered = (EditText) v.findViewById(R.id.editText);
        password = (TextView) v.findViewById(R.id.passwordTextView);
        passwordEntered = (EditText) v.findViewById(R.id.editText2);
        login = (Button) v.findViewById(R.id.loginButton);
        signup = (Button) v.findViewById(R.id.signupButton);
        errorMessage = (TextView) v.findViewById(R.id.errorMessage_login);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login();
                //Create a new user with first name set to empty string.  Will set upon confirming login
                User u = new User(emailEntered.getText().toString(), passwordEntered.getText().toString(), "");
                Log.d(TAG,"email: " + u.getEmail());
                Log.d(TAG,"password: " + u.getPassword());
                if(u.getPassword().length() == 0 || u.getEmail().length() == 0){
                    errorMessage.setText(R.string.field_empty);
                    //errorMessage.setVisibility(View.VISIBLE);
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "Field is Empty");
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
                    Bundle bundl = new Bundle();
                    bundl.putString("errLogin_msg", "Email must be of the form 'email@example.com");
                    FragmentManager manager = getFragmentManager();
                    LoginErrorDialogFragment fragment = new LoginErrorDialogFragment();

                    fragment.setArguments(bundl);
                    if (manager != null) {
                        fragment.show(manager, "login_error");
                    }
                    //errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                login(u);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in = new Intent(getActivity(),UserSignupActivity.class);
                startActivity(in);
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
        Log.d(TAG,dotIndex+"");
        Log.d(TAG,atIndex+"");
        return (atIndex > 0) && (dotIndex != -1) && (dotIndex < email.length()-1);
    }



    private void login(User u) {
        final String mPass = u.getPassword();
        //Set up post body with provided password and email
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("password", u.getPassword())
                .addFormDataPart("email", u.getEmail())
                .build();

        //Send HTTP Request to verify/user resource
        Request request = new Request.Builder()
                .url(getString(R.string.verify_user_URL))
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
                                    //                                    errorMessage.setText(message.getString(0));
//                                    errorMessage.setVisibility(View.VISIBLE);
                                    //Add dialoge
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
