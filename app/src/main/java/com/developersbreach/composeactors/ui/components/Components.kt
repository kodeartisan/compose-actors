package com.developersbreach.composeactors.ui.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.developersbreach.composeactors.R
import com.developersbreach.composeactors.utils.ApiKey
import com.developersbreach.composeactors.utils.NetworkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @param scaffoldState attach snackbar host state to the scaffold
 * @param scope Remember state of scaffold to manage snackbar
 * @param snackBarMessage message visible to user
 */
@Composable
private fun LaunchSnackBar(
    scaffoldState: ScaffoldState,
    snackBarMessage: String,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    LaunchedEffect(scope) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = snackBarMessage,
                duration = SnackbarDuration.Indefinite
            )
        }
    }
}

/**
 * Perform network check and show snackbar if offline.
 */
@Composable
fun IfOfflineShowSnackbar(
    scaffoldState: ScaffoldState,
    context: Context = LocalContext.current
) {
    val isOnline = NetworkManager(context).checkForActiveNetwork()
    if (!isOnline) {
        LaunchSnackBar(
            scaffoldState,
            context.getString(R.string.offline_snackbar_message)
        )
    }
}

/**
 * Show snackbar if tmdb api key is missing from [ApiKey.API_KEY].
 */
@Composable
fun ApiKeyMissingShowSnackbar(
    scaffoldState: ScaffoldState,
    context: Context = LocalContext.current
) {
    if (ApiKey.API_KEY.isEmpty()) {
        LaunchSnackBar(
            scaffoldState,
            context.getString(R.string.missing_api_key_snackbar_message)
        )
    }
}

@Composable
fun AppDivider(
    verticalPadding: Dp
) {
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
        thickness = 1.dp,
        startIndent = 0.dp,
        modifier = Modifier.padding(vertical = verticalPadding)
    )
}

/**
 * @param title commonly used text in all screens to show title or header for category.
 */
@Composable
fun CategoryTitle(
    title: String,
    textColor: Color = MaterialTheme.colors.onBackground,
    alpha: Float = 0.5f,
    startPadding: Dp = 20.dp
) {
    Text(
        text = title,
        style = MaterialTheme.typography.h6,
        color = textColor,
        modifier = Modifier
            .padding(start = startPadding)
            .alpha(alpha)
    )
}