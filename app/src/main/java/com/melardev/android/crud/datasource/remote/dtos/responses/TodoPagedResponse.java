package com.melardev.android.crud.datasource.remote.dtos.responses;

import com.google.gson.annotations.SerializedName;
import com.melardev.android.crud.datasource.common.entities.Todo;

import java.util.List;

public class TodoPagedResponse extends SuccessResponse {
    @SerializedName("page_meta")
    private PageMeta pageMeta;
    @SerializedName("todos")
    private List<Todo> todos;

    public PageMeta getPageMeta() {
        return pageMeta;
    }

    public void setPageMeta(PageMeta pageMeta) {
        this.pageMeta = pageMeta;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }
}
