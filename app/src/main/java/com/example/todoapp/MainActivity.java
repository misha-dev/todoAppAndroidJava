package com.example.todoapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.adapters.TodoAdapter;
import com.example.todoapp.database.TodoRoomDatabase;
import com.example.todoapp.database.models.Todo;
import com.example.todoapp.viewModels.TodoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TodoViewModel viewModel;
    private TodoAdapter adapter;
    private TextView noTodosText;
    private RecyclerView recyclerViewTodos;
    private EditText searchBar;
    private RadioGroup filterRadioGroup;


    private List<Todo> allTodos = new ArrayList<>();
    private List<Todo> filteredTodos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(
                this,
                new TodoViewModel.Factory(TodoRoomDatabase.getInstance(this))
        ).get(TodoViewModel.class);

        recyclerViewTodos = findViewById(R.id.todos);
        noTodosText = findViewById(R.id.no_todos);
        searchBar = findViewById(R.id.search_bar);
        filterRadioGroup = findViewById(R.id.filter_radio_group);
        FloatingActionButton addButton = findViewById(R.id.button_add_todo);

        adapter = new TodoAdapter(new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onTodoChecked(Todo todo) {
                handleCheckTodo(todo);
            }

            @Override
            public void onTodoDeleted(Todo todo) {
                handleDeleteTodo(todo);
            }

            @Override
            public void onTodoEdited(Todo todo) {
                handleEditTodo(todo);
            }
        });

        recyclerViewTodos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTodos.setAdapter(adapter);

        viewModel.getTodos().observe(this, todos -> {
            if (todos != null) {
                allTodos = new ArrayList<>(todos);
                applyFilters();
            }
        });

        viewModel.getErrorMessage().observe(this, this::showError);

        setupSearchBarListener();
        setupFilterRadioGroupListener();

        addButton.setOnClickListener(v -> openAddTodoDialog());
    }

    private void setupSearchBarListener() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterRadioGroupListener() {
        filterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchBar.getText().toString().trim().toLowerCase();
        int checkedRadioButtonId = filterRadioGroup.getCheckedRadioButtonId();

        filteredTodos.clear();

        for (Todo todo : allTodos) {
            boolean matchesSearch = todo.getText().toLowerCase().contains(searchText);
            boolean matchesFilter = false;

            if (checkedRadioButtonId == R.id.radio_all) {
                matchesFilter = true;
            } else if (checkedRadioButtonId == R.id.radio_active) {
                matchesFilter = !todo.isCompleted();
            } else if (checkedRadioButtonId == R.id.radio_completed) {
                matchesFilter = todo.isCompleted();
            }

            if (matchesSearch && matchesFilter) {
                filteredTodos.add(todo);
            }
        }

        updateTodos(filteredTodos);
    }

    private void updateTodos(List<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            noTodosText.setVisibility(View.VISIBLE);
            recyclerViewTodos.setVisibility(View.GONE);
        } else {
            noTodosText.setVisibility(View.GONE);
            recyclerViewTodos.setVisibility(View.VISIBLE);
            adapter.setTasks(todos);
        }
    }

    private void handleCheckTodo(Todo todo) {
        viewModel.updateTodo(todo);
    }

    private void handleDeleteTodo(Todo todo) {
        String text = todo.getText();
        new AlertDialog.Builder(this)
                .setTitle("Delete Todo")
                .setMessage(String.format("Are you sure you want to delete this todo? (%s)", (text.length() > 150) ? text.substring(0, 150) + "..." : text))
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteTodo(todo).observe(this, success -> {
                        if (success != null && success) {
                            Toast.makeText(this, "Todo deleted!", Toast.LENGTH_SHORT).show();
                        } else {
                            showError("Failed to delete todo.");
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleEditTodo(Todo todo) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_todo);

        EditText editText = dialog.findViewById(R.id.et_todo);
        Button saveButton = dialog.findViewById(R.id.btn_save);
        ImageButton closeButton = dialog.findViewById(R.id.btn_close);
        TextView datePicker = dialog.findViewById(R.id.tv_date_picker);

        editText.setText(todo.getText());
        datePicker.setText(todo.getDueDate());

        closeButton.setOnClickListener(v -> dialog.dismiss());

        final String[] selectedDate = {todo.getDueDate()};

        datePicker.setOnClickListener(v -> {
            String[] dateParts =selectedDate[0].split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate[0] = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        datePicker.setText(selectedDate[0]);
                    },
                    year, month, day
            );

            Calendar currentCalendar = Calendar.getInstance();
            Calendar dueDateCalendar = Calendar.getInstance();
            dueDateCalendar.set(year, month, day);

            Calendar minDateCalendar = (dueDateCalendar.after(currentCalendar)) ? currentCalendar  : dueDateCalendar;

            datePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String updatedText = editText.getText().toString().trim();
            if (!updatedText.isEmpty() && selectedDate[0] != null) {
                todo.setText(updatedText);
                todo.setDueDate(selectedDate[0]);

                viewModel.updateTodo(todo).observe(this, success -> {
                    if (success != null && success) {
                        Toast.makeText(this, "Todo updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }


    private void openAddTodoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_todo);

        EditText editText = dialog.findViewById(R.id.et_todo);
        Button saveButton = dialog.findViewById(R.id.btn_save);
        ImageButton buttonClose = dialog.findViewById(R.id.btn_close);
        TextView datePicker = dialog.findViewById(R.id.tv_date_picker);

        buttonClose.setOnClickListener(v -> dialog.dismiss());


        final String[] selectedDate = {null};

        datePicker.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate[0] = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        datePicker.setText(selectedDate[0]);
                    },
                    year, month, day
            );

            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty() && selectedDate[0] != null) {
                Todo newTodo = new Todo(text, false, selectedDate[0]);
                viewModel.insertTodo(newTodo).observe(this, success -> {
                    if (success != null && success) {
                        Toast.makeText(this, "Todo added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error adding todo!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        dialog.show();
    }


    private void showError(String error) {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }
}
