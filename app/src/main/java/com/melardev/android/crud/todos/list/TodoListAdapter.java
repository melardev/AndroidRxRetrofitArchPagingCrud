package com.melardev.android.crud.todos.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.melardev.android.crud.R;
import com.melardev.android.crud.datasource.common.entities.Todo;

public class TodoListAdapter extends PagedListAdapter<Todo, TodoListAdapter.TodoViewHolder> {

    private TodoRowEventListener todoRowEventListener;

    protected TodoListAdapter(TodoRowEventListener listener, @NonNull DiffUtil.ItemCallback<Todo> diffCallback) {
        super(diffCallback);
        this.todoRowEventListener = listener;
    }

    interface TodoRowEventListener {
        void onClicked(Todo todo);
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater view = LayoutInflater.from(parent.getContext());
        View itemBinding = view.inflate(R.layout.todo_row, parent, false);
        return new TodoViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder todoViewHolder, int position) {
        Todo todo = getItem(position);
        if (todo == null)
            return;

        todoViewHolder.txtId.setText(String.valueOf(todo.getId()));
        todoViewHolder.txtTitle.setText(todo.getTitle());
        todoViewHolder.txtDescription.setText(todo.getDescription());
        todoViewHolder.checkboxCompleted.setChecked(todo.isCompleted());
        todoViewHolder.txtCreatedAt.setText(todo.getCreatedAt());
        todoViewHolder.txtUpdatedAt.setText(todo.getUpdatedAt());


        todoViewHolder.itemView.setOnClickListener(v -> todoRowEventListener.onClicked(todo));
    }


    public class TodoViewHolder extends RecyclerView.ViewHolder {

        private TextView txtCreatedAt;
        private TextView txtUpdatedAt;
        private CheckBox checkboxCompleted;
        TextView txtId, txtTitle, txtDescription;

        public TodoViewHolder(View view) {
            super(view);

            txtId = view.findViewById(R.id.txtId);
            txtTitle = view.findViewById(R.id.txtTitle);
            txtDescription = view.findViewById(R.id.txtDescription);
            checkboxCompleted = view.findViewById(R.id.checkboxCompleted);
            txtCreatedAt = view.findViewById(R.id.txtCreatedAt);
            txtUpdatedAt = view.findViewById(R.id.txtUpdatedAt);

        }

    }
}
