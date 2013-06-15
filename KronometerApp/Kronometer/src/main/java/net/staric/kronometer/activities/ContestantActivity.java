package net.staric.kronometer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import net.staric.kronometer.ContestantBackend;
import net.staric.kronometer.R;
import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;

public class ContestantActivity extends Activity {
    ContestantBackend contestantsBackend;

    EditText numberEditText;
    EditText nameEditText;
    EditText surnameEditText;
    Spinner categorySpinner;
    CheckBox domesticCheckBox;

    ArrayAdapter<Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contestant);

        contestantsBackend = ContestantBackend.getInstance();

        numberEditText = (EditText)findViewById(R.id.number);
        nameEditText = (EditText)findViewById(R.id.name);
        surnameEditText = (EditText)findViewById(R.id.surname);
        categorySpinner = (Spinner)findViewById(R.id.category);
        domesticCheckBox = (CheckBox)findViewById(R.id.domestic);

        categoryAdapter = new ArrayAdapter<Category>(this,
                android.R.layout.simple_list_item_1,
                contestantsBackend.getCategories()
        );
        categorySpinner.setAdapter(categoryAdapter);
    }

    public void createNewContestant(View view) {
        if (createContestant()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivities(new Intent[]{intent});
        }
    }

    public void createNewContestantAndAddAnother() {
        if (createContestant()) {
            Intent intent = new Intent(this, ContestantActivity.class);
            startActivities(new Intent[]{intent});
        }
    }

    private boolean createContestant() {
        int number;
        try {
            number = Integer.parseInt(numberEditText.getText().toString());
            Contestant.create(
                    number,
                    nameEditText.getText().toString(),
                    surnameEditText.getText().toString(),
                    (Category)categorySpinner.getSelectedItem(),
                    domesticCheckBox.isChecked()
            );
            return true;
        } catch (NumberFormatException e) {
            numberEditText.setError("Invalid number");
            return false;
        } catch (Exception e) {
            numberEditText.setError("Contestant with this number already exists.");
            return false;
        }
    }
}
