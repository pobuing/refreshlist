package com.itcast01.refreshlistview.interf;

/**
 * @author andong
 * ˢ�����ݵļ����¼�
 */
public interface OnRefreshListener {

	/**
	 * ����ˢ������ʱ�ص�
	 */
	void onPullDownRefresh();
	
	/**
	 * ���ظ�������ʱ�ص�
	 */
	void onLoadingMore();
}
