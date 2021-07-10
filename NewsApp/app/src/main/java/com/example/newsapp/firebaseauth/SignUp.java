/*REGISTER USER USING FIREBASE AUTHENTICATION*/
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;


public class SignUp extends AppCompatActivity {

    final static int MINIMUM_PASSWORD_LENGTH = 6;
    EditText name, email, password, confirmPassword;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = findViewById(R.id.editTextUsername);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        confirmPassword = findViewById(R.id.editTextConfirmPassword);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void registerUser(View view) {

        String uName = name.getText().toString().trim();
        String uEmail = email.getText().toString().trim();
        String uPassword = password.getText().toString();
        String uConfirmPassword = confirmPassword.getText().toString();

        if (TextUtils.isEmpty(uName))
            name.setError("Username is required.");

        else if (TextUtils.isEmpty(uEmail))
            email.setError("Email is required.");

        else if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches())
            email.setError("Enter a valid email address.");

        else if (TextUtils.isEmpty(uPassword))
            password.setError("Enter password (at least 6 characters).");

        else if (uPassword.contains(" ") || uPassword.contains("\t"))
            password.setError("Password must not contain any white spaces.");

        else if (uPassword.length() < MINIMUM_PASSWORD_LENGTH)
            password.setError("Password must contain at least 6 characters.");

        else if (TextUtils.isEmpty(uConfirmPassword))
            confirmPassword.setError("Enter confirm password.");

        else if (!uPassword.equals(uConfirmPassword))
            confirmPassword.setError("Password and confirm password do not match.");

        else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(uEmail, uPassword).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    HashMap<String, String> user = new HashMap<>();
                    user.put("name", uName);

                    Toast.makeText(getApplicationContext(), "Sign Up successful.", Toast.LENGTH_LONG).show();

                    firebaseFirestore.collection("users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).set(user);

                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();

                } else {

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void switchToSignInPage(View view) {

        startActivity(new Intent(getApplicationContext(), SignIn.class));
        finish();
    }
}