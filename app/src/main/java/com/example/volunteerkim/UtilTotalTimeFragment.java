package com.example.volunteerkim;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.volunteerkim.views.CustomBarGraphView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UtilTotalTimeFragment extends Fragment {

    private TextView tvTotalVolunteerHours, tvCalculationResult;
    private Button btnAllTime, btnOneYear, btnSixMonths, btnThreeMonths;
    private Button btnMilitaryBonusCalculator, btnBloodDonationCalculator;
    private CustomBarGraphView barChartView;

    private FirebaseFirestore db;
    private Button lastSelectedTimeButton;
    private Button lastSelectedCalcButton;

    private String userNickname;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_time, container, false);

        tvTotalVolunteerHours = view.findViewById(R.id.tvTotalVolunteerHours);
        tvCalculationResult = view.findViewById(R.id.tvCalculationResult);

        btnAllTime = view.findViewById(R.id.btnAllTime);
        btnOneYear = view.findViewById(R.id.btnOneYear);
        btnSixMonths = view.findViewById(R.id.btnSixMonths);
        btnThreeMonths = view.findViewById(R.id.btnThreeMonths);
        btnMilitaryBonusCalculator = view.findViewById(R.id.btnMilitaryBonusCalculator);
        btnBloodDonationCalculator = view.findViewById(R.id.btnBloodDonationCalculator);

        barChartView = view.findViewById(R.id.barGraphView);

        db = FirebaseFirestore.getInstance();

        // Firebase 인증에서 UID 가져오기
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            // Firestore에서 UID로 닉네임 가져오기
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userNickname = documentSnapshot.getString("nickname");
                            if (userNickname != null) {
                                Log.d("DebugLog", "User nickname: " + userNickname);
                                // 닉네임을 가져온 후, 디폴트로 전체 버튼과 군가산점 계산기 버튼 클릭
                                btnAllTime.performClick();
                                btnMilitaryBonusCalculator.performClick();
                            } else {
                                Log.d("DebugLog", "Nickname not found for UID: " + uid);
                            }
                        } else {
                            Log.d("DebugLog", "Document does not exist for UID: " + uid);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("DebugLog", "Error fetching nickname", e));
        }

        setupListeners();

        // 뒤로 가기 버튼 클릭 리스너 추가
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            navigateToFragment(new HomeFragment());
        });

        return view;
    }


    private void setupListeners() {
        btnAllTime.setOnClickListener(v -> {
            updateTimeButtonStyles(btnAllTime);
            fetchVolunteerData("overall");
        });
        btnOneYear.setOnClickListener(v -> {
            updateTimeButtonStyles(btnOneYear);
            fetchVolunteerData("last1Year");
        });
        btnSixMonths.setOnClickListener(v -> {
            updateTimeButtonStyles(btnSixMonths);
            fetchVolunteerData("last6Months");
        });
        btnThreeMonths.setOnClickListener(v -> {
            updateTimeButtonStyles(btnThreeMonths);
            fetchVolunteerData("last3Months");
        });

        btnMilitaryBonusCalculator.setOnClickListener(v -> {
            updateCalcButtonStyles(btnMilitaryBonusCalculator);
            fetchMilitaryBonus();
        });
        btnBloodDonationCalculator.setOnClickListener(v -> {
            updateCalcButtonStyles(btnBloodDonationCalculator);
            calculateNextBloodDonationDate();
        });
    }

    // 시간 버튼 스타일 업데이트
    private void updateTimeButtonStyles(Button selectedButton) {
        if (lastSelectedTimeButton != null) {
            lastSelectedTimeButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryWeak));
            lastSelectedTimeButton.setTextColor(Color.BLACK);
        }
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryDark));
        selectedButton.setTextColor(Color.WHITE);
        lastSelectedTimeButton = selectedButton;
    }

    // 계산기 버튼 스타일 업데이트 (군가산점과 헌혈 버튼 상호 배타적)
    private void updateCalcButtonStyles(Button selectedButton) {
        if (lastSelectedCalcButton != null) {
            lastSelectedCalcButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryWeak));
            lastSelectedCalcButton.setTextColor(Color.BLACK);
        }
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryDark));
        selectedButton.setTextColor(Color.WHITE);
        lastSelectedCalcButton = selectedButton;
    }

    private void fetchVolunteerData(String period) {
        if (userNickname == null) return;

        db.collection("UserProfiles").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateCategoryHours(documentSnapshot, period);
                    }
                });
    }

    private void updateCategoryHours(DocumentSnapshot document, String period) {
        Map<String, Object> categoryHours = (Map<String, Object>) document.get("Summary.categoryHours." + period);

        if (categoryHours != null) {
            float education = ((Number) categoryHours.getOrDefault("education", 0)).floatValue() / 60;
            float environment = ((Number) categoryHours.getOrDefault("environment", 0)).floatValue() / 60;
            float care = ((Number) categoryHours.getOrDefault("care", 0)).floatValue() / 60;
            float medical = ((Number) categoryHours.getOrDefault("medical", 0)).floatValue() / 60;

            float totalHours = education + environment + care + medical;

            tvTotalVolunteerHours.setText(String.format("나의 누적 봉사시간: %.1f시간", totalHours));
            Log.d("GraphDebug", String.format("Period: %s, Education: %.2f, Environment: %.2f, Care: %.2f, Medical: %.2f", period, education, environment, care, medical));

            // 그래프 업데이트
            barChartView.setData(new float[]{education, environment, care, medical});
        } else {
            tvTotalVolunteerHours.setText("데이터가 없습니다.");
            Log.d("GraphDebug", "No data for period: " + period);
        }
    }



    private void fetchMilitaryBonus() {
        if (userNickname == null) return;

        db.collection("UserProfiles").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long bonusScore = documentSnapshot.getLong("Summary.militaryBonusScore");
                        Log.d("DebugLog", "Fetched military bonus score: " + bonusScore);
                        if (bonusScore != null) {
                            if(bonusScore>=8){
                                tvCalculationResult.setText("현재 계산된 군가산점은 최대 점수" + 8 + "점입니다.\n(최근 봉사활동 1년 기준)");
                            }else {
                                tvCalculationResult.setText("현재 계산된 군가산점은 " + bonusScore + "점입니다.\n(최근 봉사활동 1년 기준)");
                            }
                        } else {
                            tvCalculationResult.setText("현재 계산된 군가산점이 없습니다.\n(봉사시간을 입력해주세요)");
                        }
                    } else {
                        Log.d("DebugLog", "Document does not exist for user: " + userNickname);
                        tvCalculationResult.setText("현재 계산된 군가산점이 없습니다.\n(봉사시간을 입력해주세요)");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DebugLog", "Error fetching document", e);
                });
    }


    private void calculateNextBloodDonationDate() {
        if (userNickname == null) return;

        db.collection("UserProfiles").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String lastDonationDate = documentSnapshot.getString("Summary.lastBloodDonationDate");
                        if (lastDonationDate != null) {
                            String nextAvailableDate = DataMigration.calculateNextBloodDonationDate(lastDonationDate);

                            // 현재 날짜 가져오기
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            try {
                                Date nextDate = sdf.parse(nextAvailableDate);
                                Date today = new Date();

                                if (nextDate != null && !nextDate.after(today)) {
                                    tvCalculationResult.setText("헌혈 가능합니다!");
                                } else {
                                    tvCalculationResult.setText("다음 헌혈 가능일은 " + nextAvailableDate + " 입니다.");
                                }

                            } catch (ParseException e) {
                                tvCalculationResult.setText("날짜 형식 오류가 발생했습니다.");
                                e.printStackTrace();
                            }
                        } else {
                            tvCalculationResult.setText("헌혈 기록이 없습니다.");
                        }
                    }else{
                        tvCalculationResult.setText("헌혈 기록이 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    tvCalculationResult.setText("데이터를 불러오지 못했습니다.");
                });
    }
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // 애니메이션 설정
        transaction.setCustomAnimations(
//                R.anim.slide_in_right,  // 새로운 Fragment 들어올 때
//                R.anim.slide_out_left,  // 현재 Fragment 나갈 때
                R.anim.slide_in_left,   // 뒤로가기할 때 들어오는 애니메이션
                R.anim.slide_out_right  // 뒤로가기할 때 나가는 애니메이션
        );

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
