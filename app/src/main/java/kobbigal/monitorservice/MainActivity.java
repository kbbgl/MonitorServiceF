package kobbigal.monitorservice;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
View.OnClickListener{

    private static final int RC_SIGN_IN = 0;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private GoogleApiClient mGoogleApiClient;
    SignInButton googleSignInBtn;
    ImageView profilePicIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(this, MonitorService.class);
        final Intent barchart = new Intent(this, BarChartActivity.class);
        findViewById(R.id.google_sigin_btn).setOnClickListener(this);
        profilePicIv = findViewById(R.id.user_profile_image);
        googleSignInBtn = findViewById(R.id.google_sigin_btn);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();


        Button startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //startService(intent);
                startActivity(barchart);

                Log.i("startservice", "y");

            }
        });

        Button stopBtn = findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(intent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("onConnected", bundle + "");
        mShouldResolve = false;
        String info = "";
        String personPhoto = "";

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null){

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            personPhoto = currentPerson.getImage().getUrl();
            String personGooglePlusProfile  = currentPerson.getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            info = personName + "\n" + email;

        }

        Toast.makeText(this, "Signed in as:\n" + info, Toast.LENGTH_SHORT).show();

        if (personPhoto != null) {
            Picasso.with(this).load(personPhoto).resize(400, 400).into(profilePicIv);
        }

        googleSignInBtn.animate().alpha(0f).setDuration(2000);
        if (googleSignInBtn.getAlpha() == 0f){
            googleSignInBtn.setVisibility(View.GONE);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("connection_failed", "onConnectionFailed: " + connectionResult);

        if (!mIsResolving && mShouldResolve){
            if (connectionResult.hasResolution()){
                try{
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e){
                    Log.e("connection_failed", "Could not resolve connection . ", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                Log.d("connection_failed", "onConnectionFailed: " + connectionResult);
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.google_sigin_btn){
            onSignInClicked();
        }


    }

    private void onSignInClicked() {

        mShouldResolve = true;
        mGoogleApiClient.connect();

        Toast.makeText(this, "Signing in..", Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("onActivityResult", requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode != RESULT_OK){
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();

        }

    }
}
