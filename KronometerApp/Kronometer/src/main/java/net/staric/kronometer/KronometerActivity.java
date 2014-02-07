package net.staric.kronometer;

import android.app.Activity;
import android.view.Menu;


public class KronometerActivity extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


}
