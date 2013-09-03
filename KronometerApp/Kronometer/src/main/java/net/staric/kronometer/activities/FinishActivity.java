package net.staric.kronometer.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.ContestantAdapter;
import net.staric.kronometer.R;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.utils.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Arrays;

public class FinishActivity extends Activity {

    private ContestantAdapter contestantsAdapter;
    private ContestantAdapter contestantsOnFinishAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_finish);

        ListView contestants = (ListView)findViewById(R.id.contestants);
        contestantsAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(Arrays.asList(new Contestant[]{
                        new Contestant(1, "Janez", "Novak"),
                        new Contestant(2, "France", "Prešeren"),
                        new Contestant(3, "France2", "Prešeren"),
                        new Contestant(4, "France3", "Prešeren"),
                        new Contestant(5, "France4", "Prešeren"),
                        new Contestant(6, "France5", "Prešeren"),
                        new Contestant(7, "France6", "Prešeren"),
                        new Contestant(8, "France7", "Prešeren"),
                })));
        contestants.setAdapter(contestantsAdapter);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        contestants,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Contestant contestant = contestantsAdapter.getItem(position);
                                    contestantsAdapter.remove(contestant);
                                    contestantsOnFinishAdapter.add(contestant);

                                }
                                contestantsAdapter.notifyDataSetChanged();
                                contestantsOnFinishAdapter.notifyDataSetChanged();
                            }
                        });
        contestants.setOnTouchListener(touchListener);
        contestants.setOnScrollListener(touchListener.makeScrollListener());

        Spinner contestantsOnFinish = (Spinner)findViewById(R.id.contestantsOnFinish);
        contestantsOnFinishAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(contestantsAdapter.getCount()));
        contestantsOnFinish.setAdapter(contestantsOnFinishAdapter);
    }
}
