package com.example.volunteerkim;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DataMigration {

    public static String calculateNextBloodDonationDate(String lastDonationDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Calendar calendar = Calendar.getInstance();
            Date lastDate = sdf.parse(lastDonationDate);

            if (lastDate != null) {
                calendar.setTime(lastDate);
                calendar.add(Calendar.DAY_OF_YEAR, 14); // 2주 추가
                return sdf.format(calendar.getTime());
            } else {
                return "유효하지 않은 날짜입니다.";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "날짜 형식 오류: " + lastDonationDate;
        }
    }

    // 봉사 카테고리 매핑
    public static String mapCategoryToSummaryKey(String category) {
        switch (category) {
            // 교육 카테고리
            case "교육지원(학습 지도, 진로 상담, 디지털 교육 등)":
            case "문화행사(예체능 강의, 문화 체험 지원 등)":
                return "education";

            // 환경 카테고리
            case "환경보호(환경 정화, 보호 활동 등)":
            case "농어촌봉사(농어촌 지역 지원 봉사 등)":
            case "재난구호(재난 지원, 긴급 구호 활동 등)":
                return "environment";

            // 돌봄 카테고리
            case "돌봄서비스(아동 돌봄, 요양원 돌봄, 장애인 보조 등)":
            case "시설봉사(고아원 방문, 동물 보호 등)":
            case "생활편의(취약 계층 지원, 주거 환경 개선 등)":
                return "care";

            // 의료 카테고리
            case "보건의료(병원 도우미, 의약품 정리 등, 헌혈 제외)":
            case "헌혈(헌혈 캠페인)":
                return "medical";

            // default: 예상치 못한 카테고리 처리
            default:
                return "care"; // 기본값으로 'care'를 반환
        }


    }

    // 봉사 시간 계산 (분 단위)
    public static long calculateDurationInMinutes(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        return (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
    }

    // VolunteerCalendar 업데이트 메서드
    public static void updateVolunteerCalendar(FirebaseFirestore db, String nickname, ReviewPost post) {
        db.collection("UserProfiles")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> userData = documentSnapshot.exists() ? documentSnapshot.getData() : new HashMap<>();
                    if (userData == null) userData = new HashMap<>();

                    // VolunteerCalendar 데이터 업데이트
                    Map<String, Object> volunteerCalendar = (Map<String, Object>) userData.get("VolunteerCalendar");
                    if (volunteerCalendar == null) volunteerCalendar = new HashMap<>();

                    Map<String, Object> calendarEntry = new HashMap<>();
                    calendarEntry.put("startTime", post.getStartTime());
                    calendarEntry.put("endTime", post.getEndTime());
                    calendarEntry.put("category", mapCategoryToSummaryKey(post.getCategory())); // 4가지 카테고리로 나누기
                    calendarEntry.put("address", post.getAddress());
                    calendarEntry.put("place", post.getPlace());
                    calendarEntry.put("date", post.getVolunteerDate());

                    // 헌혈일 경우 blood 필드에 1 저장
                    if ("헌혈(헌혈 캠페인)".equals(post.getCategory())) {
                        calendarEntry.put("blood", 1);
                    }
                    else{
                        calendarEntry.put("blood",0);
                    }

                    volunteerCalendar.put(post.getVolunteerDate(), calendarEntry);
                    userData.put("VolunteerCalendar", volunteerCalendar);

                    // Firestore 업데이트
                    db.collection("UserProfiles")
                            .document(nickname)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> Log.d("DataMigration", "VolunteerCalendar 업데이트 성공"))
                            .addOnFailureListener(e -> Log.e("DataMigration", "VolunteerCalendar 업데이트 실패", e));
                })
                .addOnFailureListener(e -> Log.e("DataMigration", "사용자 데이터 가져오기 실패", e));
    }

    // 날짜를 밀리초로 변환하는 메서드
    public static long convertDateToMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        try {
            Date parsedDate = dateFormat.parse(date);
            if (parsedDate != null) {
                return parsedDate.getTime(); // 밀리초 반환
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // 오류 발생 시 0을 반환
    }
}
