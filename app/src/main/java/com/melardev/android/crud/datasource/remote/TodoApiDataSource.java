package com.melardev.android.crud.datasource.remote;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.google.gson.Gson;
import com.melardev.android.crud.datasource.common.entities.Todo;
import com.melardev.android.crud.datasource.common.models.DataSourceOperation;
import com.melardev.android.crud.datasource.common.repositories.TodoRepository;
import com.melardev.android.crud.datasource.remote.api.RxTodoApi;
import com.melardev.android.crud.datasource.remote.dtos.responses.ErrorDataResponse;
import com.melardev.android.crud.datasource.remote.dtos.responses.SuccessResponse;
import com.melardev.android.crud.datasource.remote.dtos.responses.TodoPagedResponse;
import com.melardev.android.crud.datasource.remote.dtos.responses.TodoSuccess;

import java.io.IOException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class TodoApiDataSource extends PageKeyedDataSource<Integer, Todo> implements TodoRepository {

    private final RxTodoApi todoApi;
    private final Gson gson;
    MutableLiveData<Todo> todos = new MutableLiveData<>();
    private Completable retryCompletable = null;
    CompositeDisposable compositeDisposable;

    public TodoApiDataSource(RxTodoApi todoApi, CompositeDisposable compositeDisposable) {
        this.todoApi = todoApi;
        this.gson = new Gson();
        this.compositeDisposable = compositeDisposable;
    }


    @SuppressLint("CheckResult")
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Todo> callback) {
        this.todoApi.fetchTodos(1, params.requestedLoadSize).subscribe(todos -> callback.onResult(todos.getTodos(),
                todos.getPageMeta().isHasPrevPage() ? todos.getPageMeta().getPrevPageNumber() : null,
                todos.getPageMeta().isHasNextPage() ? todos.getPageMeta().getNextPageNumber() : null));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Todo> callback) {
        System.out.println("loadBefore");
    }

    @SuppressLint("CheckResult")
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Todo> callback) {
        this.todoApi.fetchTodos(params.key, params.requestedLoadSize).subscribe(todos -> callback.onResult(todos.getTodos(),
                todos.getPageMeta().isHasNextPage() ? todos.getPageMeta().getNextPageNumber() : null));
    }

    void retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(retryCompletable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }
    }

    private void setRetry(Action action) {
        if (action == null)
            retryCompletable = null;
        else
            retryCompletable = Completable.fromAction(action);
    }

    @Override
    public Observable<DataSourceOperation<List<Todo>>> getAll() {
        return todoApi.fetchTodos()
                .flatMap(apiResponse -> Observable.just(DataSourceOperation.success(apiResponse.getTodos())))
                .onErrorReturn(throwable -> {
                    if (throwable.getClass() == HttpException.class) {
                        Response<?> response = ((HttpException) throwable).response();
                        if (response != null) {
                            ErrorDataResponse errorDataResponse = buildErrorDataResponse(response);
                            if (errorDataResponse != null && errorDataResponse.getFullMessages() != null)
                                return DataSourceOperation.error(errorDataResponse.getFullMessages(), null);
                        }
                    }
                    return DataSourceOperation.error(new String[]{throwable.getMessage()}, null);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<TodoPagedResponse>> getAll(int page, int pageSize) {
        // Observable.<DataSourceOperation<? extends BaseDataResponse>>just(
        return todoApi.fetchTodos(page, pageSize)
                .flatMap(apiResponse -> Observable.just(DataSourceOperation.success(apiResponse)))
                .onErrorReturn(throwable -> buildDataSourceError(TodoPagedResponse.class, throwable))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<Todo>> getById(long todoId) {
        return todoApi.fetchTodo(todoId)
                .flatMap(response -> {
                    if (response.code() == 200) {
                        TodoSuccess todoSuccess = gson.fromJson(response.body().string(), TodoSuccess.class);
                        Todo fetchedTodo = new Todo();
                        fetchedTodo.setId(todoSuccess.getId());
                        fetchedTodo.setTitle(todoSuccess.getTitle());
                        fetchedTodo.setDescription(todoSuccess.getDescription());
                        fetchedTodo.setCreatedAt(todoSuccess.getCreatedAt());
                        fetchedTodo.setCompleted(todoSuccess.isCompleted());

                        return Observable.just(DataSourceOperation.success(fetchedTodo));
                    } else {
                        return buildObservableError(Todo.class, response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<Todo>> create(Todo todo) {
        return todoApi.createTodo(todo)
                .flatMap(response -> Observable.just(DataSourceOperation.success(response)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<Todo>> update(Todo todo) {
        return todoApi.update(todo.getId(), todo)
                .flatMap(response -> Observable.just(DataSourceOperation.success(response)))
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(Todo.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<DataSourceOperation<SuccessResponse>> delete(Long todoId) {
        return todoApi.deleteTodo(todoId)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Observable.just(DataSourceOperation.success(response.getFullMessages(), response));
                    } else {
                        return Observable
                                .<DataSourceOperation<SuccessResponse>>just(DataSourceOperation.error(response.getFullMessages(), null));
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnNext(apiResponse -> System.out.println("Received " + apiResponse.data))
                .onErrorReturn(throwable -> buildDataSourceError(SuccessResponse.class, throwable))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private <T> DataSourceOperation<T> buildDataSourceError(Class<T> clazz, Throwable throwable) throws IOException {

        if (throwable.getClass() == HttpException.class) {
            Response<?> response = ((HttpException) throwable).response();
            if (response != null) {
                ErrorDataResponse errorDataResponse = buildErrorDataResponse(response);
                if (errorDataResponse != null && errorDataResponse.getFullMessages() != null)
                    return DataSourceOperation.error(errorDataResponse.getFullMessages(), null);
            }
        }
        return DataSourceOperation.error(new String[]{throwable.getMessage()}, null);
    }

    private <T> ObservableSource<? extends DataSourceOperation<T>> buildObservableError(Class<T> clazz, Response<?> response) throws IOException {
        ErrorDataResponse err = buildErrorDataResponse(response);
        if (err != null)
            return Observable.just(DataSourceOperation.error(err.getFullMessages(), null));
        else
            return Observable.just(DataSourceOperation.error(new String[]{"Unknown Error"}, null));
    }

    private ErrorDataResponse buildErrorDataResponse(Response<?> responseBody) throws IOException {
        ResponseBody body = responseBody.errorBody();
        if (body == null)
            body = (ResponseBody) responseBody.body();

        if (body == null) return null;

        return gson.fromJson(body.string(), ErrorDataResponse.class);
    }
}
