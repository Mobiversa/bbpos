package com.mobiversa.ezy2pay.ui.ezyWire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import java.util.Hashtable;

public class WisePOSPlusPinPadView extends View {
    Paint paint = new Paint();
    String stars = "";
    Integer starsCount = 0;
    Hashtable<String, Rect> pinButton;
    Hashtable<String, Rect> pinButtonLandscape;
    Activity parentActivity;

    private static final boolean DEBUG_MODE = true;

    private final static String LOG_TAG = WisePOSPlusPinPadView.class.getName();

    private void log(String msg) {
        if (DEBUG_MODE) {
            Log.d(LOG_TAG, "[WisePOSPlusPinPadView] " + msg);
        }
    }

    public WisePOSPlusPinPadView(Activity activity, Context context, Hashtable<String, Rect> pinButton, Hashtable<String, Rect> pinButtonLandscape) {
        super(context);
        log("[WisePOSPlusPinPadView]");
        this.parentActivity = activity;
        this.pinButton = pinButton;
        this.pinButtonLandscape = pinButtonLandscape;
    }

    @SuppressLint("Range")
    @Override
    public void onDraw(Canvas canvas) {
        try {
            int rotation = parentActivity.getWindowManager().getDefaultDisplay().getRotation();
            Hashtable<String, Rect> currentPinButton = pinButton;
            if (rotation == Surface.ROTATION_0) {
                log("[onDraw] Surface.ROTATION_0");
                currentPinButton = pinButton;
            } else if (rotation == Surface.ROTATION_90) {
                log("[onDraw] Surface.ROTATION_90");
                currentPinButton = pinButtonLandscape;
            } else if (rotation == Surface.ROTATION_180) {
                log("[onDraw] Surface.ROTATION_180");
                currentPinButton = pinButton;
            } else if (rotation == Surface.ROTATION_270) {
                log("[onDraw] Surface.ROTATION_270");
                currentPinButton = pinButtonLandscape;
            }
            int cornerRadius = 20;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#f2f3f7"));
            @SuppressLint("DrawAllocation") RectF wholeRect = new RectF(0, 0, canvas.getWidth(),canvas.getHeight());
            log("[onDraw] wholeRect : " + wholeRect);
            canvas.drawRect(wholeRect, paint);


            Object[] keys = currentPinButton.keySet().toArray();
            for (Object key : keys) {
                @SuppressLint("DrawAllocation") RectF buttonRect = new RectF(currentPinButton.get(key));
                log("[onDraw] key : " + key + " buttonRect : " + buttonRect);

                paint.setStrokeWidth(1);
                paint.setStyle(Paint.Style.FILL);

                String buttonColor = "";
                String shadowColor = "";

                if (((String)key).equalsIgnoreCase("key0")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key1")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key2")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key3")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key4")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key5")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key6")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key7")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key8")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("key9")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("backspace")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("clear")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("cancel")) {
                    buttonColor = "#FFFFFF";
                    shadowColor = "#CDCBC6";
                } else if (((String)key).equalsIgnoreCase("enter")) {
                    buttonColor = "#005baa";
                    shadowColor = "#CDCBC6";
                } else {
                    buttonColor = "#FFFFFFFF";
                    shadowColor = "#CDCBC6";
                }

                paint.setColor(Color.parseColor(shadowColor));
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                RectF shadow = new RectF(currentPinButton.get(key).left + 4f , currentPinButton.get(key).top + 4f , currentPinButton.get(key).right + 4f , currentPinButton.get(key).bottom + 4f);
                canvas.drawRoundRect(shadow,cornerRadius , cornerRadius , paint);

                paint.setColor(Color.parseColor(buttonColor));
                paint.setTextSize(40f);
                canvas.drawRoundRect(buttonRect, cornerRadius , cornerRadius , paint  );

                if (((String)key).equalsIgnoreCase("key0")) {
                    drawTwoLineText(canvas, buttonRect, paint,"0");
                } else if (((String)key).equalsIgnoreCase("key1")) {
                    drawTwoLineText(canvas, buttonRect, paint,"1");
                } else if (((String)key).equalsIgnoreCase("key2")) {
                    drawTwoLineText(canvas, buttonRect, paint,"2");
                } else if (((String)key).equalsIgnoreCase("key3")) {
                    drawTwoLineText(canvas, buttonRect, paint,"3");
                } else if (((String)key).equalsIgnoreCase("key4")) {
                    drawTwoLineText(canvas, buttonRect, paint,"4");
                } else if (((String)key).equalsIgnoreCase("key5")) {
                    drawTwoLineText(canvas, buttonRect, paint,"5");
                } else if (((String)key).equalsIgnoreCase("key6")) {
                    drawTwoLineText(canvas, buttonRect, paint,"6");
                } else if (((String)key).equalsIgnoreCase("key7")) {
                    drawTwoLineText(canvas, buttonRect, paint,"7");
                } else if (((String)key).equalsIgnoreCase("key8")) {
                    drawTwoLineText(canvas, buttonRect, paint,"8");
                } else if (((String)key).equalsIgnoreCase("key9")) {
                    drawTwoLineText(canvas, buttonRect, paint,"9");
                } else if (((String)key).equalsIgnoreCase("clear")) {
                    drawTwoLineText(canvas, buttonRect, paint,"C");
                } else if (((String)key).equalsIgnoreCase("cancel")) {
                    drawTwoLineText(canvas, buttonRect, paint,"X");
                } else if (((String)key).equalsIgnoreCase("enter")) {
                    drawTwoLineText(canvas, buttonRect, paint,"Continue");
                }
            }
            paint.setTextSize(30f);
            RectF pin = new RectF(0, (float)(canvas.getHeight()* 0.1), canvas.getWidth(),canvas.getHeight() * 0.1f);
            drawText1(canvas, pin, paint,"Enter your PIN", true);
            paint.setColor(Color.GRAY);
            RectF starsRect = new RectF(0, (float)(canvas.getHeight()* 0.15), canvas.getWidth(),canvas.getHeight() * 0.2f);
            paint.setTextSize(70f);
            paint.setColor(Color.parseColor("#005baa"));
            drawTextWithLine(canvas, starsRect, paint, stars, true);
        } catch (Exception e) {
            log("[onDraw] e : " + e.toString());
        }
    }

    private void drawTwoLineText(Canvas canvas, RectF rF, Paint paint, String text){
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        Rect r = new Rect();

        if(text.equalsIgnoreCase("⇦") ||
                text.equalsIgnoreCase("C") ||  text.equalsIgnoreCase("X")  ){
            paint.setColor(Color.parseColor("#005baa"));
        } else if (text.equalsIgnoreCase("Continue") ){
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }

        canvas.getClipBounds(r);

        float cHeight =  rF.height();
        float cWidth =   rF.width();
        paint.getTextBounds( text, 0, text.length(), r);
        float x = (cWidth / 2) - (r.width() / 2f) - r.left + rF.left;
        float y = (cHeight / 2) + (r.height() / 2f) - r.bottom + rF.top;
        canvas.drawText(text, x, y, paint);
    }

    private void drawText1(Canvas canvas, RectF rF, Paint paint, String text, boolean isIncludeDescent) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        Rect r = new Rect();

        if(text.equalsIgnoreCase("Continue") || text.equalsIgnoreCase("⇦") ||
                text.equalsIgnoreCase("↼") ||  text.equalsIgnoreCase("X")  ){
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }

        float cHeight = rF.height();
        float cWidth = rF.width();
        paint.getTextBounds(text, 0, text.length(), r);
        float x = (cWidth / 2) - (r.width() / 2f) - r.left + rF.left;


        if (isIncludeDescent) {
            float y = (cHeight / 2) + (r.height() / 2f) - r.bottom + rF.top;
            canvas.drawText(text, x, y, paint);
        } else {
            float y = (cHeight / 2) + (r.height() / 2f) + rF.top;
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawTextWithLine(Canvas canvas, RectF rF, Paint paint, String text, boolean isIncludeDescent) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        Rect r = new Rect();

        float cHeight = rF.height();
        float cWidth = rF.width();
        paint.getTextBounds(text, 0, text.length(), r);
        float x = (cWidth / 2) - (r.width() / 2f) - r.left + rF.left;


        float y;
        if (isIncludeDescent) {
            y = (cHeight / 2) + (r.height() / 2f) - r.bottom + rF.top;
        } else {
            y = (cHeight / 2) + (r.height() / 2f) + rF.top;
        }
        canvas.drawText(text, x, y, paint);
        paint.setColor(Color.parseColor("#757575"));
        paint.setStrokeWidth(2);
        canvas.drawLine(x, (float)(canvas.getHeight()* 0.22), x +(r.width()) , (float)(canvas.getHeight()* 0.22), paint);
    }


    public void setStars(String stars) {
        this.stars = stars;
    }
}