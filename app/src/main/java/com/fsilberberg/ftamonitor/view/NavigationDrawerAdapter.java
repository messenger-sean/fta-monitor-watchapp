package com.fsilberberg.ftamonitor.view;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;

import java.util.List;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private List<NavigationItem> m_data;
    private NavigationDrawerCallbacks m_navigationDrawerCallbacks;
    private View m_selectedView;
    private int m_selectedPosition;

    public NavigationDrawerAdapter(List<NavigationItem> data) {
        m_data = data;
    }

    public void setNavigationDrawerCallbacks(NavigationDrawerCallbacks navigationDrawerCallbacks) {
        m_navigationDrawerCallbacks = navigationDrawerCallbacks;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_row, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (m_selectedView != null) {
                            m_selectedView.setSelected(false);
                        }
                        m_selectedPosition = viewHolder.getAdapterPosition();
                        v.setSelected(true);
                        m_selectedView = v;
                        if (m_navigationDrawerCallbacks != null)
                            m_navigationDrawerCallbacks.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition());
                    }
                }
        );
        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.textView.setText(m_data.get(i).getText());
        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(m_data.get(i).getDrawable(), null, null, null);
        if (m_selectedPosition == i) {
            if (m_selectedView != null) {
                m_selectedView.setSelected(false);
            }
            m_selectedPosition = i;
            m_selectedView = viewHolder.itemView;
            m_selectedView.setSelected(true);
        }
    }


    public void selectPosition(int position) {
        m_selectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return m_data != null ? m_data.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
        }
    }
}