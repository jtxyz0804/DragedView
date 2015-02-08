package com.example.dragviewextend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Test extends Activity implements OnClickListener {

	private Button btnViewNewOne;

	public Test() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_new);
		btnViewNewOne = (Button) findViewById(R.id.view_one_button_new);

		btnViewNewOne.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.view_one_button_new:
			Toast.makeText(getApplicationContext(), "the button is clicked",
					Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
}
