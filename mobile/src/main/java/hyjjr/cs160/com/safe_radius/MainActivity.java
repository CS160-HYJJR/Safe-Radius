package hyjjr.cs160.com.safe_radius;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentTabHost mTabHost;
    private double safeRadius; //unit feet
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab1_name)).setIndicator(getString(R.string.tab1_name)),
                SendFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab2_name)).setIndicator(getString(R.string.tab2_name)),
                RadarFragment.class, null);
    }

    public double getSafeRadius() {
        return safeRadius;
    }

    public void setSafeRadius(double radius) {
        safeRadius = radius;
    }

    public double getSafeRadiusInMeter() {
        return safeRadius * 0.3048;
    }

}
