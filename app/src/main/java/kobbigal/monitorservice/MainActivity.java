package kobbigal.monitorservice;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
View.OnClickListener{

    private static final int RC_SIGN_IN = 0;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private GoogleApiClient mGoogleApiClient;
    SignInButton googleSignInBtn;
    CircleImageView profilePicIv;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_custom_view);

        final Intent intent = new Intent(this, MonitorService.class);
        final Intent barchart = new Intent(this, BarChartActivity.class);
        final Intent monitorServiceStartIntent = new Intent(this, MonitorService.class);
        findViewById(R.id.google_sigin_btn).setOnClickListener(this);
        profilePicIv = findViewById(R.id.user_profile_image);
        googleSignInBtn = findViewById(R.id.google_sigin_btn);
        username = findViewById(R.id.user_profile_name);

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

                startService(monitorServiceStartIntent);

                Log.i("service", "started");

            }
        });

        Button seeData = findViewById(R.id.see_graphs);
        seeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(barchart);
                Log.i("seedata", "launched");

            }
        });

        Button stopBtn = findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(intent);
                Log.i("service", "stopped");

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
        String personName = "";

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null){

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            personPhoto = currentPerson.getImage().getUrl();
            String personGooglePlusProfile  = currentPerson.getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            info = personName + "\n" + email;

        }

        Toast.makeText(this, "Signed in as:\n" + info, Toast.LENGTH_SHORT).show();
        username.setText(personName);


        if (personPhoto != null) {
            Picasso.with(this).load(personPhoto).resize(72, 72).into(profilePicIv);
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
