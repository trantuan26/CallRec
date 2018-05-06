package com.rt.callrec;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by QNIT on 12/24/2016.
 */

public class RecRecyclerAdapter extends RecyclerView.Adapter<RecRecyclerAdapter.RecyclerViewHolder> {
    private Context context;
    private Player player = null;
    private List<String> list;
    private boolean isPlaying = false;

    public RecRecyclerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    public RecRecyclerAdapter(Context context) {
        this.context = context;
        this.list = RecFile.getListFileName(context);
        Log.d("L_list", list.size() + "");
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem;
        viewItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rec_item_l, parent, false);
        return new RecyclerViewHolder(viewItem);
    }

    public void stopPlayer() {
        this.isPlaying = false;
        player.stop();
        player.release();
        player = null;
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
        String name = getItemAt(position);
        holder.tvName.setText(name);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public String getItemAt(int position) {
        return list.get(position);
    }

    public void refresh() {
        if (list != null) {
            list = RecFile.getListFileName(context);
            notifyDataSetChanged();
        }
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView imvPlay;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            imvPlay = itemView.findViewById(R.id.btnPlay);
            tvName = itemView.findViewById(R.id.tvName);

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
                        if (player == null) {
                            player = new Player(context.getFilesDir().getAbsoluteFile() + "/" + list.get(getAdapterPosition())) {
                                @Override
                                public void onPrepare() {

                                }

                                @Override
                                public void onPlaying() {

                                }

                                @Override
                                public void togglePlayPause() {
                                    if (this.isPlaying()) {
                                        pause();
//                                        btnPlayPause.setImageResource(R.drawable.play_48);
                                    } else {
                                        start();
//                                        btnPlayPause.setImageResource(R.drawable.pause_48);
                                    }
                                }

                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {

                                }
                            };
                        }

                        if (isPlaying) {
                            stopPlayer();
                        } else {
                            Log.d("L_FileDir", context.getFilesDir().getAbsoluteFile() + "/" + list.get(getAdapterPosition()));

                            player.start();
                            isPlaying = true;
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
}
