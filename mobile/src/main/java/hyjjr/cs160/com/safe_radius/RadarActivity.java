package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.os.Bundle;



public class RadarActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);
        Global.setRadarActivity(this);
    }
}
