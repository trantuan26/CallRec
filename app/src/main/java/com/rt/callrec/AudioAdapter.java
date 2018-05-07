package com.rt.callrec;

import android.content.Context;


import android.media.MediaPlayer;

import android.support.v7.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import java.util.List;


/**
 * Created by TRANTUAN on 09-Apr-18.
 */

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> implements Filterable {

    Context mContext;
    List<Audio> audioList;
    private List<Audio> audioListtFiltered;
    private DatabaseReference mDataRefUser;
    private Player player = null;
    private int lastItem;
    private ImageView lastImv;
    private SeekBar lastSeekBar;
    String name = "";

    public AudioAdapter(List<Audio> audioList, Context context) {
        this.mContext = context;
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
        if (audio != null) {
            viewHolder.tvName.setText(audio.getFileName());
            mDataRefUser = FirebaseDatabase.getInstance().getReference().child("Users");
            mDataRefUser.keepSynced(true);

            if (!TextUtils.isEmpty(audio.getUserID()))
            mDataRefUser.child(audio.getUserID()).child("userName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //Log.d("aaa", "onBindViewHolder: " + snapshot.getValue());
                    audio.setUserName(snapshot.getValue().toString());
                    viewHolder.song_artist.setText(snapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        }
    }


    @Override
    public int getItemCount() {
        return audioListtFiltered == null ? 0 : audioListtFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView song_artist;
        private TextView tvName;
        private ImageView imvPlay;
        private SeekBar sbPlay;


        public ViewHolder(View view) {
            super(view);
            song_artist = view.findViewById(R.id.song_artist);
            imvPlay = itemView.findViewById(R.id.btnPlay);
            tvName = itemView.findViewById(R.id.tvName);
            sbPlay = itemView.findViewById(R.id.sbPlay);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Use abstract function
//                    onItemClick();
                    return true;
                }
            });

            if (imvPlay != null) {
                imvPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Use abstract function
                        if ((player == null) || (player != null && getAdapterPosition() != lastItem)) {
                            if (player != null) {
                                player.stop();
                                player.release();
                                player = null;
                                lastImv.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                lastSeekBar.setVisibility(View.INVISIBLE);
                            }

                            player = new Player(audioListtFiltered.get(getAdapterPosition()).getmUri()) {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    imvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                    sbPlay.setProgress(0);
                                }

                                @Override
                                public void onPrepare() {
                                    sbPlay.setVisibility(View.VISIBLE);
                                    sbPlay.setProgress(0);
                                    sbPlay.setMax(this.getDuration());
                                }

                                @Override
                                public void onPlaying() {
                                    if (this.getCurrentPosition() < this.getDuration() && this.isPlaying()) {
                                        sbPlay.setProgress(this.getCurrentPosition());
                                    }
                                }

                                @Override
                                public void togglePlayPause() {
                                    if (player.isPlaying()) {
                                        player.pause();
                                        imvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                        Log.d("L_player psause", (player == null) + " ");
                                    } else {
                                        player.start();
                                        imvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                        Log.d("L_player playing", (player == null) + " ");
                                    }
                                }
                            };
                            imvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                            lastItem = getAdapterPosition();
                            lastImv = imvPlay;
                            lastSeekBar = sbPlay;
                            player.start();
                            Log.d("L_new player", (player == null) + " ");
                        } else {
                            if (getAdapterPosition() == lastItem) {
                                player.togglePlayPause();
                            }
                        }
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Use abstract function
//                    onItemClick(getAdapterPosition());
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
                        if (
                                row.getFileName().toLowerCase().contains(charString.toLowerCase())
                                ||  row.getUserName().toLowerCase().contains(charString.toLowerCase())
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


}