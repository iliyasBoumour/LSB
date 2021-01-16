package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailInput, passwordInput, usernameInput;
    private Button signUpButton;
    private TextView signInText;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

//        initialize attributes
        usernameInput = findViewById(R.id.textUsernameInput);
        emailInput = findViewById(R.id.textEmailInput);
        passwordInput = findViewById(R.id.textPasswordInput);
        signUpButton = findViewById(R.id.registerButton);
        signInText = findViewById(R.id.signInText);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

//        events listeners
        signInText.setOnClickListener(e -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
        signUpButton.setOnClickListener(e -> onSignUpClicked());

//        watch inputs
        emailInput.getEditText().addTextChangedListener(createTextWatcher(emailInput));
        usernameInput.getEditText().addTextChangedListener(createTextWatcher(usernameInput));
        passwordInput.getEditText().addTextChangedListener(createTextWatcher(passwordInput));
    }

    private void onSignUpClicked() {
        String username = usernameInput.getEditText().getText().toString();
        String email = emailInput.getEditText().getText().toString();
        String password = passwordInput.getEditText().getText().toString();
        if (username.isEmpty()) {
            usernameInput.setError(getString(R.string.username_empty));
        } else if (email.isEmpty()) {
            emailInput.setError(getString(R.string.email_empty));
//            Patterns.EMAIL_ADDRESS
        } else if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            emailInput.setError(getString(R.string.email_invalid));

        } else if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.password_empty));

        } else if (password.length() < 8) {
            passwordInput.setError(getString(R.string.short_password));
        } else if (!password.matches(".*\\d.*") || !Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE).matcher(password).find()) {
            passwordInput.setError(getString(R.string.invalid_password));
        } else {
            createAccount(username, email, password);
        }
    }

    private void createAccount(String username, String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            new AlertDialog.Builder(SignUpActivity.this).setTitle(R.string.successfully_signed_up).setMessage(R.string.please_sign_in).setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                finish();
                            }).show();

                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignUpActivity.this).setTitle(R.string.Login_Failed);
                            try {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        emailInput.setError(getString(R.string.email_badly_formatted));
                                        break;
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        emailInput.setError(getString(R.string.email_already_in_use));
                                        break;
                                    default:
                                        alertDialog.setMessage(R.string.error_creating_account).setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                        }).show();
                                        break;
                                }
                            } catch (ClassCastException e) {
                                showErrorSnackbar();
                            }

                        }
                    }
                });
    }

    private void showErrorSnackbar() {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView,
                R.string.connect_to_internet, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.orange));
        snackbar.setAction(R.string.retry, v -> {
            onSignUpClicked();
            snackbar.dismiss();
        }).show();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            snackbar.dismiss();
        }, 2000);

    }

    private TextWatcher createTextWatcher(TextInputLayout textInput) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInput.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}
