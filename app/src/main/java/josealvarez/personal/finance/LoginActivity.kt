package josealvarez.personal.finance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {

    private val TAG = "LoginActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isLoading = mutableStateOf(false)

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i(TAG, "googleSignInLauncher: result received. Code: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "googleSignInLauncher: Google sign in successful, authenticating with Firebase...")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "googleSignInLauncher: Google sign in failed", e)
                showError("Google sign in failed: ${e.message}")
                isLoading.value = false
            }
        } else {
            Log.w(TAG, "googleSignInLauncher: Sign in cancelled or failed. ResultCode: ${result.resultCode}")
            isLoading.value = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: ACTIVITY STARTED")
        
        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        if (auth.currentUser != null) {
            Log.i(TAG, "onCreate: User already signed in: ${auth.currentUser?.email}")
            navigateToDashboard()
            return
        }

        // Configure Google Sign In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.i(TAG, "onCreate: GoogleSignInClient configured")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Failed to configure Google Sign In", e)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onSignInClick = { signIn() },
                        isLoading = isLoading.value
                    )
                }
            }
        }
    }

    private fun signIn() {
        Log.i(TAG, "signIn: Clicked. launching Google intent")
        isLoading.value = true
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.i(TAG, "firebaseAuthWithGoogle: Attempting Firebase auth")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "firebaseAuthWithGoogle: Firebase SUCCESS")
                    navigateToDashboard()
                } else {
                    Log.e(TAG, "firebaseAuthWithGoogle: Firebase FAILURE", task.exception)
                    showError("Firebase authentication failed: ${task.exception?.message}")
                    isLoading.value = false
                }
            }
    }

    private fun navigateToDashboard() {
        Log.i(TAG, "navigateToDashboard: Switching to DashboardActivity")
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Log.e(TAG, "USER ERROR: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun LoginScreen(onSignInClick: () -> Unit, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "LOGO", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Finance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Sign in with Google")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen(onSignInClick = {}, isLoading = false)
        }
    }
}
