package com.example.devicemanagement;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    int downSpeed, upSpeed;

ListView listMsg;
    Button btn, btnSignal, btnMessage, refresh;

    private TextView batteryTxt, locTxt, DownLink, UpLink, Message,lvMsg;
    float batteryPct;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level * 100 / (float) scale;
            batteryTxt.setText("Battery :" + String.valueOf(batteryPct) + "%");
        }
    };


    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;


    private int signal = 0;


    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    private static MainActivity inst;
    public static MainActivity instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        code();

        refresh = findViewById(R.id.btnRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code();
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void code() {

        NetQuality(MainActivity.this);


        //Battery
        btn = findViewById(R.id.showBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Battery :" + batteryPct + "%", Toast.LENGTH_SHORT).show();
            }
        });
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryTxt = (TextView) this.findViewById(R.id.batteryTxt);


        //Signal
        final TextView textView = (TextView) findViewById(R.id.wifitext);
        System.out.println("Signal strength is : " + signal);

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        System.out.println(linkSpeed);
        if (linkSpeed >= -55) {
            textView.setText("Good|Range -30 -60|"+linkSpeed);
        } else if (linkSpeed >= -70) {
            textView.setText("Moderate|Range -60 -90|"+linkSpeed);
        } else if (linkSpeed >= -80) {
            textView.setText("Poor|Range -90 -120|"+linkSpeed);
        } else if (linkSpeed >= -100) {
            textView.setText("V.Poor|Range -120 -150|"+linkSpeed);
        } else {
            textView.setText("No Connection");
        }

        System.out.println("wifi" + linkSpeed);


        btnSignal = findViewById(R.id.btnSig);
        btnSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showStrength(view);
            }
        });


        //Location
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        Button btnLocation = (Button) findViewById(R.id.btnLoc);


        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationTrack = new LocationTrack(MainActivity.this);


                if (locationTrack.canGetLocation()) {


                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();

                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                } else {

                    locationTrack.showSettingsAlert();
                }

            }
        });

        LocationTrack locationTrack2 = new LocationTrack(MainActivity.this);
        double longitude2 = locationTrack2.getLongitude();
        double latitude2 = locationTrack2.getLatitude();

        locTxt = findViewById(R.id.txtLocation);
        locTxt.setText("Longitude:" + Double.toString(longitude2) + "\nLatitude:" + Double.toString(latitude2));


        // MESSEGE


        listMsg = (ListView) findViewById(R.id.msgList);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        listMsg.setAdapter(arrayAdapter);


        // Add SMS Read Permision At Runtime
        // Todo : If Permission Is Not GRANTED
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {

            // Todo : If Permission Granted Then Show SMS
            refreshSmsInbox();

        } else {
            // Todo : Then Set Permission
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        btnMessage = findViewById(R.id.btnMsg);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message = findViewById(R.id.txtMsg);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                {Manifest.permission.READ_SMS},
                        PackageManager.PERMISSION_GRANTED);

               Read_SMS(view);

            }
        });

    }


    //Signal Methods
    public void showStrength(View v) {
        TextView textView2 = (TextView) findViewById(R.id.text);



        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        int simState = telephonyManager.getSimState();
        if(simState == 5){
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength strength) {
                super.onSignalStrengthsChanged(strength);

                if (strength.isGsm()) {
                    String[] parts = strength.toString().split(" ");
                    int currentStrength = strength.getGsmSignalStrength();
                    if (currentStrength >= 18 && currentStrength <= 100) {
                        textView2.setText("Good | Range 95-100 | "+currentStrength);
                    } else if (currentStrength >= 6 && currentStrength <= 17) {
                        textView2.setText("Moderate  Range 85-94 | "+currentStrength);
                    } else if (currentStrength >= 1 && currentStrength <= 5) {
                        textView2.setText("Poor  Range 70-84 | "+currentStrength);
                    } else {
                        textView2.setText("No Connection");
                    }

                    System.out.println("sim" + currentStrength);
                }
//signal = (2 * signal) - 113;

            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
}
else{
    textView2.setText("No Sim Card");
}

    }


    //Location Methods

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String) perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        locationTrack.stopListener();
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void NetQuality(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        downSpeed = nc.getLinkDownstreamBandwidthKbps()/10;
        upSpeed = nc.getLinkUpstreamBandwidthKbps()/10;

        DownLink = findViewById(R.id.DnLink);
        DownLink.setText(" " + downSpeed + "kbps");

        UpLink = findViewById(R.id.UpLink);
        UpLink.setText(" " + upSpeed + "kbps");
    }

    public void Read_SMS(View view) {
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, "address = ?", new String[]{"+923363598868"}, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");



            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
            arrayAdapter.clear();
            do {
                String str = "SMS From: " + smsInboxCursor.getString(indexAddress)+
                        "\n" + smsInboxCursor.getString(indexBody) + "\n";
                arrayAdapter.add(str);
            } while (smsInboxCursor.moveToNext());

    }
        public void updateList ( final String smsMessage){
            arrayAdapter.insert(smsMessage, 0);
            arrayAdapter.notifyDataSetChanged();
        }

    }


