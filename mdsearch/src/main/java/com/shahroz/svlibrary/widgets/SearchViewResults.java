package com.shahroz.svlibrary.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shahroz.svlibrary.interfaces.onSearchActionsListener;

import java.util.ArrayList;

public class SearchViewResults<T> implements
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final int TRIGGER_SEARCH = 1;
    private static final long SEARCH_TRIGGER_DELAY_IN_MS = 400;
    private String sequence;
    private int mPage;
    private SearchTask mSearch;
    private Handler mHandler;
    private boolean isLoadMore;
    private ArrayAdapter<T> mAdapter;

    private onSearchActionsListener<T> mListener;
    /*
    * Used Handler in case implement Search remotely
    * */

    public SearchViewResults(Context context, String searchQuery) {
        sequence = searchQuery;
        ArrayList<T> searchList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, searchList);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_SEARCH) {
                    clearAdapter();
                    String sequence = (String) msg.obj;
                    mSearch = new SearchTask();
                    mSearch.execute(sequence);
                }
                return false;
            }
        });
    }

    public void setListView(ListView listView) {
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        listView.setAdapter(mAdapter);
        updateSequence();
    }

    public void updateSequence(String s) {
        sequence = s;
        updateSequence();
    }

    private void updateSequence() {
        mPage = 0;
        isLoadMore = true;

        if (mSearch != null) {
            mSearch.cancel(false);
        }
        if (mHandler != null) {
            mHandler.removeMessages(TRIGGER_SEARCH);
        }
        if (!sequence.isEmpty()) {
            Message searchMessage = new Message();
            searchMessage.what = TRIGGER_SEARCH;
            searchMessage.obj = sequence;
            mHandler.sendMessageDelayed(searchMessage, SEARCH_TRIGGER_DELAY_IN_MS);
        } else {
            isLoadMore = false;
            clearAdapter();
        }
    }

    private void clearAdapter() {
        mAdapter.clear();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemClicked((T) mAdapter.getItem(position));

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            mListener.onScroll();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
        if (loadMore && isLoadMore) {
            mPage++;
            isLoadMore = false;
//            mSearch = new SearchTask();
//            mSearch.execute(sequence);
        }
    }

    /*
    * Implement the Core search functionality here
    * Could be any local or remote
    */
    private ArrayList<T> findItem(String query, int page) {
        return mSearchPerformer.findItem(query, page);
    }

    private SearchPerformer<T> mSearchPerformer;

    public interface SearchPerformer<T> {
        @WorkerThread
        @NonNull
        ArrayList<T> findItem(String query, int page);
    }

    public void setSearchPerformer(SearchPerformer<T> performer) {
        this.mSearchPerformer = performer;
    }

    public void setSearchProvidersListener(onSearchActionsListener<T> listener) {
        this.mListener = listener;
    }

    /*
    * Search for the item asynchronously
    */
    @SuppressLint("StaticFieldLeak")
    private class SearchTask extends AsyncTask<String, Void, ArrayList<T>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.showProgress(true);
        }

        @Override
        protected ArrayList<T> doInBackground(String... params) {
            String query = params[0];
            return findItem(query, mPage);
        }

        @Override
        protected void onPostExecute(ArrayList<T> result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                mListener.showProgress(false);
                if (mPage == 0 && result.isEmpty()) {
                    mListener.listEmpty();
                } else {
                    mAdapter.notifyDataSetInvalidated();
                    mAdapter.clear();
                    mAdapter.addAll(result);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

}


