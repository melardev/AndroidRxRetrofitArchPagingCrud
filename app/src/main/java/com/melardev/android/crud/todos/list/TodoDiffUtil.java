package com.melardev.android.crud.todos.list;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.melardev.android.crud.datasource.common.entities.Todo;

class TodoDiffUtil extends DiffUtil.ItemCallback<Todo> {

    @Override
    public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
        return oldItem.equals(newItem);
    }
}
