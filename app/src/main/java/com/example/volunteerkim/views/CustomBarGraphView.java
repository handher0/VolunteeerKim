package com.example.volunteerkim.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomBarGraphView extends View {

    private Paint barPaint;
    private Paint axisPaint;
    private Paint labelPaint;

    private int[] data = {5, 10, 8, 12}; // Example data
    private String[] labels = {"대인", "환경", "행사", "동물"};

    public CustomBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        barPaint = new Paint();
        barPaint.setColor(Color.GREEN);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(2);

        labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(30);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw x and y axes
        canvas.drawLine(100, height - 100, width, height - 100, axisPaint);
        canvas.drawLine(100, height - 100, 100, 0, axisPaint);

        // Calculate bar width and spacing
        int barWidth = (width - 200) / data.length;
        int maxHeight = height - 200;

        for (int i = 0; i < data.length; i++) {
            int left = 100 + i * barWidth + 20;
            int right = left + barWidth - 40;
            int top = (int) ((1 - (data[i] / 15.0)) * maxHeight);

            // Draw bar
            canvas.drawRect(left, top, right, height - 100, barPaint);

            // Draw label
            canvas.drawText(labels[i], left + (barWidth - 40) / 2, height - 50, labelPaint);
        }
    }
}
