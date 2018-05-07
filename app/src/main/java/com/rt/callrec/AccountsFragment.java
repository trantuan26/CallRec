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
        tvEmail = view.findViewById(R.id.tv_mail);
        btnSignOut = view.findViewById(R.id.btn_signout);
        btnAdmin = view.findViewById(R.id.btn_admin);

        mAuth = FirebaseAuth.getInstance();


        if (mAuth != null) {
            FirebaseUser User = mAuth.getCurrentUser();
            if (User != null) {
                SetUI(User);
                if (User.getUid().equals("hxwTmdVFVkS9wuoF0FwLFssu2L13")) {
                    btnAdmin.setVisibility(View.VISIBLE);
                }
            }
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
        btnAdmin.setVisibility(View.INVISIBLE);

        Picasso.with(getContext())
                .load(User.getPhotoUrl())
                .into(avatar);
        tvName.setText(User.getDisplayName());
        tvEmail.setText(User.getEmail());

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
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                getActivity().finish();
                            }
                        });

                break;
            case R.id.btn_admin:
//                List<String> aye = Arrays.asList(new File(getContext().getFilesDir().getAbsolutePath()).list());
//                for (int i = 0; i < aye.size(); i++){
//                    if(aye.get(i).split("mp3").length > 0) {
//                        UploadFile(aye.get(i));
//                    }
//                }
                Intent mainIntent = new Intent(getActivity(), ListAudioActivity.class);
                startActivity(mainIntent);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
