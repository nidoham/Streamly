package org.schabi.newpipe;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ValueEventListener;
import android.widget.Toast;
import me.nidoham.streamly.database.firebase.model.User;
import org.schabi.newpipe.databinding.ActivityOnboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import static org.schabi.newpipe.util.Localization.assureCorrectAppLanguage;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.schabi.newpipe.util.ThemeHelper;
import org.schabi.newpipe.util.ServiceHelper;

public class OnboardActivity extends AppCompatActivity {
    private static final String TAG = "OnboardActivity";
    private static final int RC_SIGN_IN = 9001;
    private ActivityOnboardBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        ThemeHelper.setDayNightMode(this);
        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));
        assureCorrectAppLanguage(this);
        super.onCreate(savedInstanceState);

        binding = ActivityOnboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        configureGoogleSignIn();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void configureGoogleSignIn() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        binding.googleSignInButton.setOnClickListener(v -> startGoogleSignIn());
//        binding.closeButton.setOnClickListener(v -> finish());
    }

    private void startGoogleSignIn() {
        binding.googleSignInButton.setEnabled(false);
        final Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                authenticateWithFirebase(account);
            } catch (final ApiException e) {
                Log.e(TAG, "Google sign-in failed", e);
                showErrorToast("Sign-in failed: " + e.getMessage());
                resetSignInButton();
            }
        }
    }

    private void authenticateWithFirebase(final GoogleSignInAccount account) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(
                account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            syncUserData(firebaseUser);
                        } else {
                            showErrorToast("Firebase user not found");
                            resetSignInButton();
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication failed", task.getException());
                        showErrorToast("Authentication failed");
                        resetSignInButton();
                    }
                });
    }

    private void syncUserData(final FirebaseUser firebaseUser) {
        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        User user;
                        if (dataSnapshot.exists()) {
                            user = dataSnapshot.getValue(User.class);
                            if (user == null) {
                                // Data exists but couldn't be parsed, create new user
                                user = createNewUser(firebaseUser);
                            } else {
                                // User exists, update with latest Firebase data if needed
                                updateUserWithFirebaseData(user, firebaseUser);
                            }
                        } else {
                            // New user, create from Firebase data
                            user = createNewUser(firebaseUser);
                        }

                        saveUserToDatabase(user);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                        showErrorToast("Failed to access user data");
                        resetSignInButton();
                    }
                });
    }

    private User createNewUser(final FirebaseUser firebaseUser) {
        final User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail() != null
                ? firebaseUser.getEmail() : "");
        user.setFullName(firebaseUser.getDisplayName() != null
                ? firebaseUser.getDisplayName() : "");
        user.setProfilePictureUrl(firebaseUser.getPhotoUrl() != null
                ? firebaseUser.getPhotoUrl().toString() : "");
        // Set creation timestamp for new users
        user.setCreatedAt(getCurrentTimestamp());
        user.setLastLoginAt(getCurrentTimestamp());
        return user;
    }

    private void updateUserWithFirebaseData(final User user, final FirebaseUser firebaseUser) {
        // Update user data with latest info from Firebase Auth
        if (firebaseUser.getEmail() != null
                && !firebaseUser.getEmail().equals(user.getEmail())) {
            user.setEmail(firebaseUser.getEmail());
        }
        if (firebaseUser.getDisplayName() != null
                && !firebaseUser.getDisplayName().equals(user.getFullName())) {
            user.setFullName(firebaseUser.getDisplayName());
        }
        if (firebaseUser.getPhotoUrl() != null
                && !firebaseUser.getPhotoUrl().toString().equals(user.getProfilePictureUrl())) {
            user.setProfilePictureUrl(firebaseUser.getPhotoUrl().toString());
        }
        // Update last login timestamp
        user.setLastLoginAt(getCurrentTimestamp());
    }

    private void saveUserToDatabase(final User user) {
        usersRef.child(user.getUserId()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToMainActivity(user);
                    } else {
                        Log.e(TAG, "Failed to save user data", task.getException());
                        showErrorToast("Failed to save user data");
                        resetSignInButton();
                    }
                });
    }

    private void navigateToMainActivity(final User user) {
        final String welcomeName = user.hasFullName() ? user.getFullName() : "User";
        Toast.makeText(this, "Welcome " + welcomeName, Toast.LENGTH_SHORT).show();

        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", user.getUserId());
        startActivity(intent);
        finish();
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private void showErrorToast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void resetSignInButton() {
        if (binding != null && binding.googleSignInButton != null) {
            binding.googleSignInButton.setEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            syncUserData(currentUser);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
