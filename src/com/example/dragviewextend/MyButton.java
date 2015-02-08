package com.example.dragviewextend;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;

public class MyButton extends Button implements OnLongClickListener {

	private CheckForLongPress mPendingCheckForLongPress;
    public boolean isLongClicked = false;
	public MyButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		//setOnLongClickListener(this);
	}

	public MyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		//setOnLongClickListener(this);
	}

	public MyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		//setOnLongClickListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i("MyButton",
				"super.onTouchEvent(event) is : " + super.onTouchEvent(event));
		// ����button��clickable�ģ�����Ĭ�������onTouchEvent�ķ���ֵΪtrue,
		// ������imageview, textview, layout�ȣ�����Ĭ��������Ƿ�clickable�ģ�����
		// onTouchEvent���ص�ֵΪfalse, ������Ϣ�ͻᴫ�ݵ����ĸ���������
		// �ڴ˴��ж��Ƿ�Ϊ����
	    super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			checkLongClick(0);
			Log.i("MyButton1", "ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("MyButton1", "ACTION_MOVE");
			break;
		case MotionEvent.ACTION_UP:
			isLongClicked = false;
			Log.i("MyButton1", "ACTION_UP");
			break;
		default:
			break;
		}
        
//		if(isLongClicked){
//			Log.i("MyButton1", "isLongClicked is : " + isLongClicked);
//			return false;
//		}
//		
//			Log.i("MyButton1", "return true   ++" + isLongClicked);
//			return true;
		Log.i("MyButton1", "isLongClicked is : " + isLongClicked);
		return true;
	}

	public void checkLongClick(int delayOffset) {
		Log.i("MyButton1", "checkForLongClick");
		if (mPendingCheckForLongPress == null) {
			mPendingCheckForLongPress = new CheckForLongPress();
		}

		postDelayed(mPendingCheckForLongPress,
				ViewConfiguration.getLongPressTimeout() - delayOffset);
	}

	class CheckForLongPress implements Runnable {

		private int mOriginalWindowAttachCount;

		public void run() {
			Log.i("MyButton1", "run");
			if (isPressed()) {
				Log.i("MyButton1", "isPressed");
				isLongClicked = true;
			}
		}

	}

	@Override
	public boolean onLongClick(View arg0) {
		Log.i("MyButton", "onLongClick");
		// TODO Auto-generated method stub
		return false;
	}
}
