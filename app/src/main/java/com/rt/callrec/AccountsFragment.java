package com.rt.callrec;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountsFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;
    private DatabaseReference mDataRefUser;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;

    private CircleImageView avatar;
    private TextView tvName, tvEmail;
    private Button btnSignOut, btnAdmin;


    public AccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        // Configure Google Sign In

        avatar = view.findViewById(R.id.profile_image);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_name);
        btnSignOut = view.findViewById(R.id.btn_signout);
        btnAdmin = view.findViewById(R.id.btn_admin);

        mAuth = FirebaseAuth.getInstance();


        if (mAuth != null) {
            FirebaseUser User = mAuth.getCurrentUser();
            SetUI(User);
        }

        btnSignOut.setOnClickListener(this);
        btnAdmin.setOnClickListener(this);
        // Inflate the layout for this fragment
        return view;
    }

    private void SetUI(FirebaseUser User) {
        avatar.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.VISIBLE);
        btnSignOut.setVisibility(View.VISIBLE);
        btnAdmin.setVisibility(View.VISIBLE);

        Picasso.with(getContext())
                .load(User.getPhotoUrl())
                .into(avatar);
        tvName.setText(User.getDisplayName());
        tvName.setText(User.getEmail());

    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signout:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                getActivity().finish();
                            }
                        });

                break;
            case R.id.btn_admin:
                //UploadFile();
                Intent mainIntent = new Intent(getActivity(), ListAudioActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void UploadFile(){
        //cap nhat anh vao store
        List<String> aye = Arrays.asList(new File(getContext().getFilesDir().getAbsolutePath()).list());
        final String filenam = aye.get(0);
        final Uri resultUri =  Uri.fromFile(new File(getContext().getFilesDir().getAbsolutePath()+"/"+filenam));


        final String firebaseUserId = mAuth.getCurrentUser().getUid();

        StorageReference mStorageRefImage  = FirebaseStorage.getInstance().getReference().child(firebaseUserId).child(filenam);
        mStorageRefImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                    Audio audio = new Audio(filenam,downloadUrl);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("reccall").child(firebaseUserId).push();


                    Map messageBody = new HashMap();
                    messageBody.put("fileName",filenam);
                    messageBody.put("mUri",downloadUrl);



                    databaseReference.updateChildren(messageBody, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("TAG", "onComplete: databaseError");
                            }
                        }
                    });


                } else {
                    Toast.makeText(getContext(), "update picture faile", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
