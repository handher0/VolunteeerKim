package com.example.volunteerkim;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BugReportFragment extends Fragment {

    private EditText etBugDescription;
    private Button btnSubmitBugReport;
    private FirebaseFirestore db;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bug_report, container, false);

        etBugDescription = view.findViewById(R.id.et_bug_description);
        btnSubmitBugReport = view.findViewById(R.id.btn_submit_bug_report);
        db = FirebaseFirestore.getInstance();
        userEmail = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Anonymous";

        btnSubmitBugReport.setOnClickListener(v -> submitBugReport());

        return view;
    }

    private void submitBugReport() {
        String bugDescription = etBugDescription.getText().toString().trim();

        if (TextUtils.isEmpty(bugDescription)) {
            Toast.makeText(getContext(), "버그 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 버그 리포트 데이터를 Firestore에 저장
        Map<String, Object> bugReport = new HashMap<>();
        bugReport.put("description", bugDescription);
        bugReport.put("email", userEmail);
        bugReport.put("timestamp", System.currentTimeMillis());

        db.collection("bugReports")
                .add(bugReport)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "버그가 성공적으로 제출되었습니다.", Toast.LENGTH_SHORT).show();
                    etBugDescription.setText(""); // 입력 필드 초기화
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "버그 제출에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
