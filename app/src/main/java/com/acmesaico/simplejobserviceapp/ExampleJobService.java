package com.acmesaico.simplejobserviceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class ExampleJobService extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "locations.txt";
    String readPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gpsinterval.txt";
    int interval;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        createFile();
        createReadFile();
        if (readData(getApplicationContext()).isEmpty()) {
            interval = 2000;
        } else {
            interval = Integer.parseInt(readData(getApplicationContext()));
        }
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
       /* new Thread(new Runnable() {
            @Override
            public void run() {
*/
//                Handler mHandler = new Handler(Looper.getMainLooper()) {
//                    @Override
//                    public void handleMessage(Message message) {
        getLocation();
//                    }
//                };
        if (jobCancelled) {
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "Job finished");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobFinished(params, false);
        }
    }

    /*  }).start();
  }
*/
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        jobCancelled = true;
        return true;
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String locationString = "" + latitude + "///" + longitude;

                writeData(locationString, getApplicationContext());

                Log.d(TAG, "" + latitude + " /// " + longitude);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, locationListener);

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void createFile() {
        File file = new File(path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("FILEPATH&&&&", path);
        }
    }

    private void createReadFile() {
        File file = new File(readPath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("READFILEPATH@@", readPath);
        }
    }

    private void writeData(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path));
            outputStreamWriter.append(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readData(Context context) {
        String result = "";
        try {
            InputStream inputStream = new FileInputStream(readPath);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }


                inputStream.close();
                result = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


}
