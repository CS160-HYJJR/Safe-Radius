package hyjjr.cs160.com.safe_radius;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<String> iName;

    TextView spnItemName;
    ImageButton spnItemDel;

    public CustomSpinnerAdapter(Context context, int textViewResourceId, ArrayList<String> objects, ArrayList<String> iName){
        super(context,textViewResourceId,objects);
        this.context = context;
        this.iName = iName;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView =  inflater.inflate(R.layout.custom_spinner, parent, false);

        spnItemName = (TextView) rowView.findViewById(R.id.spnText);
        spnItemDel = (ImageButton) rowView.findViewById(R.id.spnDel);

        spnItemName.setText(iName.get(position));

        spnItemDel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                //iName[position] = null;
                iName.remove(position);
                if ((Global)get)
                notifyDataSetChanged();
            }
        });
        return rowView;
    }
}