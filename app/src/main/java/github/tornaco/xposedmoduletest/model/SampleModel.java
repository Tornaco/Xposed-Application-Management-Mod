package github.tornaco.xposedmoduletest.model;

import ir.mirrajabi.searchdialog.core.Searchable;

public class SampleModel implements Searchable {
    private String mTitle;

    public SampleModel(String title) {
        mTitle = title;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public SampleModel setTitle(String title) {
        mTitle = title;
        return this;
    }
}
