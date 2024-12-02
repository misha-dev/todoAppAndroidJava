package com.example.todoapp.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.database.models.Todo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodosViewHolder> {

    private final List<Todo> todosList = new ArrayList<>();
    private final OnTodoActionListener actionListener;

    public interface OnTodoActionListener {
        void onTodoChecked(Todo todo);
        void onTodoDeleted(Todo todo);
        void onTodoEdited(Todo todo);
    }

    public TodoAdapter(OnTodoActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setTasks(List<Todo> newTodos) {
        todosList.clear();
        todosList.addAll(newTodos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new TodosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodosViewHolder holder, int position) {
        Todo todo = todosList.get(position);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setText(todo.getText());
        holder.checkBox.setChecked(todo.isCompleted());

        holder.dueDate.setText(todo.getDueDate());
        holder.dueDateLabel.setText("Due Date:");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dueDate = null;
        try {
            dueDate = dateFormat.parse(todo.getDueDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date currentDate = new Date();

        if (dueDate != null) {
            if (todo.isCompleted()) {
                holder.dueDate.setTextColor(Color.parseColor("#888888"));
                holder.dueDateLabel.setTextColor(Color.parseColor("#888888"));
            } else if (isSameDay(dueDate, currentDate)) {
                holder.dueDate.setTextColor(Color.parseColor("#B98F00"));
                holder.dueDateLabel.setTextColor(Color.parseColor("#B98F00"));
            } else if (dueDate.before(currentDate)) {
                holder.dueDate.setTextColor(Color.RED);
                holder.dueDateLabel.setTextColor(Color.RED);
            } else {
                holder.dueDate.setTextColor(Color.parseColor("#888888"));
                holder.dueDateLabel.setTextColor(Color.parseColor("#888888"));
            }
        }

        if (todo.isCompleted()) {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            todo.setCompleted(isChecked);
            actionListener.onTodoChecked(todo);
        });

        holder.editButton.setOnClickListener(v -> actionListener.onTodoEdited(todo));
        holder.deleteButton.setOnClickListener(v -> actionListener.onTodoDeleted(todo));
    }

    @Override
    public int getItemCount() {
        return todosList.size();
    }

    public static class TodosViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageButton editButton;
        ImageButton deleteButton;
        TextView dueDate;
        TextView dueDateLabel;

        public TodosViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            editButton = itemView.findViewById(R.id.edit_icon);
            deleteButton = itemView.findViewById(R.id.delete_icon);
            dueDate = itemView.findViewById(R.id.due_date);
            dueDateLabel = itemView.findViewById(R.id.due_date_label);
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        return DateFormat.format("yyyyMMdd", date1).equals(DateFormat.format("yyyyMMdd", date2));
    }
}