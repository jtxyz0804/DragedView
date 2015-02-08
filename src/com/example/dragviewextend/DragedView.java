package com.example.dragviewextend;

import android.R.integer;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

public class DragedView extends ViewGroup {

	private Handler handler;
	private static final int longPressDelayTime = 1000;
	private boolean isViewDraging = false;
	private int initXpos = 0;
	private int initYpos = 0;
	private int lastXpos = 0;
	private int lastYpos = 0;
	private int dragedViewPosition = -1;

	// 当前移动view的起始位置
	private int left = 0;
	private int top = 0;
	private int right = 0;
	private int bottom = 0;
	// 交换view的位置
	private int newLeft = 0;
	private int newRight = 0;
	private int newTop = 0;
	private int newBottom = 0;

	// 被按下view的矩形区域
	private Rect initRect;

	private boolean isAnimationStart = false;

	public DragedView(Context context) {
		super(context);
		init();
	}

	public DragedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		handler = new Handler();
	}

	@Override
	protected void onLayout(boolean arg0, int left, int top, int right,
			int bottom) {
		int childCount = getChildCount();
		int viewWidth = getMeasuredWidth();
		// 放在里面与放在外面表现不一致,local 变量和全局变量
		int startXpos = 0;
		int startYpos = 0;
		// the preivew's marginright and width
		int preRight = 0;
		int preWidth = 0;
		for (int i = 0; i < childCount; i++) {
			boolean isFirstView = false;
			View childView = getChildAt(i);
			DragedView.LayoutParams params = (DragedView.LayoutParams) childView
					.getLayoutParams();
			int marginLeft = params.leftMargin;
			int marginRight = params.rightMargin;
			int marginTop = params.topMargin;
			int width = childView.getMeasuredWidth();
			int height = childView.getMeasuredHeight();
			// startXpos + width + marginLeft this is the start point of the
			// view
			// so here we need to using startXpos + width + marginLeft + width
			// to check if it is the first view in the line
			// check if the view is out
			int viewEnd = startXpos + preWidth + width + preRight + marginLeft;
			if ((viewEnd) > viewWidth) {
				isFirstView = true;
				startXpos = left + marginLeft;
				startYpos += (height + marginTop);
			}
			// for the first view
			if (i == 0) {
				isFirstView = true;
				startXpos = left + marginLeft;
				startYpos = top + marginTop;
			}
			// for the rest of view, we use the view's marginleft and the
			// preview's marginright
			if (!isFirstView) {
				if (i > 0) {
					View preView = getChildAt(i - 1);
					if (preView != null) {
						DragedView.LayoutParams preParam = (DragedView.LayoutParams) preView
								.getLayoutParams();
						preRight = preParam.rightMargin;
						preWidth = preView.getMeasuredWidth();
					}
				}
				startXpos += (preWidth + marginLeft + preRight);
			}

			childView.layout(startXpos, startYpos, startXpos + width, startYpos
					+ height);
			// we must make sure this part :
			// int viewEnd = startXpos + preWidth + width + preRight +
			// marginLeft;
			// using the correct data
			preWidth = width;

		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (isViewDraging) {
			// 由当前的onTouchEvent处理事件
			Log.i("test", "isViewDraging" + isViewDraging);
			return true;
		}
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i("test", "onInterceptTouchEvent ACTION_DOWN");
			initXpos = (int) event.getRawX();
			initYpos = (int) event.getRawY();
			if (positionInView() != -1) {
				handler.postDelayed(longPressRunable, longPressDelayTime);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("test", "onInterceptTouchEvent ACTION_MOVE");
			int moveX = (int) event.getRawX();
			int moveY = (int) event.getRawY();
          
			Log.i("test", "onInterceptTouchEvent ACTION_MOVE dragedViewPosition" + dragedViewPosition);
			
			// 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
			//dragedViewPosition is -1, and the longpressrunable is removed
			if (!inCurrentView(moveX, moveY, getDragedView())) {
				if (dragedViewPosition != -1) {
					handler.removeCallbacks(longPressRunable);
					isViewDraging = false;
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			Log.i("test", "onInterceptTouchEvent ACTION_UP");
			isViewDraging = false;
			handler.removeCallbacks(longPressRunable);
			break;
		default:
			break;
		}
		return super.onInterceptTouchEvent(event);
	}
	
	/**
	 * this onTouchEvent return false
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i("test", "onTouchEvent ACTION_DOWN");
			break;
		case MotionEvent.ACTION_UP:
			Log.i("test", "onTouchEvent ACTION_UP");
			if(isViewDraging){
        		getDragedView().layout(newLeft, newTop, newRight, newBottom);
        		restogeDragedView();
        		dragedViewPosition = -1;
        		//the initRect must be null when you up
        		//initRect = null;
        		isViewDraging = false;
        		isAnimationStart = false;
        	}
			handler.removeCallbacks(longPressRunable);
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("test", "onTouchEvent ACTION_MOVE");
			touchMove(event);
			break;

		default:
			break;
		}
		// return true means this onTouchEvent will take the massage
		return true;
	}

	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}

		return true;
	}

	private Runnable longPressRunable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isViewDraging = true;
			int position = positionInView();
			dragedViewPosition = positionInView();
			Log.i("test", "isViewDraging longPressRunable dragedViewPosition is" + dragedViewPosition);
			bringDragedViewtoFront();
			isViewDraging = true;
			scaleDragedView();
		}
	};

	public void scaleDragedView() {
		// 可以判断是否在滚动状态
		View view = getDragedView();
		ScaleAnimation scale = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
				view.getWidth() / 2, view.getHeight() / 2);
		scale.setDuration(200);
		scale.setFillAfter(true);
		scale.setFillEnabled(true);

		view.clearAnimation();
		view.startAnimation(scale);

		// 长按view后保存初始位置
		left = view.getLeft();
		top = view.getTop();
		right = view.getRight();
		bottom = view.getBottom();
		// 长按后同时初始化拖动view的新位置
		// 在没有拖动的情况下，长按松开的情况
		newLeft = left;
		newRight = right;
		newTop = top;
		newBottom = bottom;

	}

	public void bringDragedViewtoFront() {
		View view = getDragedView();
		if (view != null) {
			// view.bringToFront();
			// 通过这个来调用getChildDrawingOrder
			this.setChildrenDrawingOrderEnabled(true);
		}
	}

	public View getDragedView() {
		View view = getChildAt(dragedViewPosition);
		if (view != null)
			return view;
		return null;
	}

	public void touchMove(MotionEvent event){
		if(isViewDraging){
			//getRawX指的是相对于屏幕左上角的坐标，getX指的是触摸点相对于当前控件的坐标
			lastXpos = (int)event.getRawX();
			lastYpos = (int)event.getRawY();
			int xDistance = lastXpos - initXpos;
			int yDistance = lastYpos - initYpos;
			initXpos = lastXpos;
			initYpos = lastYpos;
			
			moveDragedView(xDistance, yDistance);
			//去除拖动幻影
			invalidate();
			//没有动画时才进行继续交换
			if(!isAnimationStart)
			    swapDragedViewPro(lastXpos, lastYpos);
		}
	}
	
	/*
	 * 1.这个方法是对swapDragedView方法的优化，通过两个view的中心点来移动view
	 */
	public void swapDragedViewPro(int lastXpos, int lastYpos){
		int childCount = getChildCount();
		View view = null;
		Point oldPt = null;
		Point newPt = null;
		for(int i = 0; i < childCount; i++){
			if(i == dragedViewPosition)
				continue;
			if(isTwoViewInterscet(getChildAt(i))){
				view = getChildAt(i);
				break;
			}
			
		}
		//isAnimationStart 如果动画正常进行，不允许再次进入
		if(view != null && !isAnimationStart){
			newLeft = view.getLeft();
			newTop = view.getTop();
			newRight = view.getRight();
			newBottom = view.getBottom();
			oldPt = getViewCenterPt(newLeft, newTop, newRight, newBottom);
			newPt = getViewCenterPt(left, top, right, bottom);
			
			startMoveView(view, newPt, oldPt);
		
		}
		
		if(view == null){
			//如果dragedview和任何view都没有交换,仍然移动到原来的位置
			newLeft = left;
			newRight = right;
			newTop = top;
			newBottom = bottom;
		}
	}
	
	public void startMoveView(final View view, final Point newPt, final Point oldPt){
			TranslateAnimation transAnimation = new TranslateAnimation(0, -oldPt.x+newPt.x, 0, -oldPt.y+newPt.y);
			transAnimation.setDuration(500);
			transAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					isAnimationStart = true;
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					view.clearAnimation();
					//由于是在move aciton中，view的位置一直变化，造成view.getLeft的值获取一直不正确
					int left1 = newLeft-oldPt.x+newPt.x;
					int top1 = newTop-oldPt.y+newPt.y;
					int right1= left1 + view.getWidth();
					int bottom1 = top1 + view.getHeight();
					Log.i("aaa", "left1 is : "+left1 + "; " + "right1 is: " + right1 + 
							"; + top1 is :" + top1 + "bottom1 is: " + bottom1);
					view.layout(left1, top1, right1, bottom1);
					left = newLeft;
					right = newRight;
					top = newTop;
					bottom = newBottom;
					isAnimationStart = false;
				}
			});
			transAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
			view.startAnimation(transAnimation);
			
		}
	
	public Point getViewCenterPt(int left, int top, int right, int bottom){
		Rect rect = new Rect(left, top, right, bottom);
		Point centerPt = new Point(rect.centerX(), rect.centerY());
		return centerPt;
	}
	
	/*
	 * 判断两个view是否相交，通过缩小view所在的矩形区域判断
	 */
	public boolean isTwoViewInterscet(View view){
		//change this method to use half rect of the view to check 
		boolean isTwoViewInterscet = false;
		Rect rawRect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		int centerX = rawRect.centerX();
		int centerY = rawRect.centerY();
		int width = view.getWidth();
		int height = view.getHeight();
		Rect rect = new Rect(centerX-width/4, centerY-height/4, centerX+width/4, centerY+height/4);
		
		if(initRect != null && rect != null){
			isTwoViewInterscet = initRect.intersect(rect);
		}
		Log.i("ccc", "isTwoViewInterscet : " + isTwoViewInterscet);
		return isTwoViewInterscet;
	}
	
	/*
	 * 移动当前拖动的view
	 */
	public void moveDragedView(int xDis, int yDis){
		boolean canMove = false;
		//verytime we need to set initRect to null
		if(initRect != null){
			initRect = null;
		}
		View view = getDragedView();
		int l = view.getLeft()+xDis;
		int t = view.getTop()+yDis;
		int r = view.getLeft()+xDis+view.getWidth();
		int b = view.getTop()+yDis+view.getHeight();
		//限制view不移动出当前的view
		if(r < getRight() && (l > getLeft()) && (t > getTop()) && (b < getBottom())){
			canMove = true;
		}
		if(view != null && canMove){
			view.layout(l, t, r, b);
			Rect rawRect = new Rect(l, t, r, b);
			int centerX = rawRect.centerX();
			int centerY = rawRect.centerY();
			int width = view.getWidth();
			int height = view.getHeight();
			initRect = new Rect(centerX-width/4, centerY-height/4, centerX+width/4, centerY+height/4);
		}
	}
	
	public void restogeDragedView(){
		View view = getDragedView();
	    ScaleAnimation scale = new ScaleAnimation(1.2f, 1f, 1.2f, 1f, view.getWidth()/2, view.getHeight()/2);
		scale.setDuration(200);
		scale.setFillAfter(true);
		scale.setFillEnabled(true);
			
		view.clearAnimation();
		view.startAnimation(scale);
	
	}
	
	public int positionInView() {
		int position = -1;
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			if (inCurrentView(initXpos, initYpos, view)) {
				position = i;
				break;
			}
		}
		return position;
	}

	public boolean inCurrentView(int xPos, int yPos, View view) {
		boolean isCurrentView = false;
		int location[] = new int[2];
		//this means the longpress is not ready
		if (view == null) {
			return false;
		}
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];
		if ((xPos > viewX && xPos < (viewX + view.getWidth()))
				&& (yPos > viewY && yPos < (viewY + view.getHeight()))) {
			isCurrentView = true;
		}

		return isCurrentView;
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		// TODO Auto-generated method stub
		if (dragedViewPosition < 0)
			return i;
		// 最后画选中的view
		// Log.i(TAG, "isViewDraging");
		if (i == dragedViewPosition) {
			return childCount - 1;
		} else if (i == (childCount - 1)) {
			return dragedViewPosition;
		} else {
			return i;
		}
	}

	/**
	 * 如果要viewgroup支持view 的layout_margin属性，需要继承viewgroup的marginlayouparams
	 * 
	 * @author Administrator
	 * 
	 */
	public static class LayoutParams extends ViewGroup.MarginLayoutParams {

		public LayoutParams(Context arg0, AttributeSet arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		// TODO Auto-generated method stub
		return new DragedView.LayoutParams(getContext(), attrs);
	}
}
