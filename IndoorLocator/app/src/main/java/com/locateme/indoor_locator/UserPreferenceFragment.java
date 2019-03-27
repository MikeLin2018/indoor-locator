package com.locateme.indoor_locator;

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


public class UserPreferenceFragment extends Fragment {

    private Button applyChanges;
    private Button cancel;
    private EditText oldPass;
    private EditText newPass;
    private EditText confirmPass;
    private TextView errorMessage;

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
                updatePassword(newP);
                goHome();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
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
    public void updatePassword(String newPass){
        KeyValueDB.setPassword(getContext(),newPass);
        //TODO: update database to have new password stored
    }
    public void goHome(){
        Intent in = new Intent(getActivity(),HomeActivity.class);
        startActivity(in);
    }


}
