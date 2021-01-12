package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailInput,passwordInput,usernameInput;
    private Button signUpButton;
    private TextView signInText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameInput = findViewById(R.id.textUsernameInput);
        emailInput = findViewById(R.id.textEmailInput);
        passwordInput = findViewById(R.id.textPasswordInput);
        signUpButton = findViewById(R.id.registerButton);
        signInText=findViewById(R.id.signInText);
        signInText.setOnClickListener(e-> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
//        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(e -> onSignUpClicked());

        emailInput.getEditText().addTextChangedListener(createTextWatcher(emailInput));
        usernameInput.getEditText().addTextChangedListener(createTextWatcher(usernameInput));
        passwordInput.getEditText().addTextChangedListener(createTextWatcher(passwordInput));
    }

    private void onSignUpClicked() {
        String username= usernameInput.getEditText().getText().toString();
        String email= emailInput.getEditText().getText().toString();
        String password= passwordInput.getEditText().getText().toString();
        if(email.isEmpty()){
            emailInput.setError(getString(R.string.email_empty));
        }else if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            emailInput.setError(getString(R.string.email_invalid));
        }else if (username.isEmpty()){
            usernameInput.setError(getString(R.string.username_empty));

        }else if(password.isEmpty()){
            passwordInput.setError(getString(R.string.password_empty));

        }else if (password.length()<8){
            passwordInput.setError(getString(R.string.short_password));
        }else if (!password.matches(".*\\d.*") || !Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE).matcher(password).find()){
            passwordInput.setError(getString(R.string.invalid_password));
        }
        else {
            createAccount(username,email,password);
        }
    }

    private void createAccount(String username,String email,String password) {
        Toast.makeText(this, "all is good", Toast.LENGTH_SHORT).show();
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
//                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
//                        }
//
//                        // ...
//                    }
//                });
    }

    private TextWatcher createTextWatcher(TextInputLayout textInput) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s,int start, int before, int count) {
                textInput.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }
}