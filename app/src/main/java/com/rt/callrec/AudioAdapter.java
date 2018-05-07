package com.rt.callrec;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by TRANTUAN on 09-Apr-18.
 */

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder>  implements Filterable {

    Context mContext;
    List<Audio> audioList;
    private List<Audio> audioListtFiltered;
    private AudioListener listener;


    public AudioAdapter(List<Audio> audioList, Context context,  AudioListener listener) {
        this.mContext = context;
        this.listener = listener;
        this.audioList = audioList;
        this.audioListtFiltered = audioList;
    }

    public void ChangeList(List<Audio> audioList) {
        this.audioList = audioList;
        this.audioListtFiltered = audioList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, null);
        ViewHolder hf = new ViewHolder(view);
        return hf;
    }



    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Audio audio = audioListtFiltered.get(position);
        if (audio!=null) {
            viewHolder.song_title.setText(audio.getFileName());
            viewHolder.song_artist.setText(audio.getUserID());
        }
    }

    @Override
    public int getItemCount() {
        return   audioListtFiltered == null ? 0 : audioListtFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView song_title, song_artist;


        public ViewHolder(View view) {
            super(view);
            song_title = view.findViewById(R.id.song_title);
            song_artist = view.findViewById(R.id.song_artist);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected chatHistory in callback
                    listener.onClickAudio(audioListtFiltered.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onClickLongAudio(audioListtFiltered.get(getAdapterPosition()),getAdapterPosition());
                    return true;
                }
            });

        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    audioListtFiltered = audioList;
                } else {
                    List<Audio> filteredList = new ArrayList<>();
                    for (Audio row : audioList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getFileName().toLowerCase().contains(charString.toLowerCase())
                                ) {
                            filteredList.add(row);
                        }
                    }

                    audioListtFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = audioListtFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                audioListtFiltered = (ArrayList<Audio>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface AudioListener {
        void onClickAudio(Audio audio,int position);
        void onClickLongAudio(Audio audio, int position);
    }
}