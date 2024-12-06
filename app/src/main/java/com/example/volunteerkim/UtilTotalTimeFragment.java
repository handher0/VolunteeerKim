package com.example.volunteerkim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UtilTotalTimeFragment extends Fragment {

    private TextView tvTotalVolunteerHours, tvCalculationResult;
    private Button btnAllTime, btnOneYear, btnSixMonths, btnThreeMonths, btnMilitaryBonusCalculator, btnBloodDonationCalculator;

    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        setupListeners();
        fetchTotalVolunteerData();

        return view;
    }

    private void setupListeners() {
        btnAllTime.setOnClickListener(v -> fetchFilteredData("overall"));
        btnOneYear.setOnClickListener(v -> fetchFilteredData("last1Year"));
        btnSixMonths.setOnClickListener(v -> fetchFilteredData("last6Months"));
        btnThreeMonths.setOnClickListener(v -> fetchFilteredData("last3Months"));

        btnMilitaryBonusCalculator.setOnClickListener(v -> calculateMilitaryBonus());
        btnBloodDonationCalculator.setOnClickListener(v -> calculateBloodDonations());
    }

    private void fetchTotalVolunteerData() {
        db.collection("Users")
                .document("USER_NICKNAME") // Replace with dynamic user nickname
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateTotalVolunteerHours(documentSnapshot);
                    }
                });
    }

    private void fetchFilteredData(String period) {
        db.collection("Users")
                .document("USER_NICKNAME") // Replace with dynamic user nickname
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateCategoryHours(documentSnapshot, period);
                    }
                });
    }

    private void updateTotalVolunteerHours(DocumentSnapshot document) {
        Long totalHours = document.getLong("Summary.totalVolunteerHours");
        tvTotalVolunteerHours.setText("나의 누적 봉사시간: " + totalHours + "시간");
    }

    private void updateCategoryHours(DocumentSnapshot document, String period) {
        if (document.contains("Summary.categoryHours." + period)) {
            DocumentSnapshot categoryHours = document.get("Summary.categoryHours." + period, DocumentSnapshot.class);

            // Example: Display specific categories
            if (categoryHours != null) {
                Long educationHours = categoryHours.getLong("educationSupport");
                Long environmentHours = categoryHours.getLong("environmentProtection");
                Long careHours = categoryHours.getLong("careService");

                tvCalculationResult.setText(
                        "교육: " + educationHours + "시간\n" +
                                "환경: " + environmentHours + "시간\n" +
                                "돌봄: " + careHours + "시간"
                );
            }
        }
    }

    private void calculateMilitaryBonus() {
        db.collection("Users")
                .document("USER_NICKNAME") // Replace with dynamic user nickname
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long totalPoints = documentSnapshot.getLong("Summary.militaryBonusScore");
                        tvCalculationResult.setText("군가산점: " + totalPoints + "점 (최근 1년 기준)");
                    }
                });
    }

    private void calculateBloodDonations() {
        db.collection("Users")
                .document("USER_NICKNAME") // Replace with dynamic user nickname
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long bloodDonations = documentSnapshot.getLong("Summary.totalBloodDonations");
                        tvCalculationResult.setText("총 헌혈 횟수: " + bloodDonations + "회");
                    }
                });
    }
}
