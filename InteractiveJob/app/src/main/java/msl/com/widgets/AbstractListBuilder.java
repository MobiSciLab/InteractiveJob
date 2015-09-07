package msl.com.widgets;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import msl.com.interactivejob.R;

public abstract class AbstractListBuilder extends AbstractBuilder {
	protected MyAdapter mAdapter;
    protected Context mContext;
	public AbstractListBuilder(Context context, ViewGroup parent) {
		super(context, parent);
        mContext = context;
	}

	protected void showEmpty(boolean isVisible, String content) {
		TextView tvEmpty = (TextView) mParentView.findViewById(android.R.id.empty);
        tvEmpty.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		tvEmpty.setText(content != null? content : mContext.getString(R.string.txt_no_data));
    }
	@Override
	protected void buildLayout(ViewGroup parent) {
		ListView listView = (ListView) parent
				.findViewById(android.R.id.list);
		mAdapter = new MyAdapter(this);
		listView.setAdapter(mAdapter);
	}

	@Override
	public void build() {
		buildLayout(mParentView);
	}
	
	protected abstract void notifyDataChange();
	
	@Override
	public synchronized void refresh() {
		new BuildData().execute();
	}
	
	class BuildData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mAdapter.updateData(buildData());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			notifyDataChange();
		}
		
	}
	
	public MyAdapter getAdapter() {
		return mAdapter;
	}
	
}
