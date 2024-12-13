package com.example.volunteerkim.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

public class CustomBarGraphView extends View {

    private float[] data = new float[4]; // 각 카테고리별 데이터
    private Paint paint;
    private float totalValue = 1; // 전체 값 초기화 (0으로 나누는 오류 방지)
    private String[] categories = {"교육", "환경", "돌봄", "의료"}; // 카테고리 레이블

    public CustomBarGraphView(Context context) {
        super(context);
        init();
    }

    public CustomBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBarGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // Paint 객체 초기화
    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(36); // 텍스트 크기 설정
        paint.setColor(Color.GREEN); // 모든 막대 색상을 초록색으로 설정
    }

    // 그래프에 데이터를 설정하고 다시 그리기
    public void setData(float[] data) {
        this.data = data;
        Log.d("GraphDebug", "Data set: " + Arrays.toString(data));

        // 전체 값 계산
        totalValue = 0;
        for (float value : data) {
            totalValue += value;
        }

        if (totalValue == 0) {
            totalValue = 1; // 전체 값이 0이면 기본값 설정 (0으로 나누는 오류 방지)
        }

        invalidate(); // 뷰 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.length == 0) return;

        int width = getWidth();
        int height = getHeight();
        Log.d("GraphDebug", String.format("Ba rheight %d",height));
        float barWidth = width / (data.length * 2.0f); // 막대 너비 설정
        float spacing = barWidth / 2.0f; // 막대 사이 간격

        float topMargin = 20f; // 상단 여백
        float bottomMargin = 100f; // 하단 여백

        for (int i = 0; i < data.length; i++) {
            // 상대적 높이를 0~100 사이로 정규화
            float percentageHeight = (data[i] / totalValue) * 100;
            float scaledHeight = percentageHeight / 100 * (height - bottomMargin - topMargin);

            float left = width/4*i+50;
            float right = left + barWidth;
            float top = (350-percentageHeight*7/2)-10;
            if(top==-10){
                top=40;
            }

            float bottom = 350; // 하단 여백을 고려한 bottom 값

            // 디버그 로그로 확인
            Log.d("GraphDebug", String.format("Bar %d - Left: %.2f, Top: %.2f, Right: %.2f, Bottom: %.2f", i, left, top, right, bottom));

            // 초록색으로 막대 색상 설정
            paint.setColor(Color.GREEN);

            // 막대 그리기
            canvas.drawRect(left, top, right, bottom, paint);

            // 텍스트 색상 설정
            paint.setColor(Color.BLACK);

            // 데이터 값 텍스트 그리기
            canvas.drawText(String.format("%.1f", data[i]), left + barWidth / 4, top - 10, paint);
        }
    }
}
