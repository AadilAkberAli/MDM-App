//package com.example.devicemanagement;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.graphics.drawable.ColorDrawable;
//import android.telephony.SmsManager;
//import android.view.Menu;
//import android.view.View;
//import android.view.Window;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class ViewDialog extends MainActivity {
//
//
//
//
//    public void showDialog(Activity activity, String msg){
//        final Dialog dialog = new Dialog(activity);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.custom_dialogbox_otp);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//
//
//        Button dialogBtn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
//        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                    Toast.makeText(getApplicationContext(),"Cancel" ,Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//
//        Button dialogBtn_okay = (Button) dialog.findViewById(R.id.btn_okay);
//        dialogBtn_okay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String num= mobileno.getText().toString();
//                String mesge=message.getText().toString();
//                sendSMS(num,mesge);
//            //                    Toast.makeText(getApplicationContext(),"Okay" ,Toast.LENGTH_SHORT).show();
//                dialog.cancel();
//            }
//        });
//
//        dialog.show();
//    }
//    public void sendSMS(String phoneNo, String msg) {
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
//            Toast.makeText(getApplicationContext(), "Message Sent",
//                    Toast.LENGTH_LONG).show();
//        } catch (Exception ex) {
//            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
//                    Toast.LENGTH_LONG).show();
//            ex.printStackTrace();
//        }
//    }
//}