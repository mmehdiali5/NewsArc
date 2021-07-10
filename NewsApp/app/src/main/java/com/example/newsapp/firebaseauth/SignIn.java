/*SIGN IN USING FIREBASE AUTHENTICATION*/
package com.example.newsapp.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsapp.Home;
import com.example.newsapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignIn extends AppCompatActivity {

    EditText email, password;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        String uEmail = email.getText().toString().trim();
        String uPassword = password.getText().toString();

        if (TextUtils.isEmpty(uEmail))
            email.setError("Email is required.");

        else if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches())
            email.setError("Enter a valid email address.");

        else if (TextUtils.isEmpty(uPassword))
            password.setError("Password is required.");

        else {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(uEmail, uPassword).addOnCompleteListener(task -> {

                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Sign In successful.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                } else
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            });

        }
    }

    public void skipToHomePage(View view) {
        startActivity(new Intent(getApplicationContext(), Home.class));
        finish();
    }

    public void switchToSignUpPage(View view) {
        startActivity(new Intent(getApplicationContext(), SignUp.class));
        finish();
    }
}