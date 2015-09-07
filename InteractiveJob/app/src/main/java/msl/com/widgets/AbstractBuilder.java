package msl.com.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbstractBuilder {
	protected ViewGroup mParentView;
	protected LayoutInflater mLayoutInflater;
	
	protected abstract void buildLayout(ViewGroup parent);

	// Constructor
	public AbstractBuilder(Context context, ViewGroup parent) {
		mParentView = parent;
		mLayoutInflater = LayoutInflater.from(context);
	}

	// Internal build product
	protected abstract List<Object> buildData();
	
	public abstract void build();

	public abstract void refresh();
	
	public abstract View buildViewHolder();

	public abstract List<View> buildViewHolderContent(View base);

	public abstract void buildViewHolderContentData(int position,
			List<View> views, List<Object> data);

}
