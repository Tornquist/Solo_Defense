package com.petronicarts.solodefense;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Rectangle {

	private int x;			
	private int y;			
	private int width;
	private int height;
	private Rect rectangle = new Rect();
	private int collideFinger;
	private int touched;
	private int offsetX;
	private int offsetY;
	private int minSize;
	private int dy;
	private int dx;

	public Rectangle(int X, int Y, int Width, int Height, int MinSize) {
		this.x = X;
		this.y = Y;
		this.width = Width;
		this.height = Height;
		this.rectangle.set(x,y,x+width,y+height);
		this.collideFinger = -1;
		this.touched = 0;	
		this.offsetX = 0;
		this.offsetY = 0;
		this.minSize = MinSize;
		this.dx = 0;
		this.dy = 0;
	}

	public int getX() {
		return x;
	}
	public void setX(int X) {
		this.dx = x;
		this.x = X;
		this.rectangle.set(x,y,x+width,y+height);
		this.dx = this.dx - x;
	}
	public int getY() {
		return y;
	}
	public void setY(int Y) {
		this.dy = y;
		this.y = Y;
		this.rectangle.set(x,y,x+width,y+height);
		this.dy = this.dy - y;
	}
	
	public int getDx() {
		return dx;
	}
	
	public int getDy() {
		return dy;
	}
	
	public Rect getRect() {
		return rectangle;
	}
	public int getWidth()
	{
		return this.width;
	}
	public int getHeight()
	{
		return this.height;
	}
	
	public void shrinkDown()
	{
		if (this.height > minSize)
		{
			this.height = this.height - 1;
			setY(getY() + 1);
		}
	}
	
	public void shrinkUp()
	{
		if (this.height > minSize)
		{
			this.height = this.height - 1;
			setY(getY());
		}
	}
	
	public void shrinkLeft()
	{
		if (this.width > minSize)
		{
			this.width = this.width - 1;
			setX(getX());
		}
	}
	
	public void shrinkRight()
	{
		if (this.width > minSize)
		{
			this.width = this.width - 1;
			setX(getX() + 1);
		}
	}

	public void draw(Canvas canvas, Paint paint) {
		if (touched())
			paint.setColor(Color.RED);
		else
			paint.setColor(Color.WHITE);
		canvas.drawRect(rectangle, paint);
	}
	
	public boolean collides(Rect rect)
	{
		return this.rectangle.intersect(rect);
	}
	
	public void setTouched(int touchFinger)
	{
		this.touched = 1;
		this.collideFinger = touchFinger;
	}
	
	public boolean touched()
	{
		boolean retVal = false;
		if (this.touched == 0)
			retVal = false;
		else if (this.touched == 1)
			retVal = true;
		return retVal;
	}
	
	public int touchFinger()
	{
		return this.collideFinger;
	}
	
	public void setOffset(int xPos, int yPos)
	{
		this.offsetX = this.x - xPos;
		this.offsetY = this.y - yPos;
	}
	
	public void dragY(int yPos)
	{
		setY((this.offsetY + yPos));
	}
	
	public void dragX(int xPos)
	{
		setX((this.offsetX + xPos));
	}
	
	public void liftFinger(){
		this.touched = 0;	
	}

	public void keepInBounds(int screenWidth, int screenHeight) {
		if (this.x < 0)
		{
			this.setX(0);
		}
		if ((this.x + this.width) > screenWidth)
		{
			this.setX((screenWidth - this.width));
		}
		
		if (this.y < 0)
		{
			this.setY(0);
		}
		if ((this.y + this.height) > screenHeight)
		{
			this.setY((screenHeight - this.height));
		}
		
	}
	
	public boolean fullyShrunk()
	{
		boolean retVal = false;
		if (this.width == this.minSize)
			retVal = true;
		return retVal;
	}
	
	public void reset(){
		this.dx = 0;
		this.dy = 0;
		this.collideFinger = -1;
		this.offsetX = 0;
		this.offsetY = 0;
	}
	

}
