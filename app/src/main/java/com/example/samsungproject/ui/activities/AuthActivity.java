package com.example.samsungproject.ui.activities;

import static com.example.samsungproject.core.classes.Constants.REQUEST_CODE;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photoeditor.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.ActionCodeSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuthActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        createSignInIntent();
    }

    /**
     * Creates a sign-in intent and launches it.
     * Uses Google as the authentication provider.
     */
    public void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Handles the result of the sign-in operation.
     *
     * @param result The authentication result.
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            startActivityForResult(new Intent(this, MainActivity.class), REQUEST_CODE);
        }
    }

    /**
     * Signs out the current user.
     */
    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                });
    }

    /**
     * Deletes the current user's account.
     */
    public void delete() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(task -> {
                    // ...
                });
    }

    /**
     * Launches the sign-in intent with privacy and terms links.
     * Uses an empty list of providers.
     */
    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                        "https://example.com/terms.html",
                        "https://example.com/privacy.html")
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Launches the sign-in intent with email link authentication.
     */
    public void emailLink() {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        /* yourPackageName= */ "...",
                        /* installIfNotAvailable= */ true,
                        /* minimumVersion= */ null)
                .setHandleCodeInApp(true)
                .setUrl("https://google.com")
                .build();

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .enableEmailLinkSignIn()
                        .setActionCodeSettings(actionCodeSettings)
                        .build()
        );
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Catches the email link sign-in intent and launches the sign-in intent with the provided link.
     */
    public void catchEmailLink() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        if (AuthUI.canHandleIntent(getIntent())) {
            if (getIntent().getExtras() == null) {
                return;
            }
            String link = getIntent().getExtras().getString("email_link_sign_in");
            if (link != null) {
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setEmailLink(link)
                        .setAvailableProviders(providers)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        }
    }
}
