package com.sio.pa;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    private TextView phoneLabel;
    private Button submit;
    private EditText phone;
    private TextView otpLabel;
    private Button submitCode;
    private EditText otp;
    private CountryCodePicker ccp;

    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    boolean mVerificationInProgress= false;

    String mVerificationId= "";
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submit= (Button) findViewById(R.id.submit);
        phone= (EditText) findViewById(R.id.phoneNumber);
        phoneLabel= (TextView) findViewById(R.id.phoneLabel);
        submitCode= (Button) findViewById(R.id.submitOTP);
        otp= (EditText) findViewById(R.id.otp);
        otpLabel= (TextView) findViewById(R.id.codeLabel);
        ccp= (CountryCodePicker) findViewById(R.id.ccp);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:" + credential);

                Toast.makeText(MainActivity.this, "Verification Successful", Toast.LENGTH_LONG);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w(TAG, "onVerificationFailed", e);

                Toast.makeText(MainActivity.this, "Verification Failed", Toast.LENGTH_LONG).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Toast.makeText(MainActivity.this, "Invalid Phone Number", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(MainActivity.this, "Quota Overload", Toast.LENGTH_LONG).show();
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(MainActivity.this, "Verification Code Sent", Toast.LENGTH_LONG).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                phone.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
                phoneLabel.setVisibility(View.GONE);
                ccp.setVisibility(View.GONE);

                otp.setVisibility(View.VISIBLE);
                otpLabel.setVisibility(View.VISIBLE);
                submitCode.setVisibility(View.VISIBLE);


                // ...
            }
        };

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        ccp.getSelectedCountryCode().toString().concat(phone.getText().toString()),
                        60,
                        TimeUnit.SECONDS,
                        MainActivity.this,
                        mCallbacks
                );        // OnVerificationStateChangedCallbacks
            }
        });

        submitCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Verified", Toast.LENGTH_LONG).show();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(MainActivity.this, "Verification Failed: Invalid Code", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
