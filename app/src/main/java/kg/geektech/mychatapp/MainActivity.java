package kg.geektech.mychatapp;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import kg.geektech.mychatapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainAdapter adapter;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> launcher;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String textFromEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initGoogle();
        initListeners();

        if (mAuth.getCurrentUser() != null) {
            getLiveData();
        }
    }

    private void getLiveData() {
        db.collection("myText")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<MainModel> list = new ArrayList<>();
                        for (DocumentSnapshot snapshot : value) {
                            MainModel model = snapshot.toObject(MainModel.class);
                            assert model != null;
                            model.setDocId(snapshot.getId());
                            list.add(model);
                        }
                        adapter.addItems(list);
                    }
                });
    }

    private void initListeners() {
        binding.btnSend.setOnClickListener(v -> {
            save();
        });
    }

    private void save() {
        textFromEditText = binding.etMy.getText().toString().trim();

        if (!textFromEditText.isEmpty()) {
            MainModel model = new MainModel();
            model.setText(textFromEditText);
            model.setCreatedAt(System.currentTimeMillis());
            model.setUserId(mAuth.getUid());
            db.collection("myText")
                    .add(model)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                        binding.etMy.setText("");
                    });
        } else {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
        }
    }

    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        adapter = new MainAdapter();
        binding.rvMain.setAdapter(adapter);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {

                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launcher.launch(signInIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.signIn:
                signIn();
                break;
            case R.id.signOut:
                signOut();
                break;
        }
        return true;
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    adapter.clearList();
                }
            }
        });

    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Вы успешно авторизовались", Toast.LENGTH_SHORT).show();
                        getLiveData();
                    } else {
                        Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}