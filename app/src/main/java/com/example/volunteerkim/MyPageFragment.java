package com.example.volunteerkim;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MyPageFragment extends Fragment {

    public static MyPageFragment newInstance(String userId) {
        MyPageFragment fragment = new MyPageFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }




    private TextView tvUserInfo;
    private Button btnChangePassword, btnLogout, btnCustomerService, btnBugReport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        tvUserInfo = view.findViewById(R.id.tv_user_info);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnCustomerService = view.findViewById(R.id.btn_customer_service);
        btnBugReport = view.findViewById(R.id.btn_bug_report);

        if (getArguments() != null) {
            String userId = getArguments().getString("userId", "알 수 없는 ID");

            // Firestore에서 닉네임 가져오기
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname");
                            tvUserInfo.setText(String.format("%s 봉사님 환영합니다!", nickname != null ? nickname : "닉네임 없음"));
                        } else {
                            tvUserInfo.setText("사용자 정보를 찾을 수 없습니다.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUserInfo.setText("정보를 불러오는 중 오류 발생");
                    });
        }

        // 비밀번호 변경 버튼 클릭
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // 로그아웃 버튼 클릭
        btnLogout.setOnClickListener(v -> logout());

        // 고객센터 버튼 클릭
        btnCustomerService.setOnClickListener(v -> showCustomerServiceInfo());

        // 버그 문의 버튼 클릭
        btnBugReport.setOnClickListener(v -> {
            BugReportFragment bugReportFragment = new BugReportFragment();

            // 프래그먼트 전환 시 애니메이션 적용
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // 새 화면 들어올 때
                            R.anim.slide_out_left,  // 현재 화면 나갈 때
                            R.anim.slide_in_left,   // 뒤로 가기 시 들어올 때
                            R.anim.slide_out_right  // 뒤로 가기 시 나갈 때
                    )
                    .replace(R.id.fragment_container, bugReportFragment)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    private void changeUserPassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "비밀번호 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "사용자 인증 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("비밀번호 변경");

        // 입력 필드 생성
        final EditText inputNewPassword = new EditText(requireContext());
        inputNewPassword.setHint("새 비밀번호 입력");
        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(inputNewPassword);

        // 확인 및 취소 버튼 추가
        builder.setPositiveButton("변경", (dialog, which) -> {
            String newPassword = inputNewPassword.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                changeUserPassword(newPassword);
            } else {
                Toast.makeText(requireContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        // 다이얼로그 표시
        builder.show();
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        // 메인 액티비티로 이동
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void showCustomerServiceInfo() {
        new AlertDialog.Builder(getActivity())
                .setTitle("고객센터")
                .setMessage("김성혁\n전화번호: 010-3956-1114\n" +
                        "손영웅\n전화번호: 010-5306-7509\n" +
                        "추교준\n전화번호: 010-4037-5634")
                .setPositiveButton("확인", null)
                .show();
    }

    private void openBugReportFragment() {
        // 버그 문의 프레그먼트로 이동하는 코드
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new BugReportFragment())
                .addToBackStack(null)
                .commit();
    }
}
