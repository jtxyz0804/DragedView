package com.example.dragviewextend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener{

	private Button btnViewTwo;
	private Button btnViewOne;
	private LinearLayout viewThree;
	private GragMoveView gragView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnViewTwo = (Button)findViewById(R.id.view_two_button);
		btnViewOne = (Button)findViewById(R.id.view_one_button);
		viewThree = (LinearLayout)findViewById(R.id.view_three);
		gragView = (GragMoveView)findViewById(R.id.container);
		gragView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				if(view.getId() == R.id.view_three){
					Intent intent1 = new Intent();
	            	intent1.setClass(getApplicationContext(), Test.class);
	            	startActivity(intent1);
				}
				Log.i("GragMoveView", "position is : " + position);
			}
		});
		btnViewTwo.setOnClickListener(this);
		btnViewOne.setOnClickListener(this);
	//	viewThree.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		    case R.id.view_one_button:
		    	Intent intent = new Intent();
            	intent.setClass(getApplicationContext(), Test.class);
            	startActivity(intent);
			    break;
            case R.id.view_two_button:
            	Toast.makeText(getApplicationContext(), "Button two is clicked", Toast.LENGTH_SHORT).show();
			    break;
            case R.id.view_three:
            	Intent intent1 = new Intent();
            	intent1.setClass(getApplicationContext(), Test.class);
            	startActivity(intent1);
                break;
		    default:
			    break;
		}
	}

}
