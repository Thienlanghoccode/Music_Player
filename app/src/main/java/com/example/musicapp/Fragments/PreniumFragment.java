package com.example.musicapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.musicapp.R;

public class PreniumFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_prenium, container, false);

        Button btnIndividual = view.findViewById(R.id.btnIndividual);
        Button btnDuo = view.findViewById(R.id.btnDuo);
        Button btnFamily = view.findViewById(R.id.btnFamily);

        // Sự kiện click từng nút
        btnIndividual.setOnClickListener(v -> showQrDialog(R.drawable.qr_code, "Gói Individual"));
        btnDuo.setOnClickListener(v -> showQrDialog(R.drawable.qr_code, "Gói Duo"));
        btnFamily.setOnClickListener(v -> showQrDialog(R.drawable.qr_code, "Gói Family"));
        return view;
    }
    private void showQrDialog(int qrImageResId, String title) {
        // Inflate layout dialog_qr_code.xml
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_qr_code, null);

        // Lấy ImageView từ dialog layout
        ImageView qrImage = dialogView.findViewById(R.id.qrImage);

        // Đảm bảo ImageView được tìm thấy
        if (qrImage != null) {
            qrImage.setImageResource(qrImageResId);  // Gán ảnh QR tương ứng
        }

        // Tạo AlertDialog với layout và tiêu đề
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Quét mã để thanh toán - " + title)
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .create();

        // Hiển thị dialog
        dialog.show();
    }

}