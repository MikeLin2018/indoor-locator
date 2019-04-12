package com.locateme.indoor_locator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * DialogFragment that gives user login error.
 *
 * Created by adamcchampion on 2017/08/04.
 */

public class LoginErrorDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String s = bundle.toString();
        return new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(bundle.getString("errLogin_msg"))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
    }
}
