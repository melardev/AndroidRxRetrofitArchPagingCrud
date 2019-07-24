package com.melardev.android.crud.datasource.remote;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.remote.api.RxTodoApi;

import io.reactivex.disposables.CompositeDisposable;

public class TodoDataSourceFactory extends DataSource.Factory<Integer, Todo> {

    private final RxTodoApi todoApi;
    private final CompositeDisposable compositeDisposable;
    MutableLiveData<TodoApiDataSource> newsDataSourceLiveData = new MutableLiveData<>();

    public TodoDataSourceFactory(CompositeDisposable compositeDisposable, RxTodoApi todoApi) {
        this.compositeDisposable = compositeDisposable;
        this.todoApi = todoApi;
    }

    @NonNull
    @Override
    public DataSource<Integer, Todo> create() {
        TodoApiDataSource todoDataSource = new TodoApiDataSource(todoApi, compositeDisposable);
        newsDataSourceLiveData.postValue(todoDataSource);
        return todoDataSource;
    }
}
