package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.itachi1706.helperlib.helpers.LogHelper;

public class FirebaseLoginActivity extends AppCompatActivity {

    private ProgressBar progress;
    private TextView acctView;
    private Button signOut, debugAcctBtn;
    private SignInButton mEmailSignInButton;
    private static final String TAG = "FirebaseLogin";
    private static final String FIREBASE_UID = "firebase_uid";

    private GoogleSignInClient mGoogleClient;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;

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
        signOut = findViewById(R.id.sign_out);
        if (getIntent().hasExtra("logout") && getIntent().getBooleanExtra("logout", false)) {
            mAuth.signOut();
            updateUI(null, true);
        }
        signOut.setOnClickListener(v -> {
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
            progress.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleClient.getSignInIntent();
            googleSignInIntent.launch(signInIntent);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleClient = GoogleSignIn.getClient(this, gso);

        debugAcctBtn = findViewById(R.id.test_account);

        if (BuildConfig.DEBUG)
            debugAcctBtn.setVisibility(View.VISIBLE);

        debugAcctBtn.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword("test@test.com", "test123").addOnCompleteListener(task -> processSignIn("TestEmail", task));
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    ActivityResultLauncher<Intent> googleSignInIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    LogHelper.d(TAG, "Sign In Result: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount acct = task.getResult();
                        firebaseAuthWithGoogle(acct);
                    } else {
                        // Signed out, show unauthenticated UI.
                        updateUI(null);
                    }
                }
            });

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        LogHelper.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progress.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> processSignIn("WithGoogle", task));
    }

    private void processSignIn(String provider, Task<AuthResult> task) {
        if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            LogHelper.d(TAG, "signIn" + provider + ":success");
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user, true);
        } else {
            // If sign in fails, display a message to the user.
            if (task.getException() != null) {
                LogHelper.w(TAG, "signIn" + provider + ":failure", task.getException());
            } else {
                LogHelper.w(TAG, "signIn" + provider + ":failure");
            }

            Toast.makeText(getApplicationContext(), "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        progress.setVisibility(View.GONE);
    }

    private void updateUI(FirebaseUser user) {
        updateUI(user, false);
    }

    private void updateUI(FirebaseUser user, boolean returnActivity) {
        progress.setVisibility(View.GONE);
        if (user != null) {
            // There's a user
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            sp.edit().putString(FIREBASE_UID, user.getUid()).apply();
            acctView.setText(getString(R.string.signed_in_as, user.getEmail()));
            if (BuildConfig.DEBUG)
                debugAcctBtn.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Currently Logged Out", Toast.LENGTH_SHORT).show();
            if (sp.contains(FIREBASE_UID)) sp.edit().remove(FIREBASE_UID).apply();
            acctView.setText(R.string.not_signed_in);
            if (BuildConfig.DEBUG)
                debugAcctBtn.setVisibility(View.VISIBLE);
            mEmailSignInButton.setVisibility(View.VISIBLE);
            signOut.setVisibility(View.GONE);
        }
        if (returnActivity) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
