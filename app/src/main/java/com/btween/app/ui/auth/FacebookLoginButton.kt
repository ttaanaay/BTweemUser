package com.btween.app.ui.auth

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Uses LoginManager's ActivityResultRegistryOwner overload (SDK v13+) rather than the
 * classic onActivityResult wiring - the modern API plays nicely with a single-Activity
 * Compose app without needing MainActivity to override onActivityResult itself.
 */
@Composable
fun FacebookLoginButton(
    onAccessTokenReceived: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val callbackManager = remember { CallbackManager.Factory.create() }

    DisposableEffect(callbackManager) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                onAccessTokenReceived(result.accessToken.token)
            }

            override fun onCancel() {
                onAccessTokenReceived(null)
            }

            override fun onError(error: FacebookException) {
                onAccessTokenReceived(null)
            }
        }
        LoginManager.getInstance().registerCallback(callbackManager, callback)
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    OutlinedButton(
        onClick = {
            val registryOwner = context as? ActivityResultRegistryOwner
            if (registryOwner != null) {
                LoginManager.getInstance()
                    .logIn(registryOwner, callbackManager, listOf("public_profile", "email"))
            } else {
                onAccessTokenReceived(null)
            }
        },
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text("Continue with Facebook")
    }
}
