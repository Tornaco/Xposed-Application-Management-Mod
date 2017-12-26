package com.shahroz.svlibrary.interfaces;

public interface onSearchListener {
    void onSearch(String query);

    void searchViewOpened();

    void searchViewClosed();

    void onCancelSearch();
}
