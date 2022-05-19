package com.example.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Target;
import java.util.List;

public class AdapterTarget extends FirebaseRecyclerAdapter<ModelTarget, AdapterTarget.myViewHolder> {

    private AdapterTarget.OnItemClickListener listener;
    public AdapterTarget(@NonNull FirebaseRecyclerOptions<ModelTarget> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ModelTarget model) {
        holder.type.setText(model.getTypeA());
        holder.desc.setText(model.getDescA());
        holder.time.setText(model.getStartdate());
        holder.act.setText(model.getState());
        holder.enddate.setText(model.getEnddate());

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_target,parent,false);
        return new myViewHolder(view);
    }

    class  myViewHolder extends RecyclerView.ViewHolder{

        TextView type,desc,time,act,enddate;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            type = (TextView) itemView.findViewById(R.id.type);
            desc = (TextView) itemView.findViewById(R.id.desc);
            time = (TextView) itemView.findViewById(R.id.startdate);
            act = (TextView) itemView.findViewById(R.id.act);
            enddate = (TextView) itemView.findViewById(R.id.enddatee);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DataSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(AdapterTarget.OnItemClickListener listener)
    {
        this.listener = listener;
    }

}
