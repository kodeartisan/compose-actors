package com.developersbreach.composeactors.ui.search

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.developersbreach.composeactors.R
import com.developersbreach.composeactors.model.Actor
import com.developersbreach.composeactors.ui.actorDetails.DetailScreen
import com.developersbreach.composeactors.ui.components.AppDivider
import com.developersbreach.composeactors.ui.components.ShowSearchProgress
import com.developersbreach.composeactors.ui.home.HomeScreen


/**
 * @param selectedActor navigates to user clicked actor from row.
 * @param viewModel to manage ui state of [SearchScreen]
 * @param navigateUp navigates user to previous screen.
 *
 * This destination can be accessed only from [HomeScreen].
 * Shows searchable category list of actors in row.
 * Shows [SearchAppBar] search box looking ui in [TopAppBar]
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    selectedActor: (Int) -> Unit,
    viewModel: SearchViewModel,
    navigateUp: () -> Unit
) {
    val uiState = viewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current
    val closeKeyboard = {
        keyboardController?.hide()
    }

    Surface(
        color = MaterialTheme.colors.background
    ) {
        Scaffold(
            topBar = {
                SearchAppBar(navigateUp, viewModel, closeKeyboard)
            }
        ) {
            Box {
                // Show progress while search is happening
                val isLoadingData = !uiState.isSearchingResults && uiState.actorList.isEmpty()
                ShowSearchProgress(isLoadingData)
                // Main content for this screen
                ItemActorList(uiState.actorList, selectedActor, closeKeyboard)
            }
        }
    }
}

/**
 * @param actorsList searchable results row list elements of [Actor]
 */
@Composable
private fun ItemActorList(
    actorsList: List<Actor>,
    selectedActor: (Int) -> Unit,
    closeKeyboard: () -> Unit?
) {
    LazyColumn(
        // This padding helps avoid content going behind the navigation bars.
        modifier = Modifier.padding(bottom = 48.dp)
    ) {
        items(actorsList) { actor ->
            ItemActor(actor, selectedActor, closeKeyboard)
        }
    }
}

/**
 * @param selectedActor navigate to actor [DetailScreen] from user selected actor.
 */
@Composable
private fun ItemActor(
    actor: Actor,
    selectedActor: (Int) -> Unit,
    closeKeyboard: () -> Unit?
) {
    Text(
        text = actor.actorName,
        style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    closeKeyboard()
                    selectedActor(actor.actorId)
                }
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .wrapContentWidth(Alignment.Start)
    )
}

/**
 * @param navigateUp Navigates to previous screen.
 *
 * onValueChange = { onQueryChanged ->
 *     query = onQueryChanged
 *     if (onQueryChanged.isNotEmpty()) {
 *     viewModel.performQuery(onQueryChanged)
 * }
 *
 * If user makes changes to text, immediately updated it.
 * To avoid crash, only query when string isn't empty.
 * Pass latest query to refresh search results.
 */
@Composable
private fun SearchAppBar(
    navigateUp: () -> Unit,
    viewModel: SearchViewModel,
    closeKeyboard: () -> Unit?
) {
    // Immediately update and keep track of query from text field changes.
    var query: String by rememberSaveable { mutableStateOf("") }

    // Handle clear icon state whether to show or hide based on query.
    var showClearQueryIcon: Boolean by rememberSaveable { mutableStateOf(false) }
    // Initially the icon state will be false and hidden since query will be empty.
    if (query.isEmpty()) {
        // If query is not empty show the icon
        showClearQueryIcon = false
    } else if (query.isNotEmpty()) {
        // If query is empty hide the icon
        showClearQueryIcon = true
    }

    // Detects whether a current keyboard is opened or closed
    val keyboardState: KeyboardState by getCurrentKeyboardState()

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val recordedSpeech = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!recordedSpeech.isNullOrEmpty()) {
            query = recordedSpeech[0]
            // Perform query with the recorded query string.
            viewModel.performQuery(query)
        }
    }

    Column {

        // This Spacer avoids colliding content with app bar by matching the height of status bar.
        Spacer(Modifier.statusBarsPadding())

        TextField(
            value = query,
            onValueChange = { onQueryChanged ->
                // If user makes changes to text, immediately updated it.
                query = onQueryChanged
                // To avoid crash, only query when string isn't empty.
                if (onQueryChanged.isNotEmpty()) {
                    // Pass latest query to refresh search results.
                    viewModel.performQuery(onQueryChanged)
                }
            },
            leadingIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 4.dp),
                    onClick = {
                        closeKeyboardAndNavigateUp(
                            navigateUp = navigateUp,
                            closeKeyboard = closeKeyboard,
                            keyboardState = keyboardState
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = stringResource(id = R.string.cd_search_icon)
                    )
                }
            },
            trailingIcon = {
                if (showClearQueryIcon) {
                    IconButton(
                        onClick = {
                            query = ""
                            closeKeyboard()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = stringResource(id = R.string.cd_clear_icon)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            // This starts the activity and populates the intent with the speech text.
                            resultLauncher.launch(createLaunchSpeechRecognitionIntent)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "",
                        )
                    }
                }
            },
            placeholder = { Text(text = stringResource(R.string.hint_search_query)) },
            textStyle = MaterialTheme.typography.subtitle1,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background, RectangleShape),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )

        // Divides content and search bar with line.
        AppDivider(verticalPadding = 0.dp)
    }
}

// Create an intent that can start the Speech Recognizer activity
private val createLaunchSpeechRecognitionIntent = Intent(
    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
).apply {
    putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
}