package com.itcast01.refreshlistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itcast01.refreshlistview.interf.OnRefreshListener;
import com.itcast01.refreshlistview.view.RefreshListView;

public class MainActivity extends Activity {

	private List<String> dataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final RefreshListView mListView = (RefreshListView) findViewById(R.id.refresh_listview);

		dataList = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			dataList.add("这是一条ListView的数据" + i);
		}

		final MyAdapter adapter = new MyAdapter();
		mListView.setAdapter(adapter);
		mListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onPullDownRefresh() {
				// 刷新数据.

				new AsyncTask<Void, Void, Void>() {

					/**
					 * 在onPreExecute执行完之后, 执行, 运行在子线程中.
					 */
					@Override
					protected Void doInBackground(Void... params) {
						SystemClock.sleep(2000);
						dataList.add(0, "这是刷新出来的最新微博");
						return null;
					}

					/**
					 * 在doInBackground方法执行之后, 调用. 运行在主线程中
					 */
					@Override
					protected void onPostExecute(Void result) {
						adapter.notifyDataSetChanged();

						// 调用REfreshListView中的方法, 隐藏头布局
						mListView.hideHeaderView();
					}
				}.execute(new Void[] {}); // 调用onPreExecute
			}

			@Override
			public void onLoadingMore() {
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						SystemClock.sleep(5000);
						dataList.add("加载跟多出来的数据1");
						dataList.add("加载跟多出来的数据2");
						dataList.add("加载跟多出来的数据3");
						dataList.add("加载跟多出来的数据4");
						dataList.add("加载跟多出来的数据5");
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						adapter.notifyDataSetChanged();

						// 脚布局隐藏
						mListView.hideFooterView();
					}

				}.execute(new Void[] {});
			}
		});
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = null;
			if (convertView == null) {
				tv = new TextView(MainActivity.this);
			} else {
				tv = (TextView) convertView;
			}

			tv.setText(dataList.get(position));
			tv.setTextSize(18);
			return tv;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
