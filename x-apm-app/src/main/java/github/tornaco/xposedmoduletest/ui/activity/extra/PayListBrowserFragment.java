package github.tornaco.xposedmoduletest.ui.activity.extra;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.Contribution;
import github.tornaco.xposedmoduletest.remote.Contributions;


/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */
@RuntimePermissions
public class PayListBrowserFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.recycler_view_template, container, false);
        setupView(root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.title_pay_list);
        startLoading();
    }

    public void setupView(View root) {

        swipeRefreshLayout = root.findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

        recyclerView = root.findViewById(R.id.recycler_view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoading();
            }
        });

        setupAdapter();
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        Contributions.loadAsync(getString(R.string.pay_list_url),
                new Contributions.Callback() {
                    @Override
                    public void onError(final Throwable e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final List<Contribution> extras) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.update(extras);
                                swipeRefreshLayout.setRefreshing(false);
                                getActivity().setTitle(getString(R.string.title_pay_list) + "\t" + extras.size());
                            }
                        });
                    }
                });
    }

    protected void setupAdapter() {
        recyclerView.setHasFixedSize(true);
        setupLayoutManager();
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

    }

    protected void setupLayoutManager() {
        recyclerView.setLayoutManager(getLayoutManager());
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    }


    class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView thumbnail;

        TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.title);
            thumbnail = itemView.findViewById(R.id.avatar);
            thumbnail.setImageResource(R.drawable.ic_header_avatar);
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final List<Contribution> data;

        public Adapter(List<Contribution> data) {
            this.data = data;
        }

        public Adapter() {
            this(new ArrayList<Contribution>());
        }

        public void update(List<Contribution> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Contribution Contribution, int position) {
            this.data.add(position, Contribution);
            notifyItemInserted(position);
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.contribute_card_item, parent, false);
            return new TwoLinesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, int position) {
            final Contribution item = data.get(position);
            holder.title.setText(item.getNick());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

    }
}
