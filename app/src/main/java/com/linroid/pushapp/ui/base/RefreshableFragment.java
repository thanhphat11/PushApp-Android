package com.linroid.pushapp.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linroid.pushapp.R;
import com.linroid.pushapp.util.SampleItemDecoration;
import com.linroid.pushapp.view.ContentLoaderView;

/**
 * Created by linroid on 7/20/15.
 */
public abstract class RefreshableFragment extends Fragment
        implements ContentLoaderView.OnRefreshListener, ContentLoaderView.OnMoreListener {
    protected RecyclerView recyclerView;
    protected ContentLoaderView loaderView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refreshable, container, false);

        loaderView = (ContentLoaderView) view.findViewById(R.id.content_loader);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SampleItemDecoration(getResources().getDimensionPixelSize(R.dimen.item_divider_space)));
        loaderView.setAdapter(getAdapter());
        loaderView.setOnRefreshListener(this);
        loaderView.setMoreListener(this);
        return view;
    }

    public abstract RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getAdapter();

    @Override
    public void onRefresh(boolean fromSwipe) {
        loadData(1);
    }

    @Override
    public void onMore(int page) {
        loadData(page);
    }

    public abstract void loadData(int page);

    protected void refresh() {
        onRefresh(false);
    }
}
