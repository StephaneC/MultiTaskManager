package stephane.castrec.objects;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ProcessObject {
	private String _package;
	private String _label;
	private String _Class;
	private Drawable _icon;
	private int _Pid;
	
	public ProcessObject(String pLabel, String pName, String pClass, Drawable pIcon, int pPid)
	{
		//Log.d("MultiTaskManager","ProcessObject constructor");
		_label = pLabel;
		_package = pName;
		_icon = pIcon;
		_Pid=pPid;
		_Class= pClass;
	}
	
	@Override
	public String toString()
	{
		return "Process "+_package+"; icon: "+_icon.toString();		
	}

	public String get_package() {
		return _package;
	}
	
	public String get_Label() {
		return _label;
	}
	
	public String get_Class() {
		return _Class;
	}

	public Drawable get_icon() {
		return _icon;
	}

	public int get_Pid() {
		return _Pid;
	}
}
