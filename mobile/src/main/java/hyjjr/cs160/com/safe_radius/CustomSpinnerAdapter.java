package hyjjr.cs160.com.safe_radius;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
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
    TextView spnItemDel;

    public CustomSpinnerAdapter(Context context, int textViewResourceId, ArrayList<String> iName){
        super(context,textViewResourceId,iName);
        this.context = context;
        this.iName = iName;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView =  inflater.inflate(R.layout.custom_spinner, parent, false);

        spnItemName = (TextView) rowView.findViewById(R.id.spnText);
        spnItemDel = (TextView) rowView.findViewById(R.id.spnDel);

        spnItemName.setText(iName.get(position));
        spnItemDel.setText("X");
        if (position == iName.size() -1) {
            spnItemDel.setVisibility(View.GONE);
            spnItemDel.setEnabled(false);
        }
        spnItemDel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 240);
                toast.show();

                //iName[position] = null;
                if (iName.size() > 2) {
                    if (((Global) getContext().getApplicationContext()).getMessage().equals(iName.get(position)))
                    {
                        ((Global) getContext().getApplicationContext()).setMessageSelected(0);
                    }
                    iName.remove(position);
                    ((Global) getContext().getApplicationContext()).setMessages(iName.toArray(new String[1]));
                    notifyDataSetChanged();
                }
            }
        });
        return rowView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView =  inflater.inflate(R.layout.custom_spinner, parent, false);

        spnItemName = (TextView) rowView.findViewById(R.id.spnText);
        spnItemDel = (TextView) rowView.findViewById(R.id.spnDel);

        spnItemName.setText(iName.get(position));
        spnItemName.setBackgroundColor(context.getResources().getColor(R.color.white));
        spnItemName.setTextColor(context.getResources().getColor(R.color.black));
        spnItemDel.setText("X");
        spnItemDel.setVisibility(View.GONE);
        spnItemDel.setEnabled(false);
//        spnItemDel.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Toast toast = Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 240);
//                toast.show();
//                //iName[position] = null;
//                if (iName.size() > 2) {
//                    if (((Global) getContext().getApplicationContext()).getMessage().equals(iName.get(position)))
//                    {
//                        ((Global) getContext().getApplicationContext()).setMessageSelected(0);
//                    }
//                    iName.remove(position);
//                    ((Global) getContext().getApplicationContext()).setMessages(iName.toArray(new String[1]));
//                    notifyDataSetChanged();
//                }
//            }
//        });
        return rowView;
    }

}