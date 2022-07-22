package com.example.locavore.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseUser;

public class EditProfileDialogFragment extends DialogFragment {

    private EditText etPhone;
    private EditText etAddress;
    private EditText etBio;

    private ImageView ivBackgroundPhoto;
    private ImageView ivProfilePhoto;

    public EditProfileDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.fragment_edit_profile_dialog, null);

        ParseUser user = ParseUser.getCurrentUser();

        etPhone = dialog.findViewById(R.id.etPhone);
        etAddress = dialog.findViewById(R.id.etAddress);
        etBio = dialog.findViewById(R.id.etBio);
        ivBackgroundPhoto = dialog.findViewById(R.id.ivBackgroundPhoto);
        ivProfilePhoto = dialog.findViewById(R.id.ivProfilePhoto);

        etPhone.setText(user.getString(User.KEY_PHONE));
        etAddress.setText(user.getString(User.KEY_ADDRESS));
        etBio.setText(user.getString(User.KEY_BIO));
        Glide.with(requireContext()).load(user.getString(User.KEY_PROFILE_BACKDROP)).centerCrop().into(ivBackgroundPhoto);
        Glide.with(requireContext()).load(user.getString(User.KEY_PROFILE_PHOTO)).circleCrop().into(ivProfilePhoto);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialog);
        alertDialogBuilder.setTitle("Edit Profile");

        alertDialogBuilder.setPositiveButton("Confirm Changes", (dialog1, which) -> {
            user.put(User.KEY_PHONE, etPhone.getText().toString());
            user.put(User.KEY_ADDRESS, etAddress.getText().toString());
            user.put(User.KEY_BIO, etBio.getText().toString());
            user.saveInBackground();
        });

        alertDialogBuilder.setNegativeButton("Cancel", (dialog12, which) -> {
            if(dialog12 != null) {
                dialog12.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }
}