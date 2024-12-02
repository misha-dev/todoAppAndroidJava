package com.example.todoapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todoapp.database.TodoRoomDatabase;
import com.example.todoapp.database.dao.TodoDao;
import com.example.todoapp.database.models.Todo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoRepository {

    private final TodoDao todoDao;
    private final ExecutorService executorService;

    public TodoRepository(TodoRoomDatabase database) {
        this.todoDao = database.todoDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Todo>> getAllTodos() {
        return todoDao.getAllTodos();
    }

    public LiveData<Boolean> insertTodo(Todo todo) {
        return performDatabaseOperation(() -> todoDao.insertTodo(todo));
    }

    public LiveData<Boolean> updateTodo(Todo todo) {
        return performDatabaseOperation(() -> todoDao.insertTodo(todo));
    }

    public LiveData<Boolean> deleteTodo(Todo todo) {
        return performDatabaseOperation(() -> todoDao.deleteTodo(todo));
    }

    public LiveData<Boolean> performDatabaseOperation(DatabaseOperation operation) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                operation.execute();
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        return result;
    }

    @FunctionalInterface
    private interface DatabaseOperation {
        void execute() throws Exception;
    }
}
