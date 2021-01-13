package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "eeeeeeeee";
    private TextInputLayout textEmailInput, textPasswordInput;
    private Button loginButton;
    private TextView signupText,forgetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ImageView signGoogle;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        textEmailInput = findViewById(R.id.textEmailInput);
        textPasswordInput = findViewById(R.id.textPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText=findViewById(R.id.signupText);
        forgetPassword =findViewById(R.id.forgetPassword);
        signGoogle=findViewById(R.id.signGoogle);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();


        signGoogle.setOnClickListener(e-> loginWithGoogle());
        forgetPassword.setOnClickListener(e->enterEmailDialog());
        loginButton.setOnClickListener(e -> onLoginClicked());
        signupText.setOnClickListener(e->{
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        textEmailInput.getEditText().addTextChangedListener(createTextWatcher(textEmailInput));
        textPasswordInput.getEditText().addTextChangedListener(createTextWatcher(textPasswordInput));
    }

    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                new AlertDialog.Builder(SignInActivity.this).setMessage(R.string.error_sign_google).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(SignInActivity.this,MainActivity.class));
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            new AlertDialog.Builder(SignInActivity.this).setMessage(R.string.error_sign_google).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
                        }
                    }
                });
    }

    private void enterEmailDialog() {
//        create the view
        LinearLayout linearLayout=new LinearLayout(this);
        EditText emailEt=new EditText(this);
        emailEt.setHint(R.string.email);
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));;
        linearLayout.setPadding(50,20,50,20);
        linearLayout.addView(emailEt);
//create the alert dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.recover_password)
                .setPositiveButton(R.string.Recover,((dialogInterface, i) -> {
                    String email=emailEt.getText().toString().trim();
                    recoverPassword(email);
                }))
                .setNegativeButton(R.string.Cancel,((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
//        show the alert dialog with an edit text
        alertDialog.setView(linearLayout).show();

    }

    private void recoverPassword(String email) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignInActivity.this);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).find() || email.isEmpty())  {
            alertDialog.setMessage(R.string.email_empty).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
            return;
        }
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            alertDialog.setTitle(R.string.Email_sent).setMessage(getString(R.string.email_was_sent)+email).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
                        }else{
                            alertDialog.setTitle(R.string.Error_recovering_password);
                            try {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_USER_NOT_FOUND":
                                        alertDialog.setMessage(R.string.user_does_not_exist);
                                        break;
                                    default:
                                        alertDialog.setMessage(R.string.error_sending_email);
                                        break;
                                }
                                alertDialog.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
                        }catch (ClassCastException e){
                                showErrorSnackbar(false);
                            }
                    }}
                })
        ;

    }

    private void onLoginClicked() {
        String email= textEmailInput.getEditText().getText().toString();
        String password= textPasswordInput.getEditText().getText().toString();
        if (email.isEmpty()){
            textEmailInput.setError("Email must not be empty");
        }else if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            textEmailInput.setError(getString(R.string.email_invalid));
        }else if(password.isEmpty()){
            textPasswordInput.setError(getString(R.string.password_empty));
        }else {
            login(email,password);
        }
    }

    private void login(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finishAffinity();
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignInActivity.this).setTitle(R.string.Login_Failed);
                            try {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_INVALID_EMAIL":
                                        textEmailInput.setError(getString(R.string.email_badly_formatted));
                                    break;
                                case "ERROR_USER_NOT_FOUND":
                                    alertDialog.setMessage(R.string.user_does_not_exist).setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                    }).show();
                                    break;
                                case "ERROR_WRONG_PASSWORD":
                                        textPasswordInput.setError(getString(R.string.wrong_password));
                                    break;
                                default:
                                    alertDialog.setMessage(R.string.error_logged_in).setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                    }).show();
                                    break;
                            }
                            }catch (ClassCastException e){
                                showErrorSnackbar(true);
                            }

                        }
                    }
                });
    }

    private void showErrorSnackbar(boolean retry) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView,
                R.string.connect_to_internet, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.orange));
        snackbar.setAction(R.string.retry, v -> {
            if (retry) {
                onLoginClicked();
            } else {
                enterEmailDialog();
            }
            snackbar.dismiss();
        }).show();
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