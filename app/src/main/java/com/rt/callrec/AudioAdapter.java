package com.rt.callrec;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TRANTUAN on 09-Apr-18.
 */

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.RecyclerViewHolder> {

    private Context context;
    private List<Audio> list;
        private AudioListener listener;

    public AudioAdapter(List<Audio> list, Context context,  AudioListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void changeList (List<Audio> list){
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem;
        viewItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item, parent, false);
        return new RecyclerViewHolder(viewItem);
    }


    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        RecyclerViewHolder viewHolder =  holder;
        Audio selectableItem = list.get(position);
        String name = selectableItem.getFileName();
        holder.songView.setText(name+"");
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }




    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView songView;
        TextView artistView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            songView = itemView.findViewById(R.id.song_title);
            artistView = itemView.findViewById(R.id.song_artist);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected chatHistory in callback
                    listener.onClick(list.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onClickLong(list.get(getAdapterPosition()));
                    return true;
                }
            });
        }
    }

    public interface AudioListener {
        void onClick(Audio audio, int position);
        void onClickLong(Audio audio);
    }
}