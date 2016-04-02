package com.example.clarabellecheng_yue.inkink;

import android.graphics.Color;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Clarabelle Cheng-Yue on 3/26/2016.
 */
public class DrawingView extends View
{
    /**
     * instance variables:
     * Path drawPath
     * Paint drawPaint, canvasPaint
     * int paintColor
     * Canvas drawCanvas
     * Bitmap canvasBitmap
     * float brushSize, lastBrushSize
     * boolean erase
     * ArrayList<Path> paths
     * LinkedList<Path> undonePaths
     */
    private Path drawPath; //drawing path
    private Paint drawPaint, canvasPaint; //drawing and canvas paint

    private int paintColor = 0xFF660000; //initial color

    private Canvas drawCanvas; //canvas

    private Bitmap canvasBitmap; //canvas bitmap

    private float brushSize, lastBrushSize;
    private boolean erase=false;

    private ArrayList<Path> paths; //paths are pushed into the stack
    private LinkedList<Path> undonePaths; //paths that are popped from the stack are added to undoPaths

    //CONSTRUCTOR
    public DrawingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupDrawing();

    }

    /**
     * initializes all variables for drawing
     * sets up drawing for interaction.
     */
    private void setupDrawing()
    {
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        //instantiate new Path and Paint objects
        drawPath = new Path();
        drawPaint = new Paint();

        //set the initial color of drawPaint
        drawPaint.setColor(paintColor);

        //set initial path properties
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        //instantiate the Paint object canvasPaint
        //Note:
        //definition of dithering:
        //add white noise to (a digital recording)
        //to reduce distortion of low-amplitude signals.
        //Happens when source of color must fit into small space
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        paths = new ArrayList<>();
        undonePaths = new LinkedList<>();
    }

    /**
     * Called when custom View is assigned a size.
     * Makes a new canvas of appropriate size.
     * Instantiates Canvas drawCanvas and Bitmap canvasBitmap
     * using width and height values
     * @param w width of canvas
     * @param h height of canvas
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        //call superclass method from View
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
//view given size
    }

    /**
     * Each time the user draws using touch, View is invalidated,
     * causing onDraw() to execute
     * @param canvas canvas, created in onSizeChanged, for drawing on top of
     */
    @Override
    //TODO: fix onDraw so that correct Paths are there after Undo is called
    protected void onDraw(Canvas canvas)
    {
        //draws the canvas
        //canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        //draws the drawing path
        //canvas.drawPath(drawPath, drawPaint);
//draw view
        for(Path p : paths) {
            canvas.drawPath(p, drawPaint);

        }

        canvas.drawPath(drawPath, drawPaint);
    }

    /**
     * Detects a user's touch which draws on the screen
     * @param event actions in which the users touches the screen
     * @return
     */
    @Override
    //TODO: fix the last case.
    public boolean onTouchEvent(MotionEvent event) {
        //detects user touch by location
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction())
        {
            //User touches the View.
            //Move to that position to start drawing.
            case MotionEvent.ACTION_DOWN:
                touch_start(touchX, touchY);
                invalidate();
                break;
            //When user moves finger on View,
            //draw the path along their touch
            case MotionEvent.ACTION_MOVE:
                touch_move(touchX, touchY);
                invalidate();
                break;
            //Touch is lifted off View,
            //draw path and reset for next drawing operation.
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }

        //after break from switch, invalidate View
        invalidate(); //calls implicitly onDraw()
        return true;
    }

    /**
     * sets the new color
     * @param newColor the new color
     */
    public void setColor(String newColor)
    {
        //invalidates the View
        invalidate();
        //new color is set
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newSize){

        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
//update size
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }


    public void setErase(boolean isErase) {
        erase = isErase;
        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void OnClickUndo()
    {
        if(paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
    }

    public void onClickRedo(){
        if(undonePaths.size() > 0){
            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y){
        undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) /2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up(){
        drawPath.lineTo(mX, mY);
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.add(drawPath);
        drawPath = new Path();
    }
}
