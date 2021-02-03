package com.inpt.lsb;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.inpt.Util.CurrentUserInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private TextInputLayout textEmailInput, textPasswordInput;
    private Button loginButton;
    private TextView signupText, forgetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ImageView signGoogle, signFb;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

//        initialize views
        textEmailInput = findViewById(R.id.textEmailInput);
        textPasswordInput = findViewById(R.id.textPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText=findViewById(R.id.signupText);
        forgetPassword =findViewById(R.id.forgetPassword);
        signGoogle=findViewById(R.id.signGoogle);
        signFb=findViewById(R.id.signFb);
        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.please_wait));

//        Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

//        Configure Fb Sign In
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {

                if (exception.getMessage().contains("ERR_INTERNET_DISCONNECTED")) {
                    showErrorSnackbar("fb");
                } else {
                    Log.d("FB", "onError: " + exception.getMessage());
                    showDialog(getString(R.string.error_sign_fb));
                }
            }
        });

//        initialize events listeners
        signFb.setOnClickListener(e -> LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile")));
        signGoogle.setOnClickListener(e -> loginWithGoogle());
        forgetPassword.setOnClickListener(e -> enterEmailDialog());
        loginButton.setOnClickListener(e -> onLoginClicked());
        signupText.setOnClickListener(e -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

//        watch edit text
        textEmailInput.getEditText().addTextChangedListener(createTextWatcher(textEmailInput));
        textPasswordInput.getEditText().addTextChangedListener(createTextWatcher(textPasswordInput));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                addNew();
                            }
                            signInSucces();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (((FirebaseAuthException) task.getException()).getErrorCode().equals("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) {
                                showDialog(getString(R.string.account_exists));
                            } else {
                                Log.d("FB", "onComplete: " + task.getException().getMessage());
                                showDialog(getString(R.string.error_sign_fb));
                            }
                        }
                    }
                });
    }

    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.d("LOGIN WITH GOOGLE", "loginWithGoogle:  SHOW DIALOG");

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("LOGIN SUCCESSFFUL", "onActivityResult: ");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                if (e.getMessage().contains("7:")) {
                    showErrorSnackbar("google");
                } else {
                    try {
                        throw e;
                    } catch (ApiException apiException) {
                        apiException.printStackTrace();
                    }
                    showDialog(getString(R.string.error_sign_google));
                    Log.d("G", "onComplete: " + e.getMessage());
                }
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
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                addNew();
                            }
                            signInSucces();
                        } else {
                            // If sign in fails, display a message to the user.
                            showDialog(getString(R.string.error_sign_google));
                            Log.d("G", "onComplete: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void enterEmailDialog() {
//        create the view
        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailEt = new EditText(this);
        emailEt.setHint(R.string.email);
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        ;
        linearLayout.setPadding(50, 20, 50, 20);
        linearLayout.addView(emailEt);
//create the alert dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.recover_password)
                .setPositiveButton(R.string.Recover, ((dialogInterface, i) -> {
                    String email = emailEt.getText().toString().trim();
                    recoverPassword(email);
                }))
                .setNegativeButton(R.string.Cancel, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
//        show the alert dialog with an edit text
        alertDialog.setView(linearLayout).show();

    }

    private void recoverPassword(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).find() || email.isEmpty()) {
            showDialog(getString(R.string.email_empty));
            return;
        }
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            showDialog(getString(R.string.email_was_sent) + email);
                        } else {
                            try {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_USER_NOT_FOUND":
                                        showDialog(getString(R.string.user_does_not_exist));
                                        break;
                                    default:
                                        showDialog(getString(R.string.error_sending_email));
                                        break;
                                }
                            } catch (ClassCastException e) {
                                showErrorSnackbar("recoverEmpty");
                            }
                        }
                    }
                })
        ;

    }

    private void onLoginClicked() {
        String email = textEmailInput.getEditText().getText().toString();
        String password = textPasswordInput.getEditText().getText().toString();
        if (email.isEmpty()) {
            textEmailInput.setError("Email must not be empty");
        } else if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            textEmailInput.setError(getString(R.string.email_invalid));
        } else if (password.isEmpty()) {
            textPasswordInput.setError(getString(R.string.password_empty));
        } else {
            login(email, password);
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
                            signInSucces();
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
                            } catch (ClassCastException e) {
                                showErrorSnackbar("simpleLogin");
                            }

                        }
                    }
                });
    }

    private void showErrorSnackbar(String src) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView,
                R.string.connect_to_internet, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.orange));
        snackbar.setAction(R.string.retry, v -> {
            switch (src) {
                case "simpleLogin":
                    onLoginClicked();
                    break;
                case "recoverEmpty":
                    enterEmailDialog();
                    break;
                case "google":
                    loginWithGoogle();
                    break;
                case "fb":
                    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                    break;
                default:
            }
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

    private void showDialog(String message) {
        new AlertDialog.Builder(SignInActivity.this).setMessage(message).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    //    get the user logged In
    private void signInSucces() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        currentUserInfo.setUserId(uid);
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        currentUserInfo.setUserName(doc.getString("username"));
                        currentUserInfo.setPdpUrl(doc.getString("pdp"));
                    }
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                });


    }

    private void addNew() {
        FirebaseUser user = mAuth.getCurrentUser();
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", user.getDisplayName());
        newUser.put("status", "offline");
        newUser.put("email", user.getEmail());
        newUser.put("pdp", user.getPhotoUrl().toString());
        newUser.put("searchName",user.getDisplayName().toLowerCase());
        newUser.put("uid", user.getUid());
        Log.i("TAG", "addNew: " + user.getPhotoUrl());
        db.collection("users")
                .document(user.getUid())
                .set(newUser);
        Log.i("TAG", "new");
    }

}