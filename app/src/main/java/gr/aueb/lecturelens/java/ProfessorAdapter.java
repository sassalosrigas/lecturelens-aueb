package gr.aueb.lecturelens.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import gr.aueb.lecturelens.R;

public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorAdapter.ProfViewHolder> {

    private final List<Professor> professorList;
    private final OnProfClickListener listener;

    public interface OnProfClickListener {
        void onProfClick(Professor prof);
    }

    public ProfessorAdapter(List<Professor> professorList, OnProfClickListener listener) {
        this.professorList = professorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_professor_chip, parent, false);
        return new ProfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfViewHolder holder, int position) {
        Professor prof = professorList.get(position);
        holder.txtName.setText(prof.getFullName());
        holder.txtTitle.setText(prof.getTitle());
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onProfClick(prof); });
    }

    @Override
    public int getItemCount() { return professorList.size(); }

    public static class ProfViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtTitle;

        public ProfViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.profName);
            txtTitle = itemView.findViewById(R.id.profTitle);
        }
    }
}