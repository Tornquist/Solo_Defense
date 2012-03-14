package com.petronicarts.solodefense;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class SoloPongActivity extends Activity{
    /** Called when the activity is first created. */
	MainGamePanel viewPanel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Window state functions.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        
        //This works without declaring a viewPanel instance here.
        //The instance declaration is needed to pass the 
        //onPause and onResume commands though.
        viewPanel = new MainGamePanel(this);
        setContentView(viewPanel);
    }
    
    //Restarts the accelerometer after onPause
    protected void onResume() {
    	super.onResume();
        viewPanel.resume(this);
        
    }
    
    //Standard Method run when the Application loses focus.
    //This runs the pause() function in the viewPanel so that
    //the accelerometer can be paused.
    protected void onPause() {
    	super.onPause();   
        viewPanel.pause();
         
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	viewPanel.destroy();
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        // do something on back.
	    	viewPanel.backHit();
	    	return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
}