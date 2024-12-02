package com.example.todoapp.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.database.TodoRoomDatabase;
import com.example.todoapp.database.models.Todo;
import com.example.todoapp.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends ViewModel {

    private final TodoRepository repository;
    private final LiveData<List<Todo>> todos;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TodoViewModel(TodoRoomDatabase database) {
        this.repository = new TodoRepository(database);
        this.todos = repository.getAllTodos();
    }

    public LiveData<List<Todo>> getTodos() {
        return todos;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> insertTodo(Todo todo) {
        return wrapWithErrorHandling(repository.insertTodo(todo), "Error adding todo");
    }

    public LiveData<Boolean> updateTodo(Todo todo) {
        return wrapWithErrorHandling(repository.updateTodo(todo), "Error updating todo");
    }

    public LiveData<Boolean> deleteTodo(Todo todo) {
        return wrapWithErrorHandling(repository.deleteTodo(todo), "Error deleting todo");
    }

    private LiveData<Boolean> wrapWithErrorHandling(LiveData<Boolean> operationResult, String errorMessageOnFailure) {
        MediatorLiveData<Boolean> result = new MediatorLiveData<>();

        result.addSource(operationResult, success -> {
            if (success != null && success) {
                result.postValue(true);
            } else {
                errorMessage.postValue(errorMessageOnFailure);
                result.postValue(false);
            }
            result.removeSource(operationResult);
        });

        return result;
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TodoRoomDatabase database;

        public Factory(TodoRoomDatabase database) {
            this.database = database;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TodoViewModel.class)) {
                return (T) new TodoViewModel(database);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

