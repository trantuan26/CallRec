package com.rt.callrec;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.rt.callrec.Constants.PATH;


/**
 * Created by QNIT on 12/13/2016.
 */

public class List_Rec_Frm extends Fragment implements SearchView.OnQueryTextListener{
    RecyclerView recyclerView;
    RecRecyclerAdapter adapter;

    SharedPreferences preferences;
    ActionMode mActionMode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rec_list_l, container, false);

        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new RecRecyclerAdapter(getContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + PATH);
        adapter.setOnItemClick(new RecRecyclerAdapter.onItemClick() {
            @Override
            public void onClick(int position) {
                if (adapter.getItemAt(position).getRecFile().isFile() && adapter.getSelectedCount() == 0){

                } else onListItemSelect(position);
            }

            @Override
            public void onLongClick(int position) {
                onListItemSelect(position);
            }

            @Override
            public void onImvContactClick(int position) {
                onListItemSelect(position);
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.refresh();
    }

    private void toggleStartStopActionMode() {
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
        } else if (!hasCheckedItems && mActionMode != null) {
            mActionMode.finish();
        }
    }

    private void onListItemSelect(int position) {
        adapter.toggleSelection(position);
        toggleStartStopActionMode();
        if (mActionMode != null) mActionMode.setTitle(adapter.getSelectedCount() + "");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }
}
