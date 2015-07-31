package hyjjr.cs160.com.safe_radius;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TextView;

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

        TextView view1 = new TextView(this);
        /*
        view1.setText(getString(R.string.tab1_name));
        view1.setHeight(20);
        view1.setWidth(50);*/
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab1_name)).setIndicator(getString(R.string.tab1_name)),
                SendFragment.class, null);

        TextView view2 = new TextView(this);
        /*
        view2.setText(getString(R.string.tab2_name));
        view2.setHeight(20);
        view2.setWidth(50);*/
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
