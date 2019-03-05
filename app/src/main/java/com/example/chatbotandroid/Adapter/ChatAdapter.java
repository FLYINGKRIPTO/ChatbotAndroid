package com.example.chatbotandroid.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatbotandroid.Model.Chat;
import com.example.chatbotandroid.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mContext;
    private List<Chat> mChat;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(Context mContext,List<Chat> mChat){
        this.mChat = mChat;
        this.mContext = mContext;

    }
    public class ViewHolder extends RecyclerView.ViewHolder {

       public TextView show_message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

              show_message = itemView.findViewById(R.id.show_message);

        }
    }


    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i == MSG_TYPE_RIGHT){
            Log.d(TAG, "onCreateViewHolder:  in MSG_TYPE_RIGHT");
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,viewGroup,false);
            return new ChatAdapter.ViewHolder(view);
        }
        else {
            Log.d(TAG, "onCreateViewHolder:  in MSG_TYPE_LEFT ");
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,viewGroup,false);
            return  new ChatAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder viewHolder, int i) {

            Chat chat = mChat.get(i);
            viewHolder.show_message.setText(chat.getMessage());
    }

    @Override
    public int getItemViewType(int position) {

        if (mChat.get(position).getSender().equals("11")){
            Log.d(TAG, "getItemViewType: ");
            return MSG_TYPE_LEFT;
        }
        else {
            return MSG_TYPE_RIGHT;
        }



    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }
}
