package msl.com.widgets;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class SearchableAdapter extends BaseAdapter implements Filterable {
	AbstractListBuilder mBuilder;
	List<Object> mData;
	boolean mIsFiltZero;

	public SearchableAdapter(AbstractListBuilder builder, boolean filtZero) {
		mBuilder = builder;
		mIsFiltZero = filtZero;
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData == null ? null : mData.get(position);
	}

	// Temporary use for Searchable
	// Because of problem in landscape mode

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
		mBuilder.buildViewHolderContentData(position, holder.views, mData);
		return convertView;
	}

	public void updateData(List<Object> source) {
		List<Object> data = new ArrayList<Object>();
		Collections.copy(data, source);
		synchronized (mData) {
			mData.clear();
			mData = data;
		}
	}
	
	private Filter mFilter;

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new MyFilter();
		}
		return mFilter;
	}

	private final Object mLock = new Object();

	private class MyFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if (mData == null) {
				mData = new ArrayList<Object>();
			}
			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					List<Object> list;
					if (mIsFiltZero) {
						list = mData;
					} else {
						list = new ArrayList<Object>();
					}
					results.values = list;
					results.count = list.size();
				}
			} else {
				String prefixString = prefix.toString().toLowerCase();
				final List<Object> values = mData;
				final int count = values.size();
				final List<Object> newValues = new ArrayList<Object>();

				for (int i = 0; i < count; i++) {
					final Object value = values.get(i);
					final String valueText = ((ISearchableObj) value)
							.getSearchableData()
							.toLowerCase();
					if (valueText.startsWith(prefixString)
								|| (valueText.startsWith("+") && valueText
										.contains(prefixString))) {
							newValues.add(value);
						} else {
							final String[] words = valueText.split(" ");
							final int wordCount = words.length;
							for (int k = 0; k < wordCount; k++) {
								if (words[k].startsWith(prefixString)
										|| (words[k].startsWith("+") && words[k]
												.contains(prefixString))) {
									newValues.add(value);
									break;
								}
							}
						}
				}
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mData = (List<Object>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}

	}

}
