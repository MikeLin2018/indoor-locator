package com.locateme.indoor_locator;

import android.app.DownloadManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class User extends HttpGetRequest{
    private String email;
    private String password;
    private String fname;
    private static final String LOGIN = "user/verify";

    public User(String email, String password, String fname){
        this.email = email;
        this.password = password;
        this.fname = fname;
    }

    public String getFname(){
        return this.fname;
    }
    public void setFname(String fname){
        this.fname = fname;
    }
    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }
}
