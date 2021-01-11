package com.cake.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cake.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneN, codi;
    private Button cont,verified;
    private CountryCodePicker ccp;
    private RelativeLayout cods;
    private String checker = "", phoneNumbers="";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerifyId;
    private PhoneAuthProvider.ForceResendingToken mForceResendToken;
    private ProgressDialog pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        pg = new ProgressDialog(this);
        //calling ids from views
        phoneN = findViewById(R.id.telephoneNumber);
        cont = findViewById(R.id.cont);
        verified = findViewById(R.id.verified);
        ccp = findViewById(R.id.ccp);
        cods = findViewById(R.id.veri);
        codi = findViewById(R.id.codeSent);

        ccp.registerCarrierNumberEditText(phoneN);
        verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCodes = codi.getText().toString();
                if (verificationCodes.equals("")){
                    FancyToast.makeText(LoginActivity.this,"Input code !",FancyToast.LENGTH_LONG,FancyToast.ERROR,true);
                }else {
                    pg.setTitle("Authenticating code");
                    pg.setMessage("please wait");
                    pg.setCanceledOnTouchOutside(false);
                    pg.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerifyId,verificationCodes);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumbers=ccp.getFullNumberWithPlus();
                if (!phoneNumbers.equals("")){
                    pg.setTitle("Sending code");
                    pg.setMessage("sending code wait");
                    pg.setCanceledOnTouchOutside(false);
                    pg.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumbers)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }else {
                    FancyToast.makeText(LoginActivity.this,"Hello World !",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true);
                }
            }
        });
        mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                FancyToast.makeText(LoginActivity.this,"Invalid",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true);
                cods.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerifyId = s;
                mForceResendToken = forceResendingToken;

                pg.dismiss();
                verified.setText("submit");
                cods.setVisibility(View.VISIBLE);
                FancyToast.makeText(LoginActivity.this,"Code sent !",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pg.dismiss();
                            FancyToast.makeText(LoginActivity.this,"Welcome",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true);
                            sendUserToMainActivity();
                        } else {
                            pg.dismiss();
                            String e =task.getException().toString();
                            FancyToast.makeText(LoginActivity.this,"Error: " + e ,FancyToast.LENGTH_LONG,FancyToast.ERROR,true);
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}