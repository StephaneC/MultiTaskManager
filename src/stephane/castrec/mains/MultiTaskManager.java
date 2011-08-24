package stephane.castrec.mains;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.flurry.android.FlurryAgent;

import stephane.castrec.objects.ProcessObject;

import android.R.color;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


public class MultiTaskManager extends Activity implements OnTouchListener, OnClickListener{
    /** Called when the activity is first created. */
	
	
	private ActivityManager _ActivityMgr;
	private LinkedList<ProcessObject> _ListProcess;
	
	private Boolean _KillClick=false;
	
	//my UI components 
	private LinearLayout _ScrollView;
	private ImageView _imgIcon;
	private TextView _txtIcon;
	private Button _btnKill;
	private Button _btnStart;	
	private ProcessObject _CurrentProcessDisplay;
	private Intent _intent;
	private int _lastIntentPid=0;
	
	private PackageManager pk;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
        	FlurryAgent.setReportLocation(false);
        	FlurryAgent.onStartSession(this, "PB5DAXFF9RJK7P583JYQ");
        }
        catch (Exception e)
        {
         Log.d("MultiTaskManager", "error in init flurryagent");	
        }
        actionStarting();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	Log.d("MultiTaskManager", "MultiTaskManager onStart");
    	actionStarting();
    }
    
     
    public void onStop()
    {
    	super.onStop();
    	try
    	{
    		FlurryAgent.onEndSession(this);
    	}
    	catch (Exception e) {
			// TODO: handle exception
		}
    }
   
    @Override
    public void onResume()
    {
    	super.onResume();
    	if(_intent != null)
    	{
    		Log.d("MultitaskManager", "onResume close previous intent");
    		Process.sendSignal(_lastIntentPid, Process.SIGNAL_QUIT);
    	}
    		//_intent.getComponent().getClassName 	this.moveTaskToBack(false);
    	
    	Log.d("MultiTaskManager", "MultiTaskManager onResume");
    	
    	actionStarting();
    }
    
    
    private void actionStarting()
    {
        setContentView(R.layout.main);
        //get my components        
        _ListProcess = new LinkedList<ProcessObject>();
        _imgIcon = (ImageView)findViewById(R.id.imgicon);
        _btnKill = (Button)findViewById(R.id.btnkill);
        _btnStart = (Button)findViewById(R.id.btnstart);
        _txtIcon = (TextView)findViewById(R.id.txticon);
        
        _ScrollView = (LinearLayout)findViewById(R.id.layoutScroll);
        _ScrollView.setBackgroundColor(color.background_dark);
        _ActivityMgr = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
         
        setRunningProcess();       
        displayIcon();
    }

    
    private void displayIcon()
    {
    	try
    	{
	    	Log.d("MultiTaskManager", "MultiTaskManager displayIcon");
	    	ImageView lImage;
	    	_ScrollView.removeAllViews();
	    	if(_ListProcess.isEmpty())
	    	{
	    		TextView ltxt = new TextView(this);
	    		ltxt.setText(R.string.noapp);
	    		_ScrollView.addView(ltxt);
	    	}
	    	else
	    	{
	    		//Log.d("MultiTaskManager", "MultiTaskManager listprocess not empty :" +_ListProcess.size());
		    	for (ProcessObject processObject : _ListProcess) {
		    		try
		    		{
		    			//Log.d("MultiTaskManager", "MultiTaskManager displayIcon "+processObject.get_Label());
		    			lImage = new ImageView(this);
		    			lImage.setImageDrawable(processObject.get_icon());
		    			lImage.setOnTouchListener(this);
		    			lImage.setMaxHeight(60);
		    			lImage.setMaxWidth(60);
		    			lImage.setMinimumHeight(50);
		    			lImage.setMinimumWidth(50);
		    			lImage.setAdjustViewBounds(true);
		    			lImage.setTag(processObject.get_package());
		    			_ScrollView.addView(lImage);
		    		}
		    		catch (Exception e) {
		    			Log.e("MultiTaskManager", "MultiTaskManager displayIcon error "+e.getMessage());
					}
				}
	    	}
	    	//Log.d("MultiTaskManager", "MultiTaskManager displayIcon fin");
    	}
    	catch (Exception ex) {
    		Log.e("MultiTaskManager", "MultiTaskManager displayIcon error "+ex.getMessage());
		}    	
    }
     
    
    /**
     * setRunningProcess: Create the list of ProcessObject 
     */  	
    private void setRunningProcess()
    {
    	Log.d("MultiTaskManager", "setRunningProcess starting");
    	try
    	{
	        ProcessObject lTmp;
	        String lPackage="";
	        String lLabel="";
	        Drawable lDrawable = null;
    		List<RunningTaskInfo> lListTaskInfo = _ActivityMgr.getRunningTasks(50);
    		List<RunningAppProcessInfo> lListApp = _ActivityMgr.getRunningAppProcesses();
    		_ListProcess = new LinkedList<ProcessObject>();
	        pk = getPackageManager();
	        
	        for (RunningAppProcessInfo lProcessInfo : lListApp) {        	
	        	try {
	        		lPackage = lProcessInfo.processName;
	        		for (RunningTaskInfo lTaskInfo : lListTaskInfo) {
	        			if(lTaskInfo.baseActivity.getPackageName().equalsIgnoreCase(lProcessInfo.processName))
	        			{
	        				lDrawable = pk.getApplicationIcon(lProcessInfo.processName);
	        				lLabel = (String)pk.getApplicationLabel(pk.getApplicationInfo(lProcessInfo.processName, ApplicationInfo.FLAG_PERSISTENT));
	        				lTmp = new ProcessObject(lLabel, lPackage, lTaskInfo.baseActivity.getClassName(), lDrawable, lProcessInfo.pid); 
	        				if(!listAlreadyContainsPid(lTmp.get_Pid())&& !lTmp.get_Label().equalsIgnoreCase("MultiTaskManager"))
	        					_ListProcess.addLast(lTmp);
	        			}	        		
	        		}
				} catch (NameNotFoundException e) {
					Log.e("MultiTaskManager", "error in getApplicationIcon for app :"+lPackage);
				}				
	        } 
    	}
    	catch (Exception e) {
			Log.e("MultiTaskManager", "setRunningProcess error "+e.getMessage());
		}
    }
    
    private void setUnvisibleComponents()
    {
    	try
    	{
    		_imgIcon.setVisibility(View.INVISIBLE);
    		_btnKill.setVisibility(View.INVISIBLE);
    		_btnStart.setVisibility(View.INVISIBLE);
    		_txtIcon.setVisibility(View.INVISIBLE);
    	}
    	catch (Exception e) {
			Log.e("MultiTaskManager", "MultiTaskManager setUnvisibleComponents error "+e.getMessage());
		}
    }
    
    private Boolean listAlreadyContainsPid(int pPid)
    {
    	for (ProcessObject object : _ListProcess) {
    		if(object.get_Pid()==pPid)
    			return true;
    	}
		return false;	
    }
    
    private Boolean killProcessus()
    {
    	int pid = _CurrentProcessDisplay.get_Pid();
    	Log.d("MultiTaskManager", "MultiTaskManager killProcessus started kill PID "+pid);
    	
    	//FlurryAgent.onEvent(0, Map.Entry<"killApp", "");Map m = Map.Entry<"s","e">();
    	try
    	{
    		try
    		{
    			Process.killProcess(pid);	
    		}
    		catch (Exception e) {
    			Log.e("MultiTaskManager", "MultiTaskManager killProcessus error "+e.getMessage());
			}
    		
    		try
    		{
        		_ActivityMgr.killBackgroundProcesses(_CurrentProcessDisplay.get_package());
    		}
    		catch (Exception e) {
    			Log.e("MultiTaskManager", "MultiTaskManager killProcessus error "+e.getMessage());
			}
    		try
    		{
        		Process.sendSignal(pid, Process.SIGNAL_KILL);
    		}
    		catch (Exception e) {
    			Log.e("MultiTaskManager", "MultiTaskManager killProcessus error "+e.getMessage());
			}    		
    		rotationAndReduceImages();
    		//setRunningProcess();
    		displayIcon();
    	}
    	catch (Exception e) {
			Log.e("MultiTaskManager", "MultiTaskManager killProcessus error "+e.getMessage());
			return false;
		}
    	return true;
    }
    
    private Boolean killAllProcessus()
    {
    	try
    	{
    		int pid;
    		Iterator<ProcessObject> iterator = _ListProcess.iterator();
    		ProcessObject po;
    		while(iterator.hasNext())
    		{
    			po = iterator.next();
    			Process.killProcess(po.get_Pid());
        		_ActivityMgr.killBackgroundProcesses(po.get_package());
        		Process.sendSignal(po.get_Pid(), Process.SIGNAL_KILL); 
    			iterator.remove();
    		}
    		//setRunningProcess();
    		displayIcon();
    		
    	}
    	catch (Exception e) {
			Log.e("MultiTaskManager", "MultiTaskManager killAllProcessus error "+e.getMessage()+" "+e.getLocalizedMessage());
			return false;
		}
    	return true;
    }
    
    private Boolean startProcessus()
    {
    	Log.d("MultiTaskManager", "MultiTaskManager startProcessus started");
    	try
    	{
    		_lastIntentPid = _CurrentProcessDisplay.get_Pid();
    		_intent = new Intent(Intent.ACTION_VIEW);
    		_intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		_intent.setComponent(new ComponentName(_CurrentProcessDisplay.get_package(),_CurrentProcessDisplay.get_Class()));
    		startActivity(_intent);
    		finish();
    		
    	}
    	catch (Exception e) {
			Log.e("MultiTaskManager", "MultiTaskManager startProcessus try 1 error "+e.getMessage());
			try
			{
				Toast.makeText(this, "Can't start "+_CurrentProcessDisplay.get_Label()+" from here!", Toast.LENGTH_LONG).show();
			}
			catch (Exception er) {
				Log.e("MultiTaskManager", "MultiTaskManager startProcessus try 2 error "+er.getMessage());
			}
			return false;			
		}
    	return true;
    }
    
    
    private ProcessObject findProcess(String pName)
    {
    	for (ProcessObject lProcess : _ListProcess) {        	
        	try {
        		if(lProcess.get_package().equals(pName))
        		{
        			Log.d("MultiTaskManager", "findProcess "+lProcess.get_Label());
        			return lProcess;
        		}
        		
			} catch (Exception e) {
				Log.e("MultiTaskManager", "error in getApplicationIcon for app :"+pName);
			}				
        } 
    	Log.e("MultiTaskManager", "MultiTaskManager findProcess not found: "+pName);
    	return null;
    }


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		try
		{
			if(_KillClick)
			{
				String tag = (String)((ImageView)v).getTag();
				_CurrentProcessDisplay = findProcess(tag);
				Log.d("MultitaskManager", "ontouch process ="+_CurrentProcessDisplay.get_Label());
				//rotationAndReduceImages((ImageView)v);
				_imgIcon.setImageDrawable(((ImageView)v).getDrawable());
				_imgIcon.setVisibility(View.VISIBLE);
				killProcessus();
				return true;
			}
			else
			{
				String tag = (String)((ImageView)v).getTag();
				_CurrentProcessDisplay = findProcess(tag);
				Log.d("MultitaskManager", "ontouch process ="+_CurrentProcessDisplay.get_Label());
				_imgIcon.setImageDrawable(((ImageView)v).getDrawable());
				_imgIcon.setVisibility(View.VISIBLE);
				_btnKill.setVisibility(View.VISIBLE);
				_btnKill.setOnClickListener(this);
				_btnStart.setVisibility(View.VISIBLE);
				_btnStart.setOnClickListener(this);
				_txtIcon.setVisibility(View.VISIBLE);
				
				_txtIcon.setText(_CurrentProcessDisplay.get_Label());
				return true;
			}
		}
		catch (Exception e) {
			Log.e("MultiTaskManager", "onTouch error "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.equals(_btnStart))
		{
			startProcessus();
		}
		else if(v.equals(_btnKill))
		{
			Log.d("MultiTaskManager", "onClick "+_btnStart.getVisibility());
			killProcessus();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.killclick:
	    	if(!_KillClick)
	    	{
	    		_KillClick=true;
	    	}
	    	else
	    	{
	    		_KillClick=false;
	    	}
	        return true;
	    case R.id.killall:
	    	Log.d("MultiTaskManager", "onOptionsItemSelected kill all");
	    	killAllProcessus();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	/*
	 * rotationAndReduceImages : create animation and display it
	 * next, it delete the process from the process list
	 */
	private void rotationAndReduceImages()
	{
		try
		{
			Log.d("MultiTaskManager", "MutliTaskManager rotationAndReduceImages height:"+_imgIcon.getHeight()+":"+_imgIcon.getWidth());
			AnimationSet rootSet = new AnimationSet(true);
			Animation animScale = new  ScaleAnimation(1, 0, 1, 0,
					ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
	                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
			animScale.setDuration(1000);
			Animation animRotate = new RotateAnimation(0.0f, 1080.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animRotate.setDuration(1000);
			
			rootSet.addAnimation(animRotate);
			rootSet.addAnimation(animScale);
			
			_imgIcon.setAnimation(rootSet);
			setUnvisibleComponents();
			_ListProcess.remove(_CurrentProcessDisplay);
		}
		catch (Exception e)
		{
			Log.e("MultiTaskManager", "MultiTaskManager rotationImages error "+e.getMessage());
		}
	}

}