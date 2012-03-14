package com.petronicarts.solodefense;

import java.io.IOException;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.petronicarts.solodefense.Rectangle;

public class MainGamePanel extends SurfaceView implements SensorEventListener, SurfaceHolder.Callback 
{
	//Variable Declarations.
	private MainThread thread;
		
	public int screenWidth;
	public int screenHeight;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;    
	
	Paint paint;
		
	public Rectangle rect1;
	public Rectangle rect2;
	public Rectangle rect3;
	public Rectangle rect4;
	
	public float fingerData[][] = new float[4][3];
	public Rect fingerRects[] = new Rect[4];
	
	public int levelState;
	public int gameState;
	
	public int timeElapsed;
	public long oldDate;
	public long newDate;
	
	public float ballXVelocity;
	public float ballYVelocity;
	public float ballX;
	public float ballY;
	public float oldBallPositions[][] = new float[20][2];
	
	public int highScore;
	
	public int textLarge;
	public int textMedium;
	public int textSmall;
	public float playWidth;
	public float instWidth;
	public float abouWidth;
	
	//Intro
	public Bitmap intro;
	public int introImage;	
	
	public MainGamePanel(Context context)
	{
		super(context);
		
		getHolder().addCallback(this);
		
		thread = new MainThread(getHolder(),this);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		
		AssetManager assetMgr = context.getAssets();
		Typeface tf = Typeface.createFromAsset(assetMgr, "block.ttf");
		
		try {
			intro = BitmapFactory.decodeStream(assetMgr.open("intro.png"));
			introImage = 0;
		} catch (IOException e) {
			introImage = -1;
		}

		
		paint.setTypeface(tf);
		
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay(); 
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
				
		for (int i = 0; i<fingerData.length; i++)
			for (int j = 0; j<fingerData[i].length; j++)
				fingerData[i][j] = 0;
		
		for (int i = 0; i<fingerRects.length; i++)
		{
			fingerRects[i] = new Rect();
			fingerRects[i].set((int) fingerData[i][0] - 20, (int)fingerData[i][1] - 20, (int)fingerData[i][0]+20, (int)fingerData[i][1]+20);
		}
					
		//New Game Variable Set
		initVariables();
		gameState = -1;
			    
		setFocusable(true);
		
	    thread.setRunning(true);
	    thread.start();
	    
	    SharedPreferences fileStore = context.getSharedPreferences("userData", 0);
	    
    	int score = fileStore.getInt("highscore", 0); //0 is the default value
    	highScore = score;
    	
    	textLarge = (int) (screenWidth*0.1388888888888889);
	    textMedium = (int) (screenWidth*0.07407407407407407);
	    textSmall = (int) (screenWidth*0.037037037037037035);
	    
		playWidth = 0;
		instWidth = 0;
		abouWidth = 0;
		
		paint.setTextSize(textMedium);
		playWidth = paint.measureText("Play");
		instWidth = paint.measureText("Instructions");
		abouWidth = paint.measureText("About");
		
	}
	
	private void initVariables(){
		rect1 = new Rectangle(0,0,screenWidth,30,200);
		rect2 = new Rectangle(screenWidth - 30, 0, 30, screenHeight,200);
		rect3 = new Rectangle(0,screenHeight - 30, screenWidth, 30,200);
		rect4 = new Rectangle(0, 0, 30, screenHeight,200);
		levelState = 0;
	    timeElapsed = 0;	
	    newDate = System.currentTimeMillis();
	    oldDate = newDate;
	    
	    ballXVelocity = (float) Math.random()*4;
		ballYVelocity = (float) Math.random()*4;
		ballX = screenWidth/2;
		ballY = screenHeight/2;
		
		for (int i = 0; i < oldBallPositions.length; i++)
		{
			oldBallPositions[i][0] = -15;
			oldBallPositions[i][1] = -15;
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//continue the thread
	    synchronized (thread) {
	        thread.pleaseWait = false;
	        thread.notifyAll();
	    }

	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//pause the thread
	    synchronized (thread) {
	        thread.pleaseWait = true;
	    }

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        int pointerId = event.getPointerId(pointerIndex);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
        	//Log.d("pointer id - down",Integer.toString(pointerId));
        	fingerData[pointerId][0] = event.getX(pointerIndex);
        	fingerData[pointerId][1] = event.getY(pointerIndex);
        	fingerData[pointerId][2] = 1;
            break;

        case MotionEvent.ACTION_UP:          
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_CANCEL:
        	//Log.d("pointer id - cancel",Integer.toString(pointerId));
        	fingerData[pointerId][0] = event.getX(pointerIndex);
        	fingerData[pointerId][1] = event.getY(pointerIndex);
        	fingerData[pointerId][2] = 0;
            break;

        case MotionEvent.ACTION_MOVE:
        	
        	int pointerCount = event.getPointerCount();
        	for(int i = 0; i < pointerCount; ++i)
        	{
        		pointerIndex = i;
        		pointerId = event.getPointerId(pointerIndex);
        		//Log.d("pointer id - move",Integer.toString(pointerId));
        		fingerData[pointerId][0] = event.getX(pointerIndex);
            	fingerData[pointerId][1] = event.getY(pointerIndex);
            	fingerData[pointerId][2] = 1;
        	}
            break;
        }
		return true;
	}

	public void backHit()
	{
		gameState = 0;
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{		 
		if (gameState == -1)
		{
			if (introImage == -1)
			{
				gameState = 0;
				initVariables();
			}
		
			int width = 320;
			int height = 350;
			canvas.drawBitmap(intro, screenWidth/2-width/2, screenHeight/2 -height/2, paint);
		
		}
		if (gameState == 0)
		{
			paint.setColor(Color.BLACK); 
			paint.setStyle(Style.FILL); 
			canvas.drawPaint(paint); 
			paint.setColor(Color.WHITE);
			
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(textLarge);
			canvas.drawText("SOLO",screenWidth/2, textLarge*2, paint);
			canvas.drawText("DEFENSE", screenWidth/2, textLarge*3+15, paint);
			paint.setTextSize(textMedium);
			//Log.d("SIZE", String.valueOf(textMedium));
			//Log.d("SIZE", String.valueOf(textLarge));
			paint.setTextAlign(Paint.Align.CENTER);
			canvas.drawText("Play",screenWidth/2,textLarge*3+15+textMedium*5,paint);
			canvas.drawText("Instructions",screenWidth/2,textLarge*3+15+textMedium*7,paint);
			canvas.drawText("About",screenWidth/2,textLarge*3+15+textMedium*9,paint);
			
			paint.setTextAlign(Paint.Align.LEFT);
			
			//paint.setColor(Color.RED);
			//canvas.drawRect(new Rect(screenWidth/2-75 , 400 , screenWidth/2+75,440), paint);
			//canvas.drawRect(new Rect(screenWidth/2-225 , 480 , screenWidth/2+250,520), paint);
			//canvas.drawRect(new Rect(screenWidth/2-75 , 555 , screenWidth/2+125,595), paint);
			

		}
		if (gameState == 1)
		{
			//region GameDrawing
			paint.setColor(Color.BLACK); 
			paint.setStyle(Style.FILL); 
			canvas.drawPaint(paint); 
			
			//region FingerDrawing Code
			//for (int i = 0; i<fingerData.length; i++)
			//{
			//	int show = (int)(fingerData[i][2]);
			//	if (show == 1)
			//	{
			//		switch (i)
			//		{
			//			case 0:
			//				paint.setColor(Color.BLUE); 
			//				break;
			//			case 1: 
			//				paint.setColor(Color.RED);
			//				break;
			//			case 2: 
			//				paint.setColor(Color.GREEN);
			//				break;
			//			case 3:
			//				paint.setColor(Color.CYAN);
			//				break;						
			//		}
			//		paint.setTextSize(20); 
			//		canvas.drawRect(fingerRects[i], paint);
			//		canvas.drawText(Integer.toString(i), fingerData[i][0], fingerData[i][1] - 30, paint);
			//	}
			//}
			
			//endregion
					
			rect1.draw(canvas, paint);
			rect2.draw(canvas, paint);
			rect3.draw(canvas, paint);
			rect4.draw(canvas, paint);
			
			paint.setColor(Color.WHITE);
			paint.setStyle(Style.STROKE);
			canvas.drawRect(new Rect(30,30,screenWidth-30, screenHeight-30), paint);
			
			paint.setTextSize(textLarge);
			canvas.drawText(Integer.toString(timeElapsed), 60, 120, paint);
			
			paint.setTextSize(textLarge);
			canvas.drawText(Integer.toString(highScore), 60, screenHeight - (60), paint);
						
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL_AND_STROKE);
			Rect BallRect = new Rect((int) (oldBallPositions[18][0] - 4), (int) (oldBallPositions[18][1] - 4), (int) (oldBallPositions[18][0] + 4) , (int) (oldBallPositions[18][1] + 4));
			canvas.drawRect(BallRect, paint);
			paint.setColor(Color.DKGRAY);
			BallRect.set((int) (oldBallPositions[18][0] - 3), (int) (oldBallPositions[18][1] - 3), (int) (oldBallPositions[18][0] + 3) , (int) (oldBallPositions[18][1] + 3));
			canvas.drawRect(BallRect, paint);
			
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL_AND_STROKE);
			BallRect.set((int) (oldBallPositions[12][0] - 8), (int) (oldBallPositions[12][1] - 8), (int) (oldBallPositions[12][0] + 8) , (int) (oldBallPositions[12][1] + 8));
			canvas.drawRect(BallRect, paint);
			paint.setColor(Color.GRAY);
			BallRect.set((int) (oldBallPositions[12][0] - 7), (int) (oldBallPositions[12][1] - 7), (int) (oldBallPositions[12][0] + 7) , (int) (oldBallPositions[12][1] + 7));
			canvas.drawRect(BallRect, paint);
			
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL_AND_STROKE);
			BallRect.set((int) (oldBallPositions[6][0] - 12), (int) (oldBallPositions[6][1] - 12), (int) (oldBallPositions[6][0] + 12) , (int) (oldBallPositions[6][1] + 12));
			canvas.drawRect(BallRect, paint);
			paint.setColor(Color.LTGRAY);
			BallRect.set((int) (oldBallPositions[6][0] - 11), (int) (oldBallPositions[6][1] - 11), (int) (oldBallPositions[6][0] + 11) , (int) (oldBallPositions[6][1] + 11));
			canvas.drawRect(BallRect, paint);
			
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL_AND_STROKE);
			BallRect.set((int) (ballX - 16), (int) (ballY - 16), (int) (ballX + 16) , (int) (ballY + 16));
			canvas.drawRect(BallRect, paint);
			paint.setColor(Color.WHITE);
			BallRect.set((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15));
			canvas.drawRect(BallRect, paint);			
			//endregion
		}
		if (gameState == 2)
		{
			paint.setColor(Color.BLACK); 
			paint.setStyle(Style.FILL); 
			canvas.drawPaint(paint); 
			paint.setColor(Color.WHITE);
			paint.setTextSize(textLarge);
			canvas.drawText("INSTRUC",30, 100, paint);
			canvas.drawText("TIONS:",30, 200, paint);
			paint.setTextSize(textSmall);
			canvas.drawText("1) Click to Drag Paddles",30,250,paint);
			canvas.drawText("2) Opposites Attract",30,280,paint);
			canvas.drawText("3) Keep the Ball in Play",30,310,paint);
			canvas.drawText("4) Acceleration Matters",30,340,paint);
			canvas.drawText("5) ???????",30,370,paint);
			canvas.drawText("6) PROFIT",30,400,paint);
			canvas.drawText("<- BACK",30,screenHeight - 50,paint);
		}
		if (gameState == 3)
		{
			paint.setColor(Color.BLACK); 
			paint.setStyle(Style.FILL); 
			canvas.drawPaint(paint); 
			paint.setColor(Color.WHITE);
			paint.setTextSize(textLarge);
			canvas.drawText("ABOUT:",30, 100, paint);
			paint.setTextSize(textSmall);
			canvas.drawText("Created By:",30,200,paint);
			canvas.drawText("Petronic Arts",80,230,paint);
			
			canvas.drawText("Designed By:",30,290,paint);
			canvas.drawText("Nathan Tornquist",80,320,paint);
			
			canvas.drawText("Version",30,380,paint);
			canvas.drawText("1.0",80,410,paint);
			
			canvas.drawText("<- BACK",30,screenHeight - 50,paint);
		}
	}
	
	public void update() {
		if (gameState == -1)
		{
			//timeElapsed = 0;	
		    newDate = System.currentTimeMillis();
		    timeElapsed += newDate - oldDate;
		    oldDate = newDate;
			if (timeElapsed > 2500)
			{
				gameState = 0;
				initVariables();
				
			}
		}
		if (gameState == 0)
		{
			for (int i = 0; i<fingerData[i].length; i++)
			{
				if (fingerData[i][2] == 1)
				{				
					
					if ((fingerData[i][0] > screenWidth/2 - playWidth/2) && (fingerData[i][0] < screenWidth/2 + playWidth/2))
						if ((fingerData[i][1] > textLarge*3+15+textMedium*5 - textMedium/2) && (fingerData[i][1] < textLarge*3+15+textMedium*5 + textMedium/2))
							gameState = 1;
					if ((fingerData[i][0] > screenWidth/2 - instWidth/2) && (fingerData[i][0] < screenWidth/2 + instWidth/2))
						if ((fingerData[i][1] > textLarge*3+15+textMedium*7 - textMedium/2) && (fingerData[i][1] < textLarge*3+15+textMedium*7 + textMedium/2))
							gameState = 2;
					if ((fingerData[i][0] > screenWidth/2 - abouWidth/2) && (fingerData[i][0] < screenWidth/2 + abouWidth/2))
						if ((fingerData[i][1] > textLarge*3+15+textMedium*9 - textMedium/2) && (fingerData[i][1] < textLarge*3+15+textMedium*9 + textMedium/2))
							gameState = 3;
					initVariables();
				}
			}		
		}
		if (gameState == 1)
		{
			
			//region Game
			//oldDate = newDate; 
			newDate = System.currentTimeMillis();
			timeElapsed = (int)((newDate - oldDate)/1000);
			if (timeElapsed > 0)
				levelState = 1;
			if (timeElapsed > 15)
				levelState = 2;
			if (timeElapsed > 29)
				levelState = 3;
			
			for (int i = 0; i<fingerRects.length; i++)
				fingerRects[i].set((int) fingerData[i][0] - 20, (int)fingerData[i][1] - 20, (int)fingerData[i][0]+20, (int)fingerData[i][1]+20);
			
			if (levelState == 0)
			{
				
			}
			if (levelState > 0)
			{
				rect3.shrinkLeft();
			}
			if (levelState > 1)
			{
				rect1.shrinkRight();
			}
			if (levelState > 2)
			{
				rect2.shrinkUp();
				rect4.shrinkDown();
			}
			if (levelState == 1)
			{
				//region levelState 1
				for (int i = 0; i < fingerRects.length; i++)
				{
					if (!rect3.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect3.collides(fingerRects[i]))
							{
								rect3.setTouched(i);
								rect3.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);						
							}
						}
					}
				}
				
				if (rect3.touched())
				{
					if (fingerData[rect3.touchFinger()][2] == 0)
					{
						rect3.liftFinger();
					}
					rect3.dragX((int) fingerData[rect3.touchFinger()][0]);						
				}
				
				rect3.keepInBounds(screenWidth, screenHeight);
				//endregion
			}
			
			if (levelState == 2)
			{
				//region levelState 2
				for (int i = 0; i < fingerRects.length; i++)
				{
					if (!rect3.touched() && !rect1.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect3.collides(fingerRects[i]))
							{
								rect3.setTouched(i);
								rect3.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
							}
						}
					}
					
					if (!rect3.touched() && !rect1.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect1.collides(fingerRects[i]))
							{
								rect1.setTouched(i);
								rect1.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
							}
						}
					}
				}
				
				if (rect3.touched())
				{
					if (fingerData[rect3.touchFinger()][2] == 0)
					{
						rect3.liftFinger();
						rect3.reset();
						rect1.reset();
					}
					else
					{
						rect3.dragX((int) fingerData[rect3.touchFinger()][0]);			
						rect1.dragX(rect1.getX() + rect3.getDx());		
					}
				}
				if (rect1.touched())
				{
					if (fingerData[rect1.touchFinger()][2] == 0)
					{
						rect1.liftFinger();
						rect3.reset();
						rect1.reset();
					}
					else
					{
						rect1.dragX((int) fingerData[rect1.touchFinger()][0]);			
						rect3.dragX(rect3.getX() + rect1.getDx());
					}
				}
				
				
				if (rect1.fullyShrunk())
				{
					if (rect1.getX() == 0)
						rect3.setX(screenWidth - rect3.getWidth());
					if (rect1.getX() == (screenWidth - rect1.getWidth()))
						rect3.setX(0);
					
					if (rect3.getX() == 0)
						rect1.setX(screenWidth - rect1.getWidth());
					if (rect3.getX() == (screenWidth - rect3.getWidth()))
						rect1.setX(0);
					
				}
				
				rect1.keepInBounds(screenWidth, screenHeight);
				rect3.keepInBounds(screenWidth, screenHeight);
				//endregion
			}
			
			if (levelState == 3)
			{
				//region levelState 3
				for (int i = 0; i < fingerRects.length; i++)
				{
					//Collision Code
					
					if (!rect1.touched() && !rect3.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect1.collides(fingerRects[i]))
							{
								if ((rect2.touchFinger() == i) || (rect4.touchFinger() == i))
								{
									
								}
								else
								{
									rect1.setTouched(i);
									rect1.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
								}
							}
						}
					}
					
					if (!rect1.touched() && !rect3.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect3.collides(fingerRects[i]))
							{
								if ((rect2.touchFinger() == i) || (rect4.touchFinger() == i))
								{
									
								}
								else
								{
									rect3.setTouched(i);
									rect3.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
								}
							}
						}
					}
					
					if (!rect2.touched() && !rect4.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect2.collides(fingerRects[i]))
							{
								if ((rect1.touchFinger() == i) || (rect3.touchFinger() == i))
								{
									
								}
								else
								{
									rect2.setTouched(i);
									rect2.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
								}
							}
						}
					}
					
					if (!rect2.touched() && !rect4.touched())
					{
						if ((int)(fingerData[i][2]) == 1)
						{
							if (rect4.collides(fingerRects[i]))
							{
								if ((rect1.touchFinger() == i) || (rect3.touchFinger() == i))
								{
									
								}
								else
								{
									rect4.setTouched(i);
									rect4.setOffset((int) fingerData[i][0], (int) fingerData[i][1]);
								}
							}
						}
					}
				}
				
				//Dragging Code
				if (rect3.touched())
				{
					if (fingerData[rect3.touchFinger()][2] == 0)
					{
						rect3.liftFinger();
						rect3.reset();
						rect1.reset();
					}
					else
					{
						rect3.dragX((int) fingerData[rect3.touchFinger()][0]);			
						rect1.dragX(rect1.getX() + rect3.getDx());		
					}
				}
				if (rect1.touched())
				{
					if (fingerData[rect1.touchFinger()][2] == 0)
					{
						rect1.liftFinger();
						rect3.reset();
						rect1.reset();
					}
					else
					{
						rect1.dragX((int) fingerData[rect1.touchFinger()][0]);			
						rect3.dragX(rect3.getX() + rect1.getDx());
					}
				}
				
				if (rect2.touched())
				{
					if (fingerData[rect2.touchFinger()][2] == 0)
					{
						rect2.liftFinger();
						rect2.reset();
						rect4.reset();
					}
					else
					{
						rect2.dragY((int) fingerData[rect2.touchFinger()][1]);			
						rect4.dragY(rect4.getY() + rect2.getDy());
					}
				}
				if (rect4.touched())
				{
					if (fingerData[rect4.touchFinger()][2] == 0)
					{
						rect4.liftFinger();
						rect2.reset();
						rect4.reset();
					}
					else
					{
						rect4.dragY((int) fingerData[rect4.touchFinger()][1]);			
						rect2.dragY(rect2.getY() + rect4.getDy());			
					}
				}			
				
				if (rect1.fullyShrunk())
				{
					if (rect1.getX() == 0)
						rect3.setX(screenWidth - rect3.getWidth());
					if (rect1.getX() == (screenWidth - rect1.getWidth()))
						rect3.setX(0);
					
					if (rect3.getX() == 0)
						rect1.setX(screenWidth - rect1.getWidth());
					if (rect3.getX() == (screenWidth - rect3.getWidth()))
						rect1.setX(0);
				}
				if (rect4.fullyShrunk())
				{
					if (rect2.getY() == 0)
					{
						rect4.setY(screenHeight - rect4.getHeight());
					}
					
					if (rect2.getY() == (screenHeight - rect2.getHeight()))
					{
						rect4.setY(0);
					}
					
					if (rect4.getY() == 0)
					{
						rect2.setY(screenHeight - rect2.getHeight());
					}
					
					if (rect4.getY() == (screenHeight - rect4.getHeight()))
					{
						rect2.setY(0);
					}
				}
				
				rect1.keepInBounds(screenWidth, screenHeight);
				rect2.keepInBounds(screenWidth, screenHeight);
				rect3.keepInBounds(screenWidth, screenHeight);
				rect4.keepInBounds(screenWidth, screenHeight);
				//endregion
			}
			
			//region Collision Code
			for (int i = oldBallPositions.length - 1; i > 0; i--)
			{
				oldBallPositions[i][0] = oldBallPositions[i-1][0];
				oldBallPositions[i][1] = oldBallPositions[i-1][1];
			}
			oldBallPositions[0][0] = ballX;
			oldBallPositions[0][1] = ballY;
			
			ballX = ballX + ballXVelocity;
			ballY = ballY + ballYVelocity;
			
			float velocityMag = (float) Math.sqrt(ballXVelocity*ballXVelocity + ballYVelocity*ballYVelocity);
			
			if (velocityMag < 3)
			{
				ballXVelocity = (float) (ballXVelocity*1.5);
				ballYVelocity = (float) (ballYVelocity*1.5);
			}
			
			if ((ballY < -15) || (ballY > (screenHeight + 15)) || (ballX < -15) || (ballX > (screenWidth + 15)))
			{
				gameState = 0;
				if (timeElapsed > highScore)
					highScore = timeElapsed;
			}
				
			Rect BallRect = new Rect((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15));
			
			if (rect1.collides(BallRect))
			{
				ballYVelocity = (float) -(1.025*ballYVelocity);
				ballXVelocity = (float) (ballXVelocity + Math.random());
				rect1.setX(rect1.getX());
				
				while (BallRect.top < 30)
				{
					ballY += 1;
					BallRect.set(new Rect((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15)));
				}					
			}	
			
			if (rect2.collides(BallRect))
			{
				ballXVelocity = (float) -(1.025*ballXVelocity);
				ballYVelocity = (float) (ballYVelocity + Math.random());
				rect2.setX(rect2.getX());
				
				while (BallRect.right > screenWidth - 30)
				{
					ballX -= 1;
					BallRect.set(new Rect((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15)));
				}	

			}
			if (rect3.collides(BallRect))
			{
				ballYVelocity = (float) -(1.025*ballYVelocity);
				ballXVelocity = (float) (ballXVelocity - Math.random());
				rect3.setX(rect3.getX());
				
				while (BallRect.bottom > screenHeight - 30)
				{
					ballY -= 1;
					BallRect.set(new Rect((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15)));
				}	

			}	
			
			if (rect4.collides(BallRect))
			{
				ballXVelocity = (float) -(1.025*ballXVelocity);
				ballYVelocity = (float) (ballYVelocity - Math.random());
				rect4.setX(rect4.getX());
				
				while (BallRect.left < 30)
				{
					ballX += 1;
					BallRect.set(new Rect((int) (ballX - 15), (int) (ballY - 15), (int) (ballX + 15) , (int) (ballY + 15)));
				}	
			}
			//endregion
			//endregion
		}	
		if (gameState == 2)
		{
			for (int i = 0; i<fingerData[i].length; i++)
			{
				if (fingerData[i][2] == 1)
				{
					if ((fingerData[i][1] > screenHeight - 100))
						gameState = 0;
				}
			}
		
		}
		if (gameState == 3)
		{
			for (int i = 0; i<fingerData[i].length; i++)
			{
				if (fingerData[i][2] == 1)
				{
					if ((fingerData[i][1] > screenHeight - 100))
						gameState = 0;
				}
			}
		
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		ballXVelocity = (float) (ballXVelocity - event.values[0]*.001);
		ballYVelocity = (float) (ballYVelocity + event.values[1]*.001);		
	}

	public void pause() {
		mSensorManager.unregisterListener(this);
		gameState = 0;
		
		SharedPreferences fileStore = this.getContext().getSharedPreferences("userData", 0);
		SharedPreferences.Editor editor = fileStore.edit();
		editor.putInt("highscore", highScore);
		editor.commit();
	}

	public void resume(Context context) {
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
	}

	public void destroy() {
		thread.setRunning(false);
	
		if (thread != null)
		{
			Thread killThread = thread;
		    thread = null;
		    killThread.interrupt();
		}	
		
	}
	
		
}
