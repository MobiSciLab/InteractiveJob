package msl.com.widgets;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyAdapter extends BaseAdapter {
	AbstractBuilder mBuilder;
	private List<Object> mData;

	public MyAdapter(AbstractBuilder builder) {
		mBuilder = builder;
		mData = new ArrayList<Object>();
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData == null ? null : mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		List<View> views;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mBuilder.buildViewHolder();
			holder.views = mBuilder.buildViewHolderContent(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		mBuilder.buildViewHolderContentData(position, holder.views,
				mData);
		return convertView;
	}
	
	public void updateData(List<Object> source) {
		List<Object> data = new ArrayList<Object>(source);
		Collections.copy(data, source);
		synchronized (mData) {
			mData.clear();
			mData = data;
		}
	}
	
	public void removeItem(int position) {
		synchronized (mData) {
			mData.remove(position);
		}
		notifyDataSetChanged();
	}
	
	public void addItem(Object obj, int position) {
		synchronized (mData) {
			mData.add(position, obj);
		}
		notifyDataSetChanged();
	}
}
