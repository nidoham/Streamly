package org.schabi.newpipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Make sure MainActivity and OnboardActivity classes exist and are correctly referenced.
// If they are in different packages, add the necessary import statements, e.g.:
// import com.your_app_package.MainActivity;
// import com.your_app_package.OnboardActivity;

public class SplashActivity extends AppCompatActivity {

    // Duration for the splash screen to be visible when user is not logged in (in milliseconds)
    private static final int SPLASH_VISIBLE_DURATION = 1500; // e.g., 1.5 seconds
    // Duration for the fade-in and fade-out animations (in milliseconds)
    private static final int FADE_ANIMATION_DURATION = 500; // e.g., 0.5 seconds

    private FirebaseAuth mAuth;
    private View rootView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in.
            // Per requirement 1: Do not display the splash screen UI.
            // Navigate directly to MainActivity.
            navigateToMainActivity();
        } else {
            // User is not logged in.
            // Set the splash screen layout.
            setContentView(R.layout.activity_splash);
            // Get the root content view for animation
            rootView = findViewById(android.R.id.content);

            // Requirement 2: Play fade-in/out animation.
            // Note: Animation only plays when the splash is shown (user not logged in),
            // as it's not possible to animate a view that isn't displayed.
            if (rootView != null) {
                startFadeInAnimation();
            } else {
                // Fallback if layout root is not found - navigate after a standard delay
                handler.postDelayed(this::navigateToOnboardActivity, SPLASH_VISIBLE_DURATION);
            }
        }
    }

    private void startFadeInAnimation() {
        rootView.setAlpha(0f); // Start fully transparent
        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(rootView, "alpha", 0f, 1f);
        fadeIn.setDuration(FADE_ANIMATION_DURATION);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                // After fade-in completes, schedule the fade-out and navigation
                scheduleFadeOutAndNavigation();
            }
        });
        fadeIn.start();
    }

    private void scheduleFadeOutAndNavigation() {
        // Calculate delay: Total visible time minus fade-in time minus fade-out time
        final long delay = Math.max(0,
                SPLASH_VISIBLE_DURATION - FADE_ANIMATION_DURATION - FADE_ANIMATION_DURATION);

        navigateRunnable = this::startFadeOutAnimation;
        handler.postDelayed(navigateRunnable, delay);
    }

    private void startFadeOutAnimation() {
        if (rootView != null) {
            final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f);
            fadeOut.setDuration(FADE_ANIMATION_DURATION);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    navigateToOnboardActivity();
                    // Prevent standard activity transition animation
                    overridePendingTransition(0, 0);
                }
            });
            fadeOut.start();
        } else {
            // Fallback if root view is somehow null at this point
            navigateToOnboardActivity();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void navigateToMainActivity() {
        // Ensure MainActivity.class is correct
        final Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish(); // Close SplashActivity
    }

    private void navigateToOnboardActivity() {
        // Ensure OnboardActivity.class is correct
        final Intent onboardIntent = new Intent(SplashActivity.this, OnboardActivity.class);
        onboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(onboardIntent);
        finish(); // Close SplashActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to prevent memory leaks or crashes
        // if activity is destroyed early
        if (navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }
}
