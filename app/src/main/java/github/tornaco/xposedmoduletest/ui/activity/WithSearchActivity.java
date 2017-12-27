package github.tornaco.xposedmoduletest.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.shahroz.svlibrary.interfaces.onSearchListener;
import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.widgets.MaterialSearchView;
import com.shahroz.svlibrary.widgets.SearchViewResults;

import java.util.ArrayList;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/12/26.
 * Email: Tornaco@163.com
 */

@SuppressLint("Registered")
public class WithSearchActivity<T> extends BaseActivity
        implements onSearchListener, onSimpleSearchActionsListener<T>,
        SearchViewResults.SearchPerformer<T> {

    private boolean mSearchViewAdded = false;
    protected MaterialSearchView<T> mSearchView;
    private WindowManager mWindowManager;
    private boolean searchActive = false;

    @Override
    protected void setupToolbar() {
        super.setupToolbar();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mSearchView = new MaterialSearchView<>(this);
        mSearchView.setOnSearchListener(this);
        mSearchView.setSearchResultsListener(this);
        mSearchView.setSearchPerformer(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            // Delay adding SearchView until Toolbar has finished loading
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    if (!mSearchViewAdded && mWindowManager != null) {
                        mWindowManager.addView(mSearchView,
                                MaterialSearchView.getSearchViewLayoutParams(getActivity()));
                        mSearchViewAdded = true;
                    }
                }
            });
        }
    }

    protected void openKeyboard() {
        getUIThreadHandler().postDelayed(new Runnable() {
            public void run() {
                mSearchView.getSearchView().dispatchTouchEvent(
                        MotionEvent.obtain(SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                mSearchView.getSearchView().dispatchTouchEvent(
                        MotionEvent.obtain(SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 200);
    }

    @Override
    public void onSearch(String query) {

    }

    @Override
    public void searchViewOpened() {

    }

    @Override
    public void searchViewClosed() {

    }

    @Override
    public void onCancelSearch() {
        searchActive = false;
        mSearchView.hide();
    }

    @Override
    public void onItemClicked(T item) {

    }

    @Override
    public void onScroll() {

    }

    @Override
    public void error(String localizedMessage) {
        Toast.makeText(getContext(), localizedMessage, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public ArrayList<T> findItem(String query, int page) {
        return new ArrayList<>(0);
    }
}
