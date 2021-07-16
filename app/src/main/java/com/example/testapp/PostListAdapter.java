package com.example.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PostListAdapter extends BaseAdapter {

    private final Context context;
    private final int layout;
    private final ArrayList<Post> postslist;

    public PostListAdapter(Context context, int layout, ArrayList<Post> postslist) {
        this.context = context;
        this.layout = layout;
        this.postslist = postslist;
    }

    @Override
    public int getCount() {
        return postslist.size();
    }

    @Override
    public Object getItem(int position) {
        return postslist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder{
        ImageView imgPost;
        TextView txtDesc;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout,null);
            holder.txtDesc = row.findViewById(R.id.txtDesc);
            holder.imgPost = row.findViewById(R.id.imgPost);
            row.setTag(holder);
        }
        else{
            holder = (ViewHolder) row.getTag();
        }

        Post post = postslist.get(position);
        holder.txtDesc.setText(post.getDesc());
        byte[] photo = post.getPhoto();
        Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
        holder.imgPost.setImageBitmap(bitmap);

        return row;
    }

}
