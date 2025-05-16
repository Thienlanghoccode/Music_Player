package com.example.musicapp.Fragments;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;

import com.example.musicapp.dbhelper.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class UserFragment extends Fragment {
    ImageButton btnBackUser,shareUser;
    ImageView imageUser;
    TextView usernameUser;
    Button updateUserBtn;
    String username;

    DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_user, container, false);


        dbHelper = new DatabaseHelper(getActivity());
        Cursor cursor= dbHelper.findUser(((MainActivity) requireActivity()).getCurrentEmail());
        imageUser= view.findViewById(R.id.imageUser);
        if(cursor != null && cursor.moveToFirst()){
            username= cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME"));
            byte [] image= cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));
            if(image != null){
                Bitmap bitmap= BitmapFactory.decodeByteArray(image,0,image.length);
                imageUser.setImageBitmap(bitmap);
            }

        }



        btnBackUser= view.findViewById(R.id.btnBackUser);
        usernameUser= view.findViewById(R.id.usernameUser);
        updateUserBtn= view.findViewById(R.id.updateUserBtn);
        shareUser= view.findViewById(R.id.shareUser);

        shareUser.setOnClickListener(v -> {
            shareFunc();

        });

        updateUserBtn.setOnClickListener(v -> {
            UpdateUserFragment userDialog = new UpdateUserFragment();
            userDialog.show(requireActivity().getSupportFragmentManager(), "userDialog");

        });
        btnBackUser.setOnClickListener(v -> navigateBack());
        usernameUser.setText(username);
        return view;
    }

    private void navigateBack() {
        ((MainActivity) requireActivity()).gotoHome();
    }


    private void shareFunc() {
        Uri contentUri = getContentUri();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType(contentUri != null ? "image/png" : "text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Music App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, username != null ? username : "Unknown User");

        if (contentUri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Share with"));
        } catch (Exception e) {
            Log.e("MyApp", "Lỗi khi xử lý: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "No app available to handle this action", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getContentUri() {
        Bitmap bitmap = null;
        Drawable drawable = imageUser.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmap = bitmapDrawable.getBitmap();
        } else if (drawable != null) {
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        if (bitmap == null) {
//            Toast.makeText(requireContext(), "No image available to share", Toast.LENGTH_SHORT).show();
            return null;
        }

        File imagesFolder = new File(requireActivity().getCacheDir(), "images");
        Uri contentUri = null;
        FileOutputStream stream = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            contentUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.musicapp.fileprovider",
                    file
            );
        } catch (Exception e) {
            Log.e("MyApp", "Lỗi khi xử lý: " + e.getMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("MyApp", "Lỗi khi xử lý: " + e.getMessage(), e);
                }
            }
        }

        return contentUri;
    }
}