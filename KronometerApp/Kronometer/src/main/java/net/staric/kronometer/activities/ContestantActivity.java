package net.staric.kronometer.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import net.staric.kronometer.StartActivity;
import net.staric.kronometer.backend.ContestantBackend;
import net.staric.kronometer.R;
import net.staric.kronometer.backend.Update;
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
        Update update = createContestantUpdate();
        if (update != null) {
            new PushUpdatedTask(StartActivity.class).execute(update);
        }
    }

    public void createNewContestantAndAddAnother(View view) {
        Update update = createContestantUpdate();
        if (update != null) {
            new PushUpdatedTask(ContestantActivity.class).execute(update);
        }
    }

    private Update createContestantUpdate() {
        int number;
        try {
            number = Integer.parseInt(numberEditText.getText().toString());
            Contestant contestant = new Contestant(
                    number,
                    nameEditText.getText().toString(),
                    surnameEditText.getText().toString(),
                    (Category)categorySpinner.getSelectedItem(),
                    domesticCheckBox.isChecked()
            );
            return contestantsBackend.createContestantUpdate(contestant);
        } catch (NumberFormatException e) {
            numberEditText.setError("Invalid number");
        } catch (Exception e) {
            numberEditText.setError("Contestant with this number already exists.");
        }
        return null;
    }

    private class PushUpdatedTask extends AsyncTask<Update, Void, Boolean> {
        Class nextActivity;
        private ProgressDialog progressDialog;

        public PushUpdatedTask(Class nextActivity) {
            super();
            this.nextActivity = nextActivity;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog= ProgressDialog.show(ContestantActivity.this,
                    "Uploading changes", "Sending contestant to server.", true);
        }

        @Override
        protected Boolean doInBackground(Update... updates) {
            for (Update u: updates) {
                if (!u.push())
                    return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Intent intent = new Intent(ContestantActivity.this, nextActivity);
                startActivities(new Intent[]{intent});
            } else {
                numberEditText.setError("Contestant with this number already exists.");
            }
        }
    }
}
