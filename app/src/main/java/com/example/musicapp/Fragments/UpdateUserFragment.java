package com.example.musicapp.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.dbhelper.DatabaseHelper;
import com.example.musicapp.entity.User;

import java.io.IOException;


public class UpdateUserFragment extends DialogFragment {

    Button closeUpdate, updateUserBtn;
    EditText usernameUpdate;
    ImageView imageUser;
    ImageButton updateImageBtn;
    private Uri imageUri;
    private Bitmap bitmap;
    DatabaseHelper dbHelper;
    private NavController navController;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_update_user, container, false);
        closeUpdate= view.findViewById(R.id.closeUpdate);
        updateUserBtn= view.findViewById(R.id.luuUserBtn);
        usernameUpdate= view.findViewById(R.id.usernameUpdate);
        imageUser= view.findViewById(R.id.imageUser);
        updateImageBtn= view.findViewById(R.id.updateImageBtn);
        dbHelper = new DatabaseHelper(getActivity());

        Cursor cursor= dbHelper.findUser(((MainActivity) requireActivity()).getCurrentEmail());
        if(cursor != null && cursor.moveToFirst()){
            usernameUpdate.setText(cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME")));

            byte [] image= cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));
            if(image != null){
                bitmap= BitmapFactory.decodeByteArray(image,0,image.length);
                imageUser.setImageBitmap(bitmap);
            }
        }

        ActivityResultLauncher<Intent> activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode()== Activity.RESULT_OK){
                            Intent data= o.getData();
                            assert data != null;
                            imageUri= data.getData();
                            try {
                                bitmap= android.provider.MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),imageUri);
                            } catch (IOException e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            imageUser.setImageBitmap(bitmap);
                        } else{
                            Toast.makeText(getActivity(), "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        updateImageBtn.setOnClickListener(view1 -> {
            try {
                Intent intent= new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);

            } catch (Exception e){
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        updateUserBtn.setOnClickListener(view12 -> updateFunction());


        closeUpdate.setOnClickListener(v -> dismiss());

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels);
            int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.8);
            dialog.getWindow().setLayout(width, height);
        }
    }
    private void updateFunction() {
        String email = ((MainActivity) requireActivity()).getCurrentEmail();
        if(!usernameUpdate.getText().toString().isEmpty() && bitmap != null){
            dbHelper.updateUser(new User(email,usernameUpdate.getText().toString(),bitmap));
            Toast.makeText(getActivity(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            dismiss();
//            ((MainActivity) requireActivity()).loadFragment(new UserFragment());
        }else{
            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }
}