package com.itachi1706.busarrivalsg

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.itachi1706.busarrivalsg.databinding.ActivityFirebaseLoginBinding
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.w
import kotlinx.coroutines.launch
import java.security.SecureRandom

class FirebaseLoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FirebaseLogin"
        private const val FIREBASE_UID = "firebase_uid"
    }

    private var credentialManager: CredentialManager? = null
    private var mAuth: FirebaseAuth? = null
    private var sp: SharedPreferences? = null

    private lateinit var binding: ActivityFirebaseLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseLoginBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding.root, this)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configure login form
        mAuth = FirebaseAuth.getInstance()
        if (intent.hasExtra("logout") && intent.getBooleanExtra("logout", false)) {
            mAuth?.signOut()
            updateUI(null, true)
        }
        binding.signOut.setOnClickListener {
            mAuth?.signOut()
            updateUI(null)
        }
        binding.signInProgress.let {
            it.isIndeterminate = true
            it.visibility = View.GONE
        }
        credentialManager = CredentialManager.create(this)

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        binding.emailSignInButton.setOnClickListener {
            // Attempt to sign in with Google
            binding.signInProgress.visibility = View.VISIBLE

            // Generate a random nonce
            val nonce = generateSecureNonce()
            val googleidOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleidOption)
                .build()

            if (credentialManager == null) {
                Log.e(TAG, "Credential Manager is null, cannot proceed")

                Snackbar.make(
                    binding.root,
                    "An error occurred with the app. Are you on a Google certified device? If so, please restart the app and try again",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
                lifecycleScope.launch {
                    try {
                        val result = credentialManager?.getCredential(this@FirebaseLoginActivity, request)
                        handleGoogleSignIn(result?.credential)
                    } catch (e: GetCredentialException) {
                        Log.e(TAG, "Unable to login user due to ${e.message}")
                        Snackbar.make(
                            binding.root,
                            "An error occurred signing in with Google: ${e.localizedMessage}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } catch (e: NoCredentialException) {
                        Log.e(TAG, "No credentials found: ${e.message}")
                        Snackbar.make(
                            binding.root,
                            "Unable to login. No Google Credentials found on device",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            binding.testAccount.let {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    binding.signInProgress.visibility = View.VISIBLE
                    mAuth?.signInWithEmailAndPassword("test@test.com", "test123")?.addOnCompleteListener { task -> processSignIn("TestEmail", task) }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        updateUI(currentUser)
    }

    private fun generateSecureNonce(size: Int = 32): String {
        val nonce = ByteArray(size)
        SecureRandom().nextBytes(nonce)
        return Base64.encodeToString(nonce, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun handleGoogleSignIn(credential: Credential?) {
        if (credential == null) {
            Log.e(TAG, "Credentials not found")
            return
        }
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Get google id token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.e(TAG, "Unexpected credential type: ${credential.type}")
            updateUI(null)
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val idTokenLast8 = if (idToken.length >= 8) "xxx${idToken.takeLast(8)}" else idToken
        d(TAG, "firebaseAuthWithGoogle: $idTokenLast8")
        binding.signInProgress.visibility = View.VISIBLE

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task -> processSignIn("WithGoogle", task) }
    }

    private fun processSignIn(provider: String, task: Task<AuthResult>) {
        if (task.isSuccessful) {
            // Sign in success, update UI with the signed-in user's information
            d(TAG, "signIn$provider:success")
            val user = mAuth?.currentUser
            updateUI(user, true)
        } else {
            // If sign in fails, display a message to the user.
            if (task.exception != null) {
                w(TAG, "signIn$provider:failure", task.exception!!)
            } else {
                w(TAG, "signIn$provider:failure - unknown exception")
            }

            Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            updateUI(null)
        }
    }

    private fun updateUI(user: FirebaseUser?, returnActivity: Boolean = false) {
        binding.signInProgress.visibility = View.GONE
        if (user != null) {
            // User exists
            Toast.makeText(this, "Signed in as ${user.email}", Toast.LENGTH_SHORT).show()
            sp?.edit { putString(FIREBASE_UID, user.uid) }
            binding.signInAs.text = getString(R.string.signed_in_as, user.email)
            if (BuildConfig.DEBUG)
                binding.testAccount.visibility = View.GONE
            binding.emailSignInButton.visibility = View.GONE
            binding.signOut.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Currently Logged Out", Toast.LENGTH_SHORT).show()
            sp?.edit { remove(FIREBASE_UID) }
            binding.signInAs.setText(R.string.not_signed_in)
            if (BuildConfig.DEBUG)
                binding.testAccount.visibility = View.VISIBLE
            binding.emailSignInButton.visibility = View.VISIBLE
            binding.signOut.visibility = View.GONE
        }

        if (returnActivity) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(RESULT_CANCELED)
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}