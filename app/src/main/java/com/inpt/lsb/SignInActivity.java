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

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout textEmailInput, textPasswordInput;
    private Button loginButton;
    private TextView signupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        textEmailInput = findViewById(R.id.textEmailInput);
        textPasswordInput = findViewById(R.id.textPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText=findViewById(R.id.signupText);

        loginButton.setOnClickListener(e -> onLoginClicked());
        signupText.setOnClickListener(e->{
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        textEmailInput.getEditText().addTextChangedListener(createTextWatcher(textEmailInput));
        textPasswordInput.getEditText().addTextChangedListener(createTextWatcher(textPasswordInput));
    }

    private void onLoginClicked() {
        String email= textEmailInput.getEditText().getText().toString();
        String password= textPasswordInput.getEditText().getText().toString();
        if (email.isEmpty()){
            textEmailInput.setError("Email must not be empty");
        }else if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            textEmailInput.setError(getString(R.string.email_invalid));
        }else if(password.isEmpty()){
            textPasswordInput.setError("Password must not be empty");
        }else {
            performLogin();
        }
    }

    private void performLogin() {
        Toast.makeText(this, "weeee", Toast.LENGTH_SHORT).show();
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