package com.example.volunteerkim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class UtilRecomendationFragment extends Fragment {

    private int currentQuestionIndex = 0; // 현재 질문 인덱스
    private int[] scores = {0, 0, 0, 0}; // 선택지 점수 저장 (4개의 유형)

    private TextView tvQuestion;
    private RadioGroup rgOptions;
    private Button btnNext;
    private View questionLayout;
    private View resultLayout;
    private TextView tvResultTitle;
    private TextView tvResultDescription;
    private Button btnRetry;

    private final String[] questions = {
            "Q1. 어떤 봉사 활동이 가장 흥미롭게 들리나요?",
            "Q2. 주말에 어떤 활동을 가장 선호하나요?",
            "Q3. 다른 사람들과 함께 일할 때 어떤 점이 중요하다고 생각하나요?",
            "Q4. 봉사 활동에서 가장 기대하는 것은 무엇인가요?"
    };

    private final String[][] options = {
            {"지역 커뮤니티와 소통", "환경 보호 활동", "아동 돌봄과 교육", "도움이 필요한 이웃 방문"},
            {"지역 축제 참여", "자연 정화 활동", "아이들과 놀이", "개인적인 시간 활용"},
            {"팀워크와 협력", "체력적인 도전", "교육과 교감", "직접적인 도움 제공"},
            {"새로운 사람 만나기", "운동과 활동", "아이들과의 관계", "사회적 책임 실천"}
    };

    private final int[][] scoresMatrix = {
            {2, 0, 0, 0}, // Q1 선택지 점수
            {0, 2, 0, 0},
            {0, 0, 2, 0},
            {0, 0, 0, 2}
    };

    private final String[] results = {
            "사회적 성향이 높은 당신에게 추천",
            "체력적 도전을 좋아하는 당신에게 추천",
            "교육적 봉사를 선호하는 당신에게 추천",
            "실질적인 도움을 제공하는 당신에게 추천"
    };

    private final String[] descriptions = {
            "지역 커뮤니티와의 소통 봉사를 추천합니다!",
            "환경 정화와 같은 체력 중심 봉사를 추천합니다!",
            "아동 돌봄과 교육 중심 봉사를 추천합니다!",
            "방문 봉사와 같은 직접적 도움 봉사를 추천합니다!"
    };

    public UtilRecomendationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendation, container, false);

        // 질문 레이아웃
        questionLayout = view.findViewById(R.id.question_layout);
        tvQuestion = view.findViewById(R.id.tv_question);
        rgOptions = view.findViewById(R.id.rg_options);
        btnNext = view.findViewById(R.id.btn_next);

        // 결과 레이아웃
        resultLayout = view.findViewById(R.id.result_layout);
        tvResultTitle = view.findViewById(R.id.tv_result_title);
        tvResultDescription = view.findViewById(R.id.tv_result_description);
        btnRetry = view.findViewById(R.id.btn_retry);

        // 첫 번째 질문 로드
        loadQuestion();

        btnNext.setOnClickListener(v -> {
            if (rgOptions.getCheckedRadioButtonId() == -1) {
                return;
            }

            // 선택한 답변의 점수 누적
            RadioButton selectedOption = view.findViewById(rgOptions.getCheckedRadioButtonId());
            int selectedIndex = rgOptions.indexOfChild(selectedOption);
            for (int i = 0; i < scores.length; i++) {
                scores[i] += scoresMatrix[selectedIndex][i];
            }

            // 다음 질문 로드 또는 결과 표시
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.length) {
                loadQuestion();
            } else {
                showResult();
            }
        });

        btnRetry.setOnClickListener(v -> resetTest());

        return view;
    }

    private void loadQuestion() {
        tvQuestion.setText(questions[currentQuestionIndex]);
        rgOptions.removeAllViews();
        for (String option : options[currentQuestionIndex]) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(option);
            radioButton.setTextSize(16);
            rgOptions.addView(radioButton);
        }
        rgOptions.clearCheck();
        questionLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
    }

    private void showResult() {
        int maxScoreIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxScoreIndex]) {
                maxScoreIndex = i;
            }
        }

        tvResultTitle.setText(results[maxScoreIndex]);
        tvResultDescription.setText(descriptions[maxScoreIndex]);

        questionLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
    }

    private void resetTest() {
        currentQuestionIndex = 0;
        scores = new int[]{0, 0, 0, 0};
        loadQuestion();
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
