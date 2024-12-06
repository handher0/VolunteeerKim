package com.example.volunteerkim;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilCalenderFragment extends Fragment {

    private CalendarView calendarView;
    private TextView selectedDateTextView;
    private EditText eventEditText;
    private Button saveEventButton;
    private TextView eventsPreview;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String selectedDate; // YYYY-MM-DD 형식으로 선택된 날짜
    private Map<String, String> eventsMap = new HashMap<>(); // 날짜별 일정 저장

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 뷰 초기화
        calendarView = view.findViewById(R.id.calendarView);
        selectedDateTextView = view.findViewById(R.id.selected_date_text_view);
        eventEditText = view.findViewById(R.id.event_edit_text);
        saveEventButton = view.findViewById(R.id.save_event_button);
        eventsPreview = view.findViewById(R.id.events_preview);

        // 오늘 날짜 기본 선택
        selectedDate = getCurrentDateFromCalendar();
        selectedDateTextView.setText("선택한 날짜: " + selectedDate);

        // 날짜 선택 리스너 설정
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            selectedDateTextView.setText("선택한 날짜: " + selectedDate);

            // 해당 날짜의 일정 불러오기
            loadEventForDate(selectedDate);
        });

        // 일정 저장 버튼 리스너
        saveEventButton.setOnClickListener(v -> saveEvent());

        // 해당 월의 일정 불러오기
        loadAllEventsForMonth();

        return view;
    }

    private void saveEvent() {
        String eventText = eventEditText.getText().toString().trim();

        if (TextUtils.isEmpty(eventText)) {
            Toast.makeText(getContext(), "일정을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Firestore에 데이터 저장
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event", eventText);

        db.collection("users")
                .document(userId)
                .collection("events")
                .document(selectedDate)
                .set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    eventsMap.put(selectedDate, eventText); // 로컬에 저장
                    updateEventsPreview(); // 미리보기 갱신
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "일정 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventForDate(String date) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("events")
                .document(date)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String event = documentSnapshot.getString("event");
                        eventEditText.setText(event);
                    } else {
                        eventEditText.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "일정을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAllEventsForMonth() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eventsMap.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String date = document.getId();
                        String event = document.getString("event");
                        eventsMap.put(date, event);
                    }
                    updateEventsPreview();
                });
    }

    private void updateEventsPreview() {
        if (eventsMap.isEmpty()) {
            eventsPreview.setText("일정 미리보기:\n현재 저장된 일정이 없습니다.");
            return;
        }

        StringBuilder previewText = new StringBuilder("일정 미리보기:\n");
        for (Map.Entry<String, String> entry : eventsMap.entrySet()) {
            previewText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        eventsPreview.setText(previewText.toString());
    }


    private String getCurrentDateFromCalendar() {
        long millis = calendarView.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return year + "-" + month + "-" + day;
    }
}
