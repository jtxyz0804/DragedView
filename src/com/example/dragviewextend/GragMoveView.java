package com.example.dragviewextend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GragMoveView extends ViewGroup implements OnTouchListener, OnLongClickListener{
	
 	private static final String TAG = "GragMoveView";
 	
	private int initXpos = 0;
	private int initYpos = 0;
	private int lastXpos = 0;
	private int lastYpos = 0;
	
	private int dragedView = 0;
	private boolean isViewDraging = false;
	
	//当前移动view的起始位置
	private int left = 0;
	private int top = 0;
	private int right = 0;
	private int bottom = 0;
	//交换view的位置
	private int newLeft = 0;
	private int newRight = 0;
	private int newTop = 0;
	private int newBottom = 0;
	
	//被按下view的矩形区域
	private Rect initRect;
	
	private boolean isAnimationStart = false;
	
	public GragMoveView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public GragMoveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public GragMoveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public void init(){
		setOnLongClickListener(this);
		setOnTouchListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		int childCount = getChildCount();
		int viewWidth = getMeasuredWidth();
		//放在里面与放在外面表现不一致,local 变量和全局变量
		int startXpos = 0;
		int startYpos = 0;
		for(int i = 0; i < childCount; i++){
			View childView = getChildAt(i);
			GragMoveView.LayoutParams params = (GragMoveView.LayoutParams) childView.getLayoutParams();
			int marginLeft = params.leftMargin;
			int marginRight = params.rightMargin;
			int marginTop = params.topMargin;
			int width  = childView.getMeasuredWidth();
		    int height = childView.getMeasuredHeight();
		    
		    if((startXpos + width + marginLeft + marginRight)> viewWidth){
		    	startXpos = left + marginLeft;
		    	startYpos += (height+marginTop);
		    }
		                     
			childView.layout(startXpos, startYpos, startXpos+width, startYpos+height);
		    startXpos += (width+marginLeft+marginRight);
		}
		
	}
    /**
     * 获取自定义view中的view的宽度和各个子view的宽度
     */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 这个方法默认返回的值为false,表示父类不拦截子类的消息
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(positionInView() != -1){
			int position = positionInView();
			dragedView = positionInView();
			Toast.makeText(getContext(), "the view is : " + position, Toast.LENGTH_LONG).show();
			
			bringDragedViewtoFront();
			isViewDraging = true;
			scaleDragedView();
		}
		return false;
	}

	/**
	 * ontouchlistener, dispatchtouchevent 会根据该方法返回的值，判断是否
	 * 执行onTouchEvent
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			initXpos = (int)event.getRawX();
			initYpos = (int)event.getRawY();
			break;
        case MotionEvent.ACTION_UP:
			//抬起如果没有拖动，恢复view的尺寸
        	if(isViewDraging){
        		getDragedView().layout(newLeft, newTop, newRight, newBottom);
        		restogeDragedView();
        		isViewDraging = false;
        	}else{
        		//do nothing
        	}
			break;
        case MotionEvent.ACTION_MOVE:
	        touchMove(event);
	        break;

		default:
			break;
		}
		return false;
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
	 * 1.根据lastXpos,lastYpos的位置判断移动的view和哪一个view相交
	 * 2.将两个相交的view的view的原始的位置记录，交换两个view的位置
	 */
	public void swapDragedView(int lastXpos, int lastYpos){
		int childCount = getChildCount();
		
		for(int i = 0; i < childCount; i++){
			if(i == dragedView)
				continue;
			View view  = getChildAt(i);
			if(inCurrentView(lastXpos, lastYpos, view)){
				newLeft = view.getLeft();
				newTop = view.getTop();
				newRight = view.getRight();
				newBottom = view.getBottom();
				view.layout(left, top, right, bottom);
			   //多次移动后，最后移动的view使用之前的位置
				left = newLeft;
				top = newTop;
				right = newRight;
				bottom = newBottom;
				break;
			}else{
				//如果dragedview和任何view都没有交换,仍然移动到原来的位置
				newLeft = left;
				newRight = right;
				newTop = top;
				newBottom = bottom;
			}
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
			if(i == dragedView)
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
			
			Log.i(TAG, "newleft is : "+newLeft + "; " + "newright is: " + newRight + 
					"; + newtop is :" + newTop + "; newbottom is: " + newBottom);
			Log.i(TAG, "oldPt x is : " + oldPt.x + "; oldPt y is : " + oldPt.y);
			Log.i(TAG, "newPt x is : " + newPt.x + "; newPt y is : " + newPt.y);
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
	//	Log.i("aaa", "oldPt.x-newPt.x is : " + (oldPt.x-newPt.x) + "; oldPt.y-newPt.y is : " + (oldPt.y-newPt.y));
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
		//transAnimation.setFillAfter(true);
		transAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		view.startAnimation(transAnimation);
		
	}
	
	public Point getViewCenterPt(int left, int top, int right, int bottom){
		Rect rect = new Rect(left, top, right, bottom);
		Point centerPt = new Point(rect.centerX(), rect.centerY());
		return centerPt;
	}
	
	/*
	 * 移动当前拖动的view
	 */
	public void moveDragedView(int xDis, int yDis){
		boolean canMove = false;
		
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
			//拖动view的矩形区域，必实际view缩小些
			int rLeft = ((int)(l*1.2));
			int rRight = ((int)(r*0.8));
			int rTop = ((int)(t*1.2));
			int rBottom = ((int)(b*0.8));
			int temp = 0;
			if(rLeft > rRight){
				temp = rRight;
				rRight = rLeft;
				rLeft = temp;
			}
			
			if(rTop > rBottom){
				temp = rBottom;
				rBottom = rTop;
				rTop = temp;
			}
			initRect = new Rect(rLeft, rTop, rRight, rBottom);
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
	public void scaleDragedView(){
		//可以判断是否在滚动状态
	    View view = getDragedView();
		ScaleAnimation scale = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, view.getWidth()/2, view.getHeight()/2);
		scale.setDuration(200);
		scale.setFillAfter(true);
		scale.setFillEnabled(true);
		
		view.clearAnimation();
		view.startAnimation(scale);
		
		//长按view后保存初始位置
		left = view.getLeft();
		top = view.getTop();
		right = view.getRight();
		bottom = view.getBottom();
		//长按后同时初始化拖动view的新位置
		//在没有拖动的情况下，长按松开的情况
		newLeft = left;
		newRight = right;
		newTop = top;
		newBottom = bottom;
		
		Log.i(TAG, "left is : "+left + "; " + "right is: " + right + 
				"; + top is :" + top + "bottom is: " + bottom);
		
	}
	
	public void bringDragedViewtoFront(){
	    View view = getDragedView();
	    if(view != null){
	    	//view.bringToFront();
	    	//通过这个来调用getChildDrawingOrder
	    	this.setChildrenDrawingOrderEnabled(true);
	    }
	}
	/*
	 * 调用getChildDrawingOrder来调整view的显示顺序
	 * 实现点击某个view后的放大显示
	 * @see android.view.ViewGroup#getChildDrawingOrder(int, int)
	 */
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		// TODO Auto-generated method stub
		if(dragedView < 0)
			return i;
		//最后画选中的view
		//Log.i(TAG, "isViewDraging");
		if(i == dragedView) {
            return childCount-1;
        }
        else if(i == (childCount - 1)) {
            return dragedView;
        }       
        else {
            return i;
        }
	}

	public View getDragedView(){
		View view = getChildAt(dragedView);
		if(view != null)
			return view;
		return null;
	}
	/**
	 * 通过鼠标按下后的位置以及每个vieW的位置来判断当前选择的view是哪一个
	 * @return
	 */
	public int positionInView(){
		int position = -1;
		for(int i = 0; i < getChildCount(); i++){
			View view = getChildAt(i);
			//initXpos是相对于屏幕左上角的坐标
//			int location[] = new int[2];
//			view.getLocationOnScreen(location);
//			int viewX = location[0];
//			int viewY = location[1];
//			if((initXpos > viewX && initXpos < (viewX+view.getWidth())) && (initYpos > viewY && initYpos < (viewY+view.getHeight()))){
//				position = i;
//				break;
//			}
			
			if(inCurrentView(initXpos, initYpos, view)){
				position = i;
				break;
			}
		}
		
		return position;
	}
	
	/*
	 * 判断两个view是否相交，通过缩小view所在的矩形区域判断
	 */
	public boolean isTwoViewInterscet(View view){
		//能不能有更好的缩小矩形的方法或者先将view scale一下再计算
		boolean isTwoViewInterscet = false;
		int vLeft = (int)(view.getLeft()*1.1);
		int vRight = (int)(view.getRight()*0.9);
		int vTop = (int)(view.getTop()*1.12);
		int vBottom = (int)(view.getBottom()*0.8);
		int temp = 0;
		if(vLeft > vRight){
			temp = vRight;
			vRight = vLeft;
			vLeft = temp;
		}
		
		if(vTop > vBottom){
			temp = vBottom;
			vBottom = vTop;
			vTop = temp;
		}
		Rect rect = new Rect(vLeft, vTop, vRight, vBottom);
		
		isTwoViewInterscet = initRect.intersect(rect);
		Log.i("ccc", "isTwoViewInterscet : " + isTwoViewInterscet);
		Log.i("ccc", "vLeft : " + vLeft +"; vTop : " + vTop + "; vRight" + vRight + "; vBottom" + vBottom);
		return isTwoViewInterscet;
	}
	
	public boolean inCurrentView(int xPos, int yPos, View view){
		boolean isCurrentView = false;
		int location[] = new int[2];
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];
		if((xPos > viewX && xPos < (viewX+view.getWidth())) && (yPos > viewY && yPos < (viewY+view.getHeight()))){
			isCurrentView = true;
		}
		
		return isCurrentView;
	}
	
	/**
	 * 如果要viewgroup支持view 的layout_margin属性，需要继承viewgroup的marginlayouparams
	 * @author Administrator
	 *
	 */
	public static class LayoutParams extends ViewGroup.MarginLayoutParams{

		public LayoutParams(Context arg0, AttributeSet arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		// TODO Auto-generated method stub
		return new GragMoveView.LayoutParams(getContext(), attrs);
	}
}
