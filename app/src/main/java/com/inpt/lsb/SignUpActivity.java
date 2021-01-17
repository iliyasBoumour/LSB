package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailInput, passwordInput, usernameInput;
    private Button signUpButton;
    private TextView signInText;
    private ImageView pdp;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef;
    private Uri imageUri;
    private StorageTask uploadTask;

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
        mStorageRef = FirebaseStorage.getInstance().getReference("pdps");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

//        events listeners
        signInText.setOnClickListener(e -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
        signUpButton.setOnClickListener(e -> onSignUpClicked());
        pdp.setOnClickListener(e->selectImage());

//        watch inputs
        emailInput.getEditText().addTextChangedListener(createTextWatcher(emailInput));
        usernameInput.getEditText().addTextChangedListener(createTextWatcher(usernameInput));
        passwordInput.getEditText().addTextChangedListener(createTextWatcher(passwordInput));
    }

    private void selectImage() {
        try {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 0);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 1);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pdp.setPadding(0,0,0,0);
        pdp.setMinimumWidth(200);
        pdp.setMinimumHeight(200);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    imageUri = getImageUri(getApplicationContext(), selectedImage);
                    Glide.with(this)
                            .load(imageUri)
                            .transform(new CircleCrop())
                            .into(pdp);
                }

                break;
            case 1:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    imageUri = data.getData();
                    Glide.with(this)
                            .load(imageUri)
                            .transform(new CircleCrop())
                            .into(pdp);
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

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private Uri uploadImage(){
        if (imageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            imageUri= taskSnapshot.getUploadSessionUri();
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            imageUri=null;;
                            Toast.makeText(SignUpActivity.this, "failed to upload image please try later", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return imageUri;
    }

    private void createAccount(String username, String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        Uri pdpUrl=uploadImage();
                        if (task.isSuccessful()) {

                            new AlertDialog.Builder(SignUpActivity.this).setTitle(R.string.successfully_signed_up).setMessage(R.string.please_sign_in).setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                finish();
                            }).show();

                            String userid = mAuth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            if (pdpUrl==null) user.put("pdp","");
                            else user.put("pdp",pdpUrl.toString());
                            db.collection("users")
                                    .document(userid)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            new AlertDialog.Builder(SignUpActivity.this)
                                                    .setTitle(R.string.successfully_signed_up)
                                                    .setMessage(R.string.please_sign_in)
                                                    .setPositiveButton("OK", (dialog, which) -> {
                                                        dialog.dismiss();
                                                        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                                        finish();
                                                    }).show();
                                        }
                                    });


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
                                        failedDialog();
                                        break;
                                }
                            } catch (ClassCastException e) {
                                showErrorSnackbar();
                            }

                        }
                    }
                });
    }

    private void failedDialog() {
        new AlertDialog.Builder(SignUpActivity.this)
                .setTitle(R.string.Login_Failed)
                .setMessage(R.string.error_creating_account)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
        ;
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
