package com.rt.callrec;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by QNIT on 12/24/2016.
 */

public class RecRecyclerAdapter extends RecyclerView.Adapter<RecRecyclerAdapter.RecyclerViewHolder>
        implements Filterable {

    public interface onItemClick {
        public void onClick(int position);

        public void onLongClick(int position);

        public void onImvContactClick(int position);
    }

    private onItemClick myOnItemClick;
    private Context context;
    private List<RecItem> list, listRoot = null;
    private SparseBooleanArray mSelectedItemIds;
    private String path;
    private RecFilter filter;

    public RecRecyclerAdapter(Context context, List<RecItem> list) {
        this.context = context;
        this.list = list;
        mSelectedItemIds = new SparseBooleanArray();
    }

    public RecRecyclerAdapter(Context context, String path) {
        this.context = context;
        this.path = path;
        list = Explorer.getItemList(context, path);
        Log.d("L_ListRec", new Gson().toJson(list));
        Toast.makeText(context, "ListSize:" + list.size(), Toast.LENGTH_SHORT).show();
        mSelectedItemIds = new SparseBooleanArray();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem = null;
        switch (viewType) {
            case 1:
                viewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_item_recycler_l, parent, false);
                break;
            case 2:
                viewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_group_item_l, parent, false);
                break;
        }
        return new RecyclerViewHolder(viewItem);
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).recFile.isFile())
            return 1;
        else
            return 2;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        RecItem item = getItemAt(position);
        if (!item.recFile.isFile()) {
            holder.tvDateGroup.setText(item.recFile.getDateCreate());
        } else {
            if (mSelectedItemIds.get(position)) {
//                holder.imvContact.setImageResource(R.drawable.ic_check_white_48dp);
                holder.loRecItemContent.setBackgroundColor(context.getResources().getColor(R.color.item_selected_color));
            } else {
//                holder.imvContact.setImageBitmap(item.getContactPhoto());
                holder.loRecItemContent.setBackgroundColor(0);
            }
            if (item.recFile.getCallAction().equals("IN")){
//                holder.imvCallAction.setImageResource(R.drawable.call_in_arrow);
            } else
//                holder.imvCallAction.setImageResource(R.drawable.call_out_arrow);
            holder.tvName.setText(!item.recFile.getContactName().equals("") ?
                    item.recFile.getContactName() : item.recFile.getPhoneNumber());
            holder.tvTimeCreate.setText(item.getTimeCreate());
            holder.tvLength.setText(item.recFile.getDuration());
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            listRoot = list;
            filter = new RecFilter();
        }
        return filter;
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void update(List<RecItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void refresh() {
        list = Explorer.getItemList(context, path);
        notifyDataSetChanged();
    }

    public void addItem(RecItem item) {
        list.add(item);
        notifyItemInserted(list.size());
    }

    public RecItem getItemAt(int position) {
        return list.get(position);
    }

    public void toggleSelection(int position) {
        if (list.get(position).recFile.isFile()) {
            if (!mSelectedItemIds.get(position))
                mSelectedItemIds.put(position, true);
            else
                mSelectedItemIds.delete(position);
            notifyDataSetChanged();
        }
    }

    public void setSelectedAll() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).recFile.isFile())
                mSelectedItemIds.put(i, true);
        }
        notifyDataSetChanged();
    }

    public void deleteSelected() {
        for (int i = mSelectedItemIds.size() - 1; i >= 0; i--) {
            if (mSelectedItemIds.valueAt(i)) {
                list.get(mSelectedItemIds.keyAt(i)).recFile.delete();
            }
        }
        Explorer.deleteBackupFile(path);
        list = Explorer.getItemList(context, path);
        removeSelection();
    }

    public ArrayList<Uri> getSelectedRecFile() {
        ArrayList<Uri> listRec = new ArrayList<>();
        for (int i = mSelectedItemIds.size() - 1; i >= 0; i--) {
            if (mSelectedItemIds.valueAt(i)) {
                listRec.add(Uri.fromFile(list.get(mSelectedItemIds.keyAt(i)).recFile));
            }
        }
        return listRec;
    }


    public void removeSelection() {
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemIds.size();
    }

    public SparseBooleanArray getSelectedIDs() {
        return mSelectedItemIds;
    }

//    public abstract void onItemLongClick(int position);
//
//    public abstract void onItemClick(int position);
//
//    public abstract void onImvContactClick(int position);

    public void setOnItemClick(onItemClick onitemclick) {
        myOnItemClick = onitemclick;
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateGroup, tvName, tvTimeCreate, tvLength;
        private ImageView imvContact;
        private ImageView imvCallAction;
        private RelativeLayout loRecItemContent;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            imvCallAction = (ImageView) itemView.findViewById(R.id.imvCallAction);
            imvContact = (ImageView) itemView.findViewById(R.id.imvItemContact);
            tvDateGroup = (TextView) itemView.findViewById(R.id.tvDateGroup);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTimeCreate = (TextView) itemView.findViewById(R.id.tvTimeCreate);
            tvLength = (TextView) itemView.findViewById(R.id.tvLength);
            loRecItemContent = (RelativeLayout) itemView.findViewById(R.id.loRecItemContent_recyclerView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    myOnItemClick.onLongClick(getAdapterPosition());
                    // Use abstract function
//                    onItemClick();
                    return true;
                }
            });

            if (imvContact != null) {
                imvContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myOnItemClick.onImvContactClick(getAdapterPosition());
                        // Use abstract function
//                        onImvContactClick(getAdapterPosition());
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myOnItemClick.onClick(getAdapterPosition());
                    // Use abstract function
//                    onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public class RecFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence key) {
            FilterResults results = new FilterResults();

            if (!TextUtils.isEmpty(key)) {
                List<RecItem> tempList = new ArrayList<>();
                for (RecItem item : listRoot) {
                    if (item.recFile.isFile() &&
                            (item.recFile.getContactName().toLowerCase().contains(key.toString().toLowerCase())
                                    || item.recFile.getPhoneNumber().contains(key.toString()))) {
                        tempList.add(item);
                    }
                }
                results.count = tempList.size();
                results.values = tempList;
            } else {
                results.count = listRoot.size();
                results.values = listRoot;
                listRoot = null;
                filter = null;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list = (List<RecItem>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
