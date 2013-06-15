package net.staric.kronometer.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.staric.kronometer.ContestantBackend;
import net.staric.kronometer.R;
import net.staric.kronometer.models.Category;

public class ContestantActivity extends Activity {
    ContestantBackend contestantsBackend;
    Spinner categoriesSpinner;
    ArrayAdapter<Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contestant);

        contestantsBackend = ContestantBackend.getInstance();

        categoriesSpinner = (Spinner)findViewById(R.id.category);
        categoryAdapter = new ArrayAdapter<Category>(this,
                android.R.layout.simple_list_item_1,
                contestantsBackend.getCategories()
        );
        categoriesSpinner.setAdapter(categoryAdapter);


    }
}
