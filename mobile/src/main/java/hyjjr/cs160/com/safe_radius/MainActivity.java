package hyjjr.cs160.com.safe_radius;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Global) getApplication()).setMainActivity(this);

        TabHost tabHost = getTabHost();

        TabHost.TabSpec tab1 = tabHost.newTabSpec(getString(R.string.tab1_name))
                .setIndicator(getString(R.string.tab1_name))
                .setContent(new Intent(this, SendActivity.class));
        TabHost.TabSpec tab2 = tabHost.newTabSpec(getString(R.string.tab2_name))
                .setIndicator(getString(R.string.tab2_name))
                .setContent(new Intent(this, RadarActivity.class));

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);


    }

    public void disableTab() {
        getTabWidget().setEnabled(false);
    }

    public void enableTab() {
        getTabWidget().setEnabled(true);
    }


}
