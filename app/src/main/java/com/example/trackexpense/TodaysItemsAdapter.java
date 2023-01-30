package com.example.trackexpense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodaysItemsAdapter extends RecyclerView.Adapter<TodaysItemsAdapter.viewHolder> {

    private Context mContext;
    private List<Data> myDataList;

    public TodaysItemsAdapter(Context mContext, List<Data> myDataList) {
        this.mContext = mContext;
        this.myDataList = myDataList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrieve_layout, parent, false);
        return new TodaysItemsAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        final Data data = myDataList.get(position);
        holder.item.setText("Item: " + data.getItem());
        holder.amount.setText("Amount: $" + data.getAmount());
        holder.date.setText("Today: " + data.getDate());
        holder.item.setText("Note: " + data.getNotes());


        switch (data.getItem()) {
            case "Transport":
                holder.imageView.setImageResource(R.drawable.transport);
                break;
            case "Food":
                holder.imageView.setImageResource(R.drawable.food);
                break;
            case "Entertainment":
                holder.imageView.setImageResource(R.drawable.entertainment);
                break;
            case "Other":
                holder.imageView.setImageResource(R.drawable.other);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        public TextView item, amount, date, notes;
        public ImageView imageView;

        public viewHolder(@NonNull View itemView){
            super(itemView);

            item = itemView.findViewById(R.id.item);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            notes = itemView.findViewById(R.id.note);
            imageView = itemView.findViewById(R.id.imageView);

        }
    }
}
