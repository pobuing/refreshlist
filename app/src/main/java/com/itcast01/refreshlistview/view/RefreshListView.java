package com.itcast01.refreshlistview.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itcast01.refreshlistview.R;
import com.itcast01.refreshlistview.interf.OnRefreshListener;

public class RefreshListView extends ListView implements OnScrollListener {
	
	private int mFirstVisibleItem = -1; // ����ʱ��Ļ�ϵ�һ����ʾitem��position
	private int downY; // ����ʱy���ƫ����
	private int headerViewHeight; // ͷ���ֵĸ߶�
	private View headerView; // ͷ���ֶ���

	private final int PULL_DOWN_REFRESH = 0; // ����ˢ��״̬
	private final int RELEASE_REFRESH = 1; // �ͷ�ˢ��
	private final int REFRESHING = 2; // ����ˢ����
	
	private int currentHeaderState = PULL_DOWN_REFRESH; // ��ǰͷ���ֵ�״̬, Ĭ��Ϊ: ����ˢ��
	private ImageView ivArrow; // ��ͷ
	private ProgressBar mProgressBar; // ������
	private TextView tvState; // ͷ���ֵ�״̬
	private TextView tvLastUpdateTime; // ͷ���ֵ����ˢ��ʱ��

	private Animation upAnimation; // ������ת�Ķ���
	private Animation downAnimation; // ������ת�Ķ���
	
	private OnRefreshListener mOnRefreshListener; // �û���ˢ�»ص��¼�
	private View footerView; // �Ų��ֶ���
	private int footerViewHeight; // �Ų��ָ߶�
	private boolean isLoadingMore = false; // �Ƿ����ڼ��ظ�����

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// ��ͷ����
		initHeader();
		
		// �ӽŲ���
		initFooter();
		
		setOnScrollListener(this);
	}

	/**
	 * ��ʼ���Ų���
	 */
	private void initFooter() {
		footerView = View.inflate(getContext(), R.layout.listview_footer, null);
		
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		System.out.println("�Ų��ֵĸ߶�: " + footerViewHeight);
		
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		
		addFooterView(footerView);
	}

	private void initHeader() {
		headerView = View.inflate(getContext(), R.layout.listview_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
		mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_listview_header_progress);
		tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
		tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);
		
		tvLastUpdateTime.setText("����ˢ��ʱ��: " + getLastUpdateTime());
		
		// �ֶ�����headerView�Ĳ�������, ���������ǲ���
		headerView.measure(0, 0);
		
		// getHeight ֻ���ڲ����Ѿ���ʾ����Ļ��֮��ſ��Ի�ȡ��ֵ.
		// getMeasuredHeight ������measure����ִ��֮��, �õ��߶�
		headerViewHeight = headerView.getMeasuredHeight();
		System.out.println("ͷ���ָ߶� : " + headerViewHeight);
		
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		
		addHeaderView(headerView);
		
		initAnimation();
	}
	
	/**
	 * ��ʼ��ͷ���ֵĶ���
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(
				0, -180, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true); 

		downAnimation = new RotateAnimation(
				-180, -360, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true); 
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN: // ����
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE: // �ƶ�
			// �жϵ�ǰ�Ƿ�������ˢ����
			if(currentHeaderState == REFRESHING) {
				// ��ǰ����ˢ��, ֱ������.
				break;
			}
			
			int moveY = (int) ev.getY();
			
			int diff = moveY - downY;
			System.out.println("downY---="+downY);
			System.out.println("moveY---="+moveY);
			System.out.println("diff-----="+diff);
			if(mFirstVisibleItem == 0 && diff > 0) { // ��ǰ��Ļ���ڶ���, �����������϶�
				int paddingTop = -headerViewHeight + diff;
				System.out.println("paddingTop-------="+paddingTop);
				if(paddingTop > 0 && currentHeaderState == PULL_DOWN_REFRESH) {
					// ��ȫ��ʾ, ���뵽�ͷ�ˢ��״̬
					System.out.println("�ͷ�ˢ��");
					currentHeaderState = RELEASE_REFRESH;
					refreshHeaderState();
				} else if(paddingTop < 0 && currentHeaderState == RELEASE_REFRESH) {
					// ������һ����, ���뵽����ˢ��״̬
					System.out.println("����ˢ��");
					currentHeaderState = PULL_DOWN_REFRESH;
					refreshHeaderState();
				}
				
				headerView.setPadding(0, paddingTop, 0, 0);
				return true; // �Լ���Ӧ�û����¼�
			}
			break;
		case MotionEvent.ACTION_UP: // ̧��
			if(currentHeaderState == PULL_DOWN_REFRESH) {
				// ��ǰ������ˢ��̧���, ����ͷ����
				headerView.setPadding(0, -headerViewHeight, 0, 0);
			} else if(currentHeaderState == RELEASE_REFRESH) {
				// ��ǰ���ͷ�ˢ��̧���, ��ͷ���ֵ�paddingTop��Ϊ0, ���ҽ��뵽����ˢ����
				headerView.setPadding(0, 0, 0, 0);
				currentHeaderState = REFRESHING;
				refreshHeaderState();
				
				// �����û��Ļص��¼�
				if(mOnRefreshListener != null) {
					mOnRefreshListener.onPullDownRefresh();
				}
			}
			break;
		default:
			break;
		}
		
		return super.onTouchEvent(ev); // ���ø���Ĭ�ϵĹ���Ч��
	}
	
	/**
	 * ����currentHeaderState��ˢ��ͷ���ֵ�״̬
	 */
	private void refreshHeaderState() {
		switch (currentHeaderState) {
		case PULL_DOWN_REFRESH: // ����ˢ��
			tvState.setText("����ˢ��");
			ivArrow.startAnimation(downAnimation);
			break;
		case RELEASE_REFRESH: // �ͷ�ˢ��
			tvState.setText("�ͷ�ˢ��");
			ivArrow.startAnimation(upAnimation);
			break;
		case REFRESHING: // ����ˢ����
			ivArrow.setVisibility(View.INVISIBLE);
			ivArrow.clearAnimation(); // ���imageView���ϵĶ���
			mProgressBar.setVisibility(View.VISIBLE);
			tvState.setText("����ˢ����...");
			break;
		default:
			break;
		}
	}

	/**
	 * ������ʱ����.
	 * firstVisibleItem ����ʱ��Ļ�ϵ�һ���ɼ���item��position
	 * visibleItemCount ��Ļ����ʾ�˶�����item
	 * totalItemCount ListView���ܳ���
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
//		System.out.println("������: " + firstVisibleItem);
	}

	/**
	 * ������״̬�ı�ʱ, �ص��˷���
	 * scrollState ��ǰ������״̬
	 * 
	 * SCROLL_STATE_IDLE ͣ��״̬
	 * SCROLL_STATE_TOUCH_SCROLL ��ָ�����ƶ���״̬
	 * SCROLL_STATE_FLING �͵�һ��
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// ������״̬�Ǵ���ֹͣ���߿��ٻ���ʱ, ���ҵ�ǰ��Ļ���һ����ʾ��item��������ListView����Ŀ-1
		if((scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING)
				&& getLastVisiblePosition() == (getCount() -1)) {
			
			if(!isLoadingMore) {
				System.out.println("��ǰ�������ײ���, ���Լ��ظ���");
				isLoadingMore = true;
				// ��ʾ�Ų���
				footerView.setPadding(0, 0, 0, 0);
				
				// �������ײ�, �ýŲ�����ʾ����
				setSelection(getCount() -1);
				
				// �����û��Ļص��¼�, ���û�ȥ���ظ�������
				if(mOnRefreshListener != null) {
					mOnRefreshListener.onLoadingMore();
				}
			}
		}
		
	}
	
	public void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
	}
	
	/**
	 * ����ͷ����
	 */
	public void hideHeaderView() {
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		currentHeaderState = PULL_DOWN_REFRESH;
		mProgressBar.setVisibility(View.INVISIBLE);
		ivArrow.setVisibility(View.VISIBLE);
		
		// �����µ�ʱ�����ø�����ˢ��ʱ��Ŀؼ�
		tvLastUpdateTime.setText("���ˢ��ʱ��: " + getLastUpdateTime());
	}
	
	/**
	 * ������µ�ʱ��
	 * 1990-09-09 09:09:09
	 * @return
	 */
	private String getLastUpdateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	/**
	 * ���ؽŲ���
	 */
	public void hideFooterView() {
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		isLoadingMore = false;
	}
}
