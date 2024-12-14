package com.example.volunteerkim;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserDataManager {

    private static final String TAG = "UserDataManager";

    // 사용자 Summary 및 VolunteerCalendar 업데이트
    public static void updateUserSummary(FirebaseFirestore db, String nickname, ReviewPost post) {
        db.collection("UserProfiles")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> userData = documentSnapshot.exists() ? documentSnapshot.getData() : new HashMap<>();
                    if (userData == null) userData = new HashMap<>();

                    // VolunteerCalendar 가져오기
                    Map<String, Object> volunteerCalendar = (Map<String, Object>) userData.get("VolunteerCalendar");
                    if (volunteerCalendar == null) volunteerCalendar = new HashMap<>();

                    // 새로운 봉사 기록 추가
                    Map<String, Object> calendarEntry = new HashMap<>();
                    calendarEntry.put("startTime", post.getStartTime());
                    calendarEntry.put("endTime", post.getEndTime());
                    calendarEntry.put("category", post.getCategory());
                    calendarEntry.put("address", post.getAddress());
                    calendarEntry.put("place", post.getPlace());
                    calendarEntry.put("date", post.getVolunteerDate());
                    calendarEntry.put("blood", post.getCategory().contains("헌혈") ? 1 : 0);

                    volunteerCalendar.put(post.getVolunteerDate(), calendarEntry);
                    userData.put("VolunteerCalendar", volunteerCalendar);

                    // Summary 가져오기
                    Map<String, Object> summary = (Map<String, Object>) userData.get("Summary");
                    if (summary == null) summary = new HashMap<>();

                    // categoryHours 가져오기
                    Map<String, Object> categoryHours = (Map<String, Object>) summary.get("categoryHours");
                    if (categoryHours == null) categoryHours = new HashMap<>();

                    // 봉사시간 계산
                    long duration = calculateDurationInMinutes(post.getStartTime(), post.getEndTime());
                    String categoryKey = DataMigration.mapCategoryToSummaryKey(post.getCategory());

                    // 전체, 1년, 6개월, 3개월 데이터 누적
                    updateCategoryTime(categoryHours, "overall", categoryKey, volunteerCalendar);
                    updateCategoryTime(categoryHours, "last1Year", categoryKey, volunteerCalendar);
                    updateCategoryTime(categoryHours, "last6Months", categoryKey, volunteerCalendar);
                    updateCategoryTime(categoryHours, "last3Months", categoryKey, volunteerCalendar);

                    // 군가산점 계산
                    calculateMilitaryBonus(summary, categoryKey, duration);

                    // 헌혈 날짜 및 횟수 계산
                    calculateBloodDonation(summary, categoryKey, post);

                    // Summary에 업데이트
                    summary.put("categoryHours", categoryHours);
                    userData.put("Summary", summary);

                    // Firestore에 업데이트
                    db.collection("UserProfiles").document(nickname)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Summary 및 VolunteerCalendar 업데이트 성공"))
                            .addOnFailureListener(e -> Log.e(TAG, "Summary 및 VolunteerCalendar 업데이트 실패", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "사용자 데이터 가져오기 실패", e));
    }


    // 봉사 시간 누적 (전체, 1년, 6개월, 3개월)
    private static void updateCategoryTime(Map<String, Object> categoryHours, String period, String categoryKey, Map<String, Object> volunteerCalendar) {
        Map<String, Long> periodData = (Map<String, Long>) categoryHours.get(period);
        if (periodData == null) periodData = new HashMap<>();

        long currentTime = System.currentTimeMillis();
        long filterDays = getFilterDays(period);

        long totalDuration = 0;

        for (String date : volunteerCalendar.keySet()) {
            long entryTime = convertDateToMillis(date);

            // 전체 기간 또는 지정된 기간 내의 데이터만 누적
            if (period.equals("overall") || (currentTime - entryTime <= filterDays * 24 * 60 * 60 * 1000)) {
                Map<String, Object> entry = (Map<String, Object>) volunteerCalendar.get(date);
                String entryCategory = DataMigration.mapCategoryToSummaryKey((String) entry.get("category"));
                long entryDuration = calculateDurationInMinutes((String) entry.get("startTime"), (String) entry.get("endTime"));

                if (entryCategory.equals(categoryKey)) {
                    totalDuration += entryDuration;
                }
            }
        }

        periodData.put(categoryKey, totalDuration);
        categoryHours.put(period, periodData);
    }

    // 기간에 따라 필터링할 일 수 반환
    private static long getFilterDays(String period) {
        switch (period) {
            case "last1Year":
                return 365;
            case "last6Months":
                return 182;
            case "last3Months":
                return 90;
            default:
                return Long.MAX_VALUE; // overall은 모든 기간 포함
        }
    }

    // 봉사시간 계산 (분 단위로 시작 시간과 종료 시간 차이 계산)
    private static long calculateDurationInMinutes(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        return (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
    }

    // 날짜를 밀리초로 변환
    private static long convertDateToMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parsedDate = dateFormat.parse(date);
            if (parsedDate != null) {
                return parsedDate.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 군가산점 계산 (1년 기준)
    private static void calculateMilitaryBonus(Map<String, Object> summary, String categoryKey, long duration) {
        if ("medical".equals(categoryKey)) {
            long militaryBonusScore = (long) summary.getOrDefault("militaryBonusScore", 0L);
            militaryBonusScore += duration / 60; // 봉사 시간으로 군가산점 추가 (1시간당 1점)
            summary.put("militaryBonusScore", militaryBonusScore);
        }
    }

    // 헌혈 관련 정보 업데이트
    private static void calculateBloodDonation(Map<String, Object> summary, String categoryKey, ReviewPost post) {
        if ("medical".equals(categoryKey) && post.getCategory().contains("헌혈")) {
            // 헌혈 횟수 추가
            long totalBloodDonations = (long) summary.getOrDefault("totalBloodDonations", 0L);
            totalBloodDonations++;
            summary.put("totalBloodDonations", totalBloodDonations);

            // 마지막 헌혈 날짜 업데이트
            summary.put("lastBloodDonationDate", post.getVolunteerDate());
        }
    }
}
