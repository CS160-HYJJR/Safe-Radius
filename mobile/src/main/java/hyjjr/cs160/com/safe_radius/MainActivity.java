package hyjjr.cs160.com.safe_radius;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab1_name)).setIndicator(getString(R.string.tab1_name), getResources().getDrawable(R.drawable.ic_send, getTheme())),
                SendFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab2_name)).setIndicator(getString(R.string.tab2_name), getResources().getDrawable(R.drawable.ic_map, getTheme())),
                RadarFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab3_name)).setIndicator(getString(R.string.tab3_name, getResources().getDrawable(R.drawable.ic_settings, getTheme()))),
                SettingsFragment.class, null);

        /*
        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 300;
        }*/

    }


}
