package com.btween.app.ui.auth

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.btween.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

/**
 * Uses the classic (well-proven, still fully supported) GoogleSignInClient flow rather than
 * the newer Credential Manager API - fewer moving parts to configure correctly for a first
 * integration. The ID token it returns is what the backend verifies at /auth/oauth/google.
 */
@Composable
fun GoogleSignInButton(
    onIdTokenReceived: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)

    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            android.util.Log.w("GoogleSignIn", "Result not OK, resultCode=${result.resultCode}")
            onIdTokenReceived(null)
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            onIdTokenReceived(account.idToken)
        } catch (e: ApiException) {
            android.util.Log.e(
                "GoogleSignIn",
                "Sign-in failed, statusCode=${e.statusCode} (${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)})",
                e
            )
            onIdTokenReceived(null)
        }
    }

    OutlinedButton(
        onClick = {
            // Always show the account chooser rather than silently reusing a previous
            // session - simplest way to let someone switch Google accounts if they want to.
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        },
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text("Continue with Google")
    }
}
