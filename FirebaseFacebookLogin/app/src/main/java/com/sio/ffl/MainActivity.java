package com.sio.ffl;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth auth;
    private Button logout;
    private TextView name;
    private ImageView displayPic;

    private static final int RC_SIGN_IN= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout= (Button) findViewById(R.id.logout);
        name= (TextView) findViewById(R.id.name);

        providers= new ArrayList<>();
        providers.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
        auth= FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            // already signed in
            Log.d("AUTH", auth.getCurrentUser().getEmail()+" logged in.");
        }else{
            // not signed in
            startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build(), RC_SIGN_IN);
        }

        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(v.getId() == R.id.logout){
                    AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("AUTH", "User logged out.");
                                    finish();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                // user logged in
                name.setText(auth.getCurrentUser().getDisplayName().toString());
                Profile profile= Profile.getCurrentProfile();
                new DownloadImage((ImageView) findViewById(R.id.displayPic)).execute(profile.getProfilePictureUri(400,400).toString());
                Log.d("AUTH", auth.getCurrentUser().getEmail()+"\n"+auth.getCurrentUser().getPhotoUrl().toString()+"\n"+profile.getName());
            }else{
                Log.d("AUTH", "Login Failed");
                finish();
            }
        }
    }
}
