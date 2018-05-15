package com.shahroz.svlibrary.interfaces;

public interface onSimpleSearchActionsListener<T> {
    void onItemClicked(T item);

    void onScroll();

    void error(String localizedMessage);
}
