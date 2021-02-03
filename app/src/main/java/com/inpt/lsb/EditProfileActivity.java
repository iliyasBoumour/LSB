package com.inpt.lsb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout textNameInput, language;
    private TextView editImage;
    private ImageView cancel, valid, pdp;
    private Button editPassword, deleteAccount, logout;
    private SwitchMaterial darkMode;
    private AutoCompleteTextView autoCompleteTextView;
    private UploadImage uploadImage;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private boolean imageEdited = false;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private MaterialButton cancelBtn, confirmBtn;
    private TextInputLayout oldPwdInput, newPwdInput, pwdInput;

    private SharedPreferences.Editor editor;

    private String strProvider;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private FirebaseAuth mAuth;
    private CollectionReference collection = FirebaseFirestore.getInstance().collection("users");
    private StorageReference storageReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isDark()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        cancel = findViewById(R.id.back);
        valid = findViewById(R.id.valid);
        pdp = findViewById(R.id.pdp);
        editImage = findViewById(R.id.editImage);
        textNameInput = findViewById(R.id.textNameInput);
        language = findViewById(R.id.language);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        darkMode = findViewById(R.id.darkMode);
        editPassword = findViewById(R.id.editPassword);
        deleteAccount = findViewById(R.id.deleteAccount);
        logout = findViewById(R.id.logout);

        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.please_wait));
        pdp.setImageURI(Uri.parse(currentUserInfo.getPdpUrl()));

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = mAuth.getCurrentUser();

        editor = getSharedPreferences("Settings", Activity.MODE_PRIVATE).edit();
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        darkMode.setChecked(preferences.getBoolean("dark", false));
        List<String> languges = new ArrayList<>();
        languges.add("English");
        languges.add("Francais");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, languges);
        autoCompleteTextView.setText(preferences.getString("language", "English"));
        autoCompleteTextView.setAdapter(arrayAdapter);
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> changeLanguge(i));

        strProvider = mAuth.getAccessToken(false).getResult().getSignInProvider();
        textNameInput.getEditText().setText(currentUserInfo.getUserName());
        textNameInput.getEditText().addTextChangedListener(createTextWatcher(textNameInput));
        if (strProvider.equals("google.com") || strProvider.equals("facebook.com")) {
            editPassword.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(this);
        valid.setOnClickListener(this);
        editImage.setOnClickListener(this);
        darkMode.setOnCheckedChangeListener((compoundButton, b) -> editTheme(b));
        editPassword.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        logout.setOnClickListener(this);
        uploadImage = new UploadImage(this);

        Log.d("TAG", "onCreate: "+isLocalImage());

    }

    private void setStatus(String status) {
        collection.whereEqualTo("uid", currentUserInfo.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", status);
                        for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                            Log.d("STATUS", "onSuccess: ");
                            userDocument.getReference().update(data);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }

    private boolean isLocalImage(){
        String u=currentUserInfo.getPdpUrl();
        URL url = null;
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String host = url.getHost();
        return "firebasestorage.googleapis.com".equals(host);
    }

    private void changeLanguge(int i) {
        switch (i) {
            case 0:
                setLocal("en", "English");
                break;
            case 1:
                setLocal("fr", "Francais");
                break;
        }
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void setLocal(String lang, String language) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        editor.putString("language", language);
        editor.apply();
    }

    private boolean isDark(){
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        boolean isDark=preferences.getBoolean("dark",false);
        return isDark;
    }

    private void editTheme(boolean b) {
        Log.d("TAG", "editTheme: ");
        if (b) {
            editor.putBoolean("dark", true);
        } else {
            editor.putBoolean("dark", false);
        }
        editor.apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
// todo delete (without mdps) name !empty
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.valid:
                checkChanges(isLocalImage());
                break;
            case R.id.editImage:
                uploadImage.verifyPermissions();
                break;
            case R.id.editPassword:
                showDialoEditPwd();
                break;
            case R.id.deleteAccount:
                showDialogDelete();
                break;
            case R.id.logout:
                Log.d("LOGOUT", "onClick: " + FirebaseAuth.getInstance().getCurrentUser());
                db.collection("Tokens").document(currentUserInfo.getUserId()).delete();
                collection.whereEqualTo("uid", currentUserInfo.getUserId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("status", "offline");
                                for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                    Log.d("STATUS", "onSuccess: ");
                                    userDocument.getReference().update(data)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    FirebaseAuth.getInstance().signOut();
                                                    Log.d("LOGOUT", "onClick: " + FirebaseAuth.getInstance().getCurrentUser());
                                                }
                                            });
                                }
                            }
                        });
                startActivity(new Intent(this, SignInActivity.class));
                finishAffinity();
                break;
        }
    }

    private void showDialogDelete() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.delete_account_popup, null);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        confirmBtn = view.findViewById(R.id.confirmBtn);
        pwdInput = view.findViewById(R.id.pwdInput);
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });
        pwdInput.getEditText().addTextChangedListener(createTextWatcher(pwdInput));
        if (strProvider.equals("google.com")) {
            pwdInput.setVisibility(View.GONE);
            confirmBtn.setOnClickListener(v -> {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                if (acct != null) {
                    progressDialog.show();
                    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                    user.reauthenticate(credential).addOnSuccessListener(task -> {
                        Log.d("TAG", "showDialogDelete: "+user.getPhotoUrl());
                        deleteAll(isLocalImage());
                    })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, getString(R.string.resign_in), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                dialog.dismiss();
                                Log.d("TAG", "showDialogDelete: " + e.getMessage());
                            });
                }
            });
        } else if (strProvider.equals("facebook.com")) {
            pwdInput.setVisibility(View.GONE);
            confirmBtn.setOnClickListener(v -> {
                progressDialog.show();
                AuthCredential credential = FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().getToken());
                user.reauthenticate(credential).addOnSuccessListener(task -> {
                    deleteAll(isLocalImage());
                })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, getString(R.string.resign_in), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                            Log.d("TAG", "showDialogDelete: " + e.getMessage());
                        });
            });
        } else {
            confirmBtn.setOnClickListener(v -> {
                if (pwdInput.getEditText().getText().toString().isEmpty()) {
                    pwdInput.setError(getString(R.string.password_empty));
                    return;
                }else deleteAcc(dialog, pwdInput.getEditText().getText().toString());
            });
        }
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    private void deleteAcc(AlertDialog dialog, String pwd) {
        progressDialog.show();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), pwd);
        user.reauthenticate(credential)
                .addOnSuccessListener(task -> {
                    deleteAll(isLocalImage());
                })
                .addOnFailureListener(e -> {
                    pwdInput.setError(getString(R.string.wrong_password));
                    progressDialog.dismiss();
                });
    }

    private void deletePdp() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(currentUserInfo.getPdpUrl());
        photoRef.delete().addOnSuccessListener((aVoid) -> {
            Log.d("TAG", "deleteUser: deleted");
        })
                .addOnFailureListener(e -> Log.d("TAG", "deleteUser: " + e.getMessage()));
    }

    private void deleteAll(boolean toDelete){
//        FirebaseUser user = mAuth.getCurrentUser();
        collection.document(currentUserInfo.getUserId()).delete();
        db.collection("Notifications").whereEqualTo("from", currentUserInfo.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshots -> {
                    for (QueryDocumentSnapshot doc : documentSnapshots) {
                        doc.getReference().delete();
                    }
                    db.collection("Notifications").whereEqualTo("to", currentUserInfo.getUserId())
                            .get()
                            .addOnSuccessListener(documentSnapshots1 -> {
                                for (QueryDocumentSnapshot doc1 : documentSnapshots1) {
                                    doc1.getReference().delete();
                                }
                                db.collection("Likes").whereEqualTo("userId", currentUserInfo.getUserId())
                                        .get()
                                        .addOnSuccessListener(documentSnapshots2 -> {
                                            for (QueryDocumentSnapshot doc2 : documentSnapshots2) {
                                                doc2.getReference().delete();
                                            }

                                            db.collection("Posts").whereEqualTo("userId", currentUserInfo.getUserId())
                                                    .get()
                                                    .addOnSuccessListener(documentSnapshots3 -> {
                                                        for (QueryDocumentSnapshot doc3 : documentSnapshots3) {
                                                            doc3.getReference().delete();
                                                        }
                                                        db.collection("Relations").whereEqualTo("userFollowedId", currentUserInfo.getUserId())
                                                                .get()
                                                                .addOnSuccessListener(documentSnapshots4 -> {
                                                                    for (QueryDocumentSnapshot doc4 : documentSnapshots4) {
                                                                        doc4.getReference().delete();
                                                                    }
                                                                    db.collection("Relations").whereEqualTo("userFollowerId", currentUserInfo.getUserId())
                                                                            .get()
                                                                            .addOnSuccessListener(documentSnapshots5 -> {
                                                                                for (QueryDocumentSnapshot doc5 : documentSnapshots5) {
                                                                                    doc5.getReference().delete();
                                                                                }
                                                                                db.collection("Tokens").document(currentUserInfo.getUserId()).delete();
                                                                                if(toDelete) deletePdp();
                                                                                user.delete()
                                                                                        .addOnSuccessListener(t -> {
                                                                                            dialog.dismiss();
                                                                                            FirebaseAuth.getInstance().signOut();
                                                                                            startActivity(new Intent(this, SignInActivity.class));
                                                                                            finishAffinity();
                                                                                        })
                                                                                        .addOnFailureListener(e -> {
                                                                                            Toast.makeText(this, "error please try later", Toast.LENGTH_SHORT).show();
                                                                                            progressDialog.dismiss();
                                                                                            dialog.dismiss();
                                                                                        });
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Log.d("TAG", "relation user follower not deleted " + e.getMessage());
                                                                            });
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.d("TAG", "relation user followed not deleted " + e.getMessage());
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d("TAG", "posts not deleted " + e.getMessage());
                                                    });
//

                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("TAG", "likes not deleted " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.d("TAG", "notifications to not deleted " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d("TAG", "notifications from not deleted " + e.getMessage());
                });
    }

    private void showDialoEditPwd() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.edit_password_popup, null);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        confirmBtn = view.findViewById(R.id.confirmBtn);
        oldPwdInput = view.findViewById(R.id.oldPwdInput);
        newPwdInput = view.findViewById(R.id.newPwdInput);
        newPwdInput.getEditText().addTextChangedListener(createTextWatcher(newPwdInput));
        oldPwdInput.getEditText().addTextChangedListener(createTextWatcher(oldPwdInput));
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });
        confirmBtn.setOnClickListener(v -> {
            updatePassword(dialog, oldPwdInput.getEditText().getText().toString(), newPwdInput.getEditText().getText().toString());
        });
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    private void updatePassword(AlertDialog dialog, String oldP, String newP) {
        if (oldP.isEmpty()){
            oldPwdInput.setError(getString(R.string.password_empty));
        }else if (newP.isEmpty()) {
            newPwdInput.setError(getString(R.string.password_empty));

        } else if (newP.length() < 8) {
            newPwdInput.setError(getString(R.string.short_password));
        } else if (!newP.matches(".*\\d.*") || !Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE).matcher(newP).find()) {
            newPwdInput.setError(getString(R.string.invalid_password));
        } else {
            progressDialog.show();
//            FirebaseUser user = mAuth.getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), oldP);
            user.reauthenticate(credential)
                    .addOnSuccessListener(task -> {
                        user.updatePassword(newP)
                                .addOnSuccessListener(t -> {
                                    dialog.dismiss();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(this, SignInActivity.class));
                                    finishAffinity();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "error please try later", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                });
                    })
                    .addOnFailureListener(
                            e -> {
                                oldPwdInput.setError(getString(R.string.wrong_password));
                                progressDialog.dismiss();
                            }
                    );
        }
    }

    private void checkChanges(boolean toDelete) {
        progressDialog.show();
        String newUserName = textNameInput.getEditText().getText().toString();
        if (!currentUserInfo.getUserName().equals(newUserName)) {
            if (newUserName.isEmpty()){
                textNameInput.setError(getString(R.string.username_empty));
                progressDialog.dismiss();
                return;
            }else{
            collection.document(currentUserInfo.getUserId())
                    .update("username", newUserName);
            currentUserInfo.setUserName(newUserName);
            }
        }
        if (imageEdited) {
            StorageReference filepath = storageReference
                    .child("pdps")
                    .child(currentUserInfo.getUserId() + Timestamp.now().getSeconds());
            filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                    if (toDelete) {
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(currentUserInfo.getPdpUrl());
                        photoRef.delete().addOnSuccessListener((aVoid) -> {
                            collection.document(currentUserInfo.getUserId())
                                    .update("pdp", uri.toString());
                            currentUserInfo.setPdpUrl(uri.toString());

                            finish();
                        })
                        .addOnFailureListener(e->{
                            Log.d("TAG", "update pdp: "+e.getMessage());

                            finish();
                        });
                    }else{
                        collection.document(currentUserInfo.getUserId())
                                .update("pdp", uri.toString());
                        currentUserInfo.setPdpUrl(uri.toString());
                        finish();
                    }
                });
            });
        } else {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale(permission);
                if (!showRationale) {
                    Toast.makeText(this, getString(R.string.permission_settings), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        uploadImage.verifyPermissions();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    imageUri = uploadImage.getImageUri(getApplicationContext(), selectedImage);
                    pdp.setImageURI(imageUri);
                    imageEdited = true;
                }

                break;
            case 1:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    imageUri = data.getData();
                    pdp.setImageURI(imageUri);
                    imageEdited = true;
                }
                break;
        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}