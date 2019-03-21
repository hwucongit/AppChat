package com.devt3h.appchat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.MyViewHolder> {
    private Context context;
    private List<Post> modelFeeds;
    private IPost iPost;

    public NewsfeedAdapter(NewsfeedAdapter.IPost iPost) {
        this.iPost = iPost;
    }
    @NonNull
    @Override
    public NewsfeedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_newsfeed, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsfeedAdapter.MyViewHolder myViewHolder, int position) {
        Post post = iPost.getPost(position);
        String url = post.getAvatarURL();
        if(url!= null && !url.equals(Constants.KEY_DEFAULT)){
            Picasso.get().load(url)
                    .resize(50, 50)
                    .centerCrop()
                    .into(myViewHolder.imgAvatar);
        }else {
            Picasso.get()
                    .load(R.drawable.default_profile)
                    .resize(50, 50)
                    .centerCrop()
                    .into(myViewHolder.imgAvatar);
        }

        if(!post.getCaption().equals(Constants.KEY_DEFAULT)) myViewHolder.tvCaption.setText(post.getCaption());
        if(!post.getPostImage().equals(Constants.KEY_DEFAULT)){
            myViewHolder.imgPicture.setVisibility(View.VISIBLE);
            Picasso.get().load(post.getPostImage())
                    .into(myViewHolder.imgPicture);
        }
        myViewHolder.tvName.setText(post.getName());
        myViewHolder.tvTime.setText(post.getTime() + " " + post.getDate());
    }

    @Override
    public int getItemCount() {
        return iPost.getCount();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvTime, tvCaption;
        private ImageView imgAvatar, imgPicture;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvCaption = itemView.findViewById(R.id.tv_status);
            imgAvatar = itemView.findViewById(R.id.imgView_proPic);
            imgPicture =itemView.findViewById(R.id.imgView_postPic);
        }
    }

    public interface IPost{
        int getCount();
        Post getPost(int position);
    }
}
