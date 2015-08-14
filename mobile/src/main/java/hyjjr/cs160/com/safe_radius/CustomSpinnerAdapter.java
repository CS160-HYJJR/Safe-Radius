package hyjjr.cs160.com.safe_radius;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    public static final String FONTS_GOTHAM_TTF = "fonts/gotham.ttf";
    private final Context context;
    private final ArrayList<String> iName;
    private final boolean isMessageSpinner;
    private final SharedPreferences prefs;
    private final String prompt;
    private final Spinner spinner;
    private int selectedPos;

    TextView spnItemName;
    TextView spnItemDel;

    public CustomSpinnerAdapter(final Context context, int textViewResourceId, final ArrayList<String> iName, final Spinner spinner,
                                final boolean isMessageSpinner, final String prompt){
        super(context,textViewResourceId,iName);
        this.context = context;
        this.iName = iName;
        this.isMessageSpinner = isMessageSpinner;
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.spinner = spinner;
        this.prompt = prompt;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (position != 0 && position != iName.size() - 1 && isMessageSpinner) {
                    ((Activity) context).findViewById(R.id.send_button).setClickable(true);
                    ((Activity) context).findViewById(R.id.send_button).setEnabled(true);
                }
                if (position != iName.size() - 1) {
                    selectedPos = position;
                    notifyDataSetChanged();
                    spinner.setSelection(selectedPos);
                    updatePref();
                } else {
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    if (isMessageSpinner) {
                        spinner.setSelection(prefs.getInt(Global.KEY_MESSAGE_POSITION_INT, 1));
                    } else {
                        spinner.setSelection(prefs.getInt(Global.KEY_RADIUS_POSITION_INT, 0));
                    }
                    new AlertDialog.Builder(context)
                            .setTitle(prompt)
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String newMessage = input.getText().toString();
                                    iName.add(iName.size() - 1, newMessage);
                                    spinner.setSelection(iName.size()-2);
                                    selectedPos = iName.size()-2;
                                    notifyDataSetChanged();
                                    updatePref();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public View getDropDownView(final int position, final View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView =  inflater.inflate(R.layout.custom_spinner, parent, false);

        spnItemName = (TextView) rowView.findViewById(R.id.spnText);
        spnItemDel = (TextView) rowView.findViewById(R.id.spnDel);
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/gotham.ttf");
        spnItemName.setTypeface(custom_font);
        spnItemDel.setTypeface(custom_font);
        spnItemDel.setText("X");
        spnItemName.setText(iName.get(position));

        if ((position == 0 && isMessageSpinner)) {
            spnItemName.setVisibility(View.GONE);
            spnItemDel.setVisibility(View.GONE);
            rowView.setVisibility(View.GONE);
            spnItemDel.setEnabled(false);
        }

        if ((!isMessageSpinner && iName.size() <= 2) || (isMessageSpinner && iName.size() <= 3)) {
            spnItemDel.setVisibility(View.GONE);
            spnItemDel.setEnabled(false);
        }

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
                CustomSpinnerAdapter.super.remove(iName.get(position));
                notifyDataSetChanged();

                if (isMessageSpinner) {
                    if (position == selectedPos) {
                        selectedPos = 1;
                        spinner.setSelection(selectedPos);
                    } else if (position < selectedPos) {
                        selectedPos -=1;
                        spinner.setSelection(selectedPos);
                    } else {
                        spinner.setSelection(selectedPos);
                    }
                } else {
                    if (position == selectedPos ) {
                        selectedPos = 0;
                        spinner.setSelection(selectedPos);
                    } else if (position < selectedPos) {
                        selectedPos -= 1;
                        spinner.setSelection(selectedPos);
                    } else {
                        spinner.setSelection(selectedPos);
                    }
                }
                updatePref();
            }
        });


        return rowView;
    }

    private void updatePref() {
        SharedPreferences.Editor editor = prefs.edit();
        if (isMessageSpinner) {
            editor.putInt(Global.KEY_MESSAGE_COUNT, iName.size());
            editor.putString(Global.KEY_MESSAGE_STRING, iName.get(selectedPos));
            editor.putInt(Global.KEY_MESSAGE_POSITION_INT, selectedPos);
            for (int i = 0; i < iName.size(); i++) {
                editor.putString(Global.KEY_MESSAGES_ + i, iName.get(i));
            }
        } else {
            editor.putInt(Global.KEY_RADIUS_COUNT, iName.size());
            editor.putString(Global.KEY_RADIUS_STRING, iName.get(selectedPos));
            editor.putInt(Global.KEY_RADIUS_POSITION_INT, selectedPos);
            for (int i = 0; i < iName.size(); i++) {
                editor.putString(Global.KEY_RADIUSES_ + i, iName.get(i));
            }
        }
        editor.commit();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView = inflater.inflate(R.layout.custom_spinner, parent, false);

        spnItemName = (TextView) rowView.findViewById(R.id.spnText);
        spnItemDel = (TextView) rowView.findViewById(R.id.spnDel);
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(),FONTS_GOTHAM_TTF);
        spnItemName.setTypeface(custom_font);

        spnItemName.setText(iName.get(position));
        spnItemName.setBackgroundColor(context.getResources().getColor(R.color.white));
        spnItemName.setTextColor(context.getResources().getColor(R.color.black));

        spnItemDel.setVisibility(View.GONE);
        spnItemDel.setEnabled(false);

        if (isMessageSpinner && position == 0) {
            spnItemName.setTextColor(0xffcbcbcb);
        }
        return rowView;
    }

}