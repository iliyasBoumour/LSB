package com.inpt.lsb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailInput, passwordInput, usernameInput;
    private Button signUpButton;
    private TextView signInText;
    private SimpleDraweeView pdp;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Uri imageUri;
    private UploadImage uploadImage;
    private StorageReference storageReference;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();

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
        pdp=findViewById(R.id.pdp);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        uploadImage=new UploadImage(this);
        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.please_wait));
        Fresco.initialize(this);
//        events listeners
        signInText.setOnClickListener(e -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
        signUpButton.setOnClickListener(e -> onSignUpClicked());
        pdp.setOnClickListener(e->uploadImage.verifyPermissions());

//        watch inputs
        emailInput.getEditText().addTextChangedListener(createTextWatcher(emailInput));
        usernameInput.getEditText().addTextChangedListener(createTextWatcher(usernameInput));
        passwordInput.getEditText().addTextChangedListener(createTextWatcher(passwordInput));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale( permission );
                if (! showRationale) {
                    Toast.makeText(this, getString(R.string.permission_settings), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Log.d("TAG", "onRequestPermissionsResult: had per");
        }
        uploadImage.verifyPermissions();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    imageUri = uploadImage.getImageUri(getApplicationContext(), selectedImage);
                    pdp.setImageURI(imageUri);
                }

                break;
            case 1:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    imageUri = data.getData();
                    pdp.setImageURI(imageUri);
                }
                break;
        }

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
                        if (task.isSuccessful()) {
                            String userid = mAuth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("status","offline");
                            user.put("uid",userid);
                            user.put("username", username);
                            user.put("searchName",username.toLowerCase());
                            user.put("email", email);
                            if(imageUri != null){
                                StorageReference filepath = storageReference
                                        .child("pdps")
                                        .child(userid + Timestamp.now().getSeconds());
                                filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                                    filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                                        user.put("pdp",uri.toString());
                                        db.collection("users")
                                                .document(userid)
                                                .set(user)
                                                .addOnSuccessListener( aVoid-> {
                                                    progressDialog.dismiss();
                                                    succesdDialog();
                                                });
                                    });
                                });
                            }else{
                                user.put("pdp","https://graph.facebook.com/3559754890766919/picture");
                                db.collection("users")
                                        .document(userid)
                                        .set(user)
                                        .addOnSuccessListener( aVoid-> {
                                            progressDialog.dismiss();
                                            succesdDialog();
                                        });
                            }




                        } else {
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
                                        new AlertDialog.Builder(SignUpActivity.this)
                                                .setTitle(R.string.Login_Failed)
                                                .setMessage(R.string.error_creating_account)
                                                .setPositiveButton("OK", (dialog, which) -> {
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

    private void succesdDialog() {
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
                    new AlertDialog.Builder(SignUpActivity.this)
                            .setTitle(R.string.successfully_signed_up)
                            .setMessage(R.string.great_enjoy)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                                finish();
                            }).show();
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
