package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar progress;
    private TextView acctView;
    private Button signout, debugAcctBtn;
    private SignInButton mEmailSignInButton;
    private static final String TAG = "FirebaseLogin";

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_login);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up the login form.
        mAuth = FirebaseAuth.getInstance();
        acctView = findViewById(R.id.sign_in_as);
        signout = findViewById(R.id.sign_out);
        signout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI(null);
        });
        progress = findViewById(R.id.sign_in_progress);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(v -> {
            //Attempts to sign in with Google
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        debugAcctBtn = findViewById(R.id.test_account);

        if (BuildConfig.DEBUG)
            debugAcctBtn.setVisibility(View.VISIBLE);

        debugAcctBtn.setOnClickListener(v -> mAuth.signInWithEmailAndPassword("test@test.com", "test123").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInTestEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInTestEmail:failure", task.getException());
                Toast.makeText(getApplicationContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "Sign In Result:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                firebaseAuthWithGoogle(acct);
            } else {
                // Signed out, show unauthenticated UI.
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progress.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithGoogle:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithGoogle:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                    progress.setVisibility(View.GONE);
                });
    }

    private void updateUI(FirebaseUser user) {
        updateUI(user, false);
    }

    private void updateUI(FirebaseUser user, boolean returnActivity) {
        progress.setVisibility(View.GONE);
        if (user != null) {
            // There's a user
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            sp.edit().putString("firebase_uid", user.getUid()).apply();
            acctView.setText("Signed in as " + user.getEmail());
            if (BuildConfig.DEBUG)
                debugAcctBtn.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.GONE);
            signout.setVisibility(View.VISIBLE);
            if (returnActivity) {
                setResult(RESULT_OK);
                finish();
            }
        } else {
            Toast.makeText(this, "Currently Logged Out", Toast.LENGTH_SHORT).show();
            if (sp.contains("firebase_uid")) sp.edit().remove("firebase_uid").apply();
            acctView.setText("Not Signed In");
            if (BuildConfig.DEBUG)
                debugAcctBtn.setVisibility(View.VISIBLE);
            mEmailSignInButton.setVisibility(View.VISIBLE);
            signout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        if (mAuth.getCurrentUser() == null)
            new AlertDialog.Builder(this).setTitle("Unable to connect to Google Servers")
                    .setMessage("We are unable to connect to Google Servers to sign you in, therefore this utility cannot be used")
                    .setCancelable(false).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                setResult(RESULT_CANCELED);
                finish();
            }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
