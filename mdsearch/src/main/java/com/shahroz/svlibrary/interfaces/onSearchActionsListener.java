package com.shahroz.svlibrary.interfaces;

/**
 * Created by shahroz on 1/12/2016.
 * Email: Tornaco@163.com
 */
public interface onSearchActionsListener<T> {
    void onItemClicked(T item);
    void showProgress(boolean show);
    void listEmpty();
    void onScroll();
    void error(String localizedMessage);
}
