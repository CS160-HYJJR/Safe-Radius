package hyjjr.cs160.com.safe_radius;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;


public class SendFragment extends Fragment {

    private static final String TAG = SendFragment.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;
    private View view;

    private static final int REQUEST_PARENT_PICTURE = 12;
    private static final int REQUEST_BACKGROUND = 24;
    private static final int REQUEST_SPEECH_TO_TEXT = 36;
    private static final int TRANSPARENCY_WHEN_OFF = 30;
    private static final String MESSAGE_PATH = "/message_mobile_to_wear";

    private View.OnClickListener powerButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (((Global) getActivity().getApplication()).isTurnedOn()) {
                turnOff();
            } else {
                turnOn();
            }
        }
    };

    private View.OnClickListener backgroundImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent2, REQUEST_BACKGROUND);
        }
    };

    private View.OnClickListener parentImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent2, REQUEST_PARENT_PICTURE);
        }
    };

    private View.OnClickListener speakButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Talk to your child");
            try {
                startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT);
            } catch (ActivityNotFoundException a) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "not supported",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 240);
                toast.show();
            }
        }
    };

    private AdapterView.OnItemSelectedListener messageSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
            if (position != 0 && position != parent.getCount() -1){
                getActivity().findViewById(R.id.send_button).setClickable(true);
            }
            if (position != parent.getCount() - 1)
                ((Global) getActivity().getApplication()).setMessageSelected(position);
            else { // last message selected
                final EditText input = new EditText(getActivity());
                input.setGravity(Gravity.CENTER);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Please write the new message")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newMessage = input.getText().toString();
                                SendFragment.this.messageSpinnerAddItem(newMessage);
                                ((Global) getActivity().getApplication()).setMessageSelected(position);
                                ((Spinner) SendFragment.this.view.findViewById(R.id.message_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getMessageSelected());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Spinner) SendFragment.this.view.findViewById(R.id.message_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getMessageSelected());
                            }
                        }).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener radiusSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

            if (position != parent.getCount() - 1) {
                ((Global) getActivity().getApplication()).setSafeRadiusSelected(position);
            } else { // last message selected
                final EditText input = new EditText(getActivity());
                input.setGravity(Gravity.CENTER);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Please write a new radius")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newRadius = input.getText().toString();
                                SendFragment.this.radiusSpinnerAddItem(newRadius);
                                ((Global) getActivity().getApplication()).setSafeRadiusSelected(position);
                                ((Spinner) SendFragment.this.view.findViewById(R.id.radius_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getSafeRadiusSelected());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Spinner) SendFragment.this.view.findViewById(R.id.radius_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getSafeRadiusSelected());
                            }
                        }).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener sendButtonListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", ((Global) getActivity().getApplication()).getMessage().getBytes());
            intent.putExtra("source", "phone");

            Handler handler = new Handler(Looper.getMainLooper());
            if (((Global) getActivity().getApplication()).isConnectedToWatch()) {
                ((Global) getActivity().getApplication()).setSentMessageTime(System.currentTimeMillis());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 340);
                        toast.show();
                        getActivity().startService(intent);
                    }
                });
            } else {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getActivity(), "Message Failed", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 340);
                        toast.show();
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/gotham.ttf");
        ((TextView)getActivity().findViewById(R.id.radius_text)).setTypeface(custom_font);
        ((TextView)getActivity().findViewById(R.id.radius_unit)).setTypeface(custom_font);
        view = getView();
        if (BuildConfig.DEBUG && view == null) {
            throw new AssertionError();
        }

        ArrayList<String> arrayList = new ArrayList<>();
        for (String s: ((Global) getActivity().getApplication()).getMessages()) {
            arrayList.add(s);
        }
        ((Spinner) view.findViewById(R.id.message_spinner)).setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, arrayList, true));

        ((Spinner) view.findViewById(R.id.message_spinner)).setOnItemSelectedListener(messageSpinnerListener);
        ((Spinner) view.findViewById(R.id.message_spinner)).setSelection(((Global) getActivity().getApplication()).getMessageSelected());

        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String s: ((Global) getActivity().getApplication()).getRadii()) {
            arrayList2.add(s);
        }

        ((Spinner) view.findViewById(R.id.radius_spinner)).setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, arrayList2, false));

        ((Spinner) getView().findViewById(R.id.radius_spinner)).setOnItemSelectedListener(radiusSpinnerListener);
        ((Spinner) getView().findViewById(R.id.radius_spinner)).setSelection(((Global) getActivity().getApplication()).getSafeRadiusSelected());

        (view.findViewById(R.id.send_button)).setOnClickListener(sendButtonListener);
        (view.findViewById(R.id.on_off_button)).setOnClickListener(powerButtonListener);

        (getView().findViewById(R.id.add_parent_button)).setOnClickListener(parentImageListener);
        ((ImageView) getView().findViewById(R.id.add_parent_button)).setImageBitmap(((Global) getActivity().getApplication()).getParentPicture());

        (getView().findViewById(R.id.change_background)).setOnClickListener(backgroundImageListener);
        ((TextView)getView().findViewById(R.id.message_history)).setText(((Global) getActivity().getApplication()).getMessageHistory());
        //(getView().findViewById(R.id.speak_button)).setOnClickListener(speakButtonListener);
        ((ImageView) getView().findViewById(R.id.background_pic)).setImageBitmap(((Global) getActivity().getApplication()).getBckgrdPicture());
        if (((Global) getActivity().getApplication()).getBckgrdPicture().sameAs((BitmapFactory.decodeResource(getResources(), R.drawable.title_safe_radius)))) {

        } else {
            ((ImageView) getView().findViewById(R.id.background_pic)).setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if (((Global) getActivity().getApplication()).isTurnedOn())
            turnOn();
        else
            turnOff();

        if (((Global) getActivity().getApplication()).getMessageSelected() == 0) {
            getActivity().findViewById(R.id.send_button).setClickable(false);
        }
    }

    private void turnOff() {
        ((Global) getActivity().getApplication()).turnOff();
        (view.findViewById(R.id.on_off_button)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_power_off));
        setTransparencyAll(TRANSPARENCY_WHEN_OFF);
        setEnabledAll(false);
        ((MainActivity)getActivity()).stopRequestLocation();
    }

    private void turnOn() {
        ((Global) getActivity().getApplication()).turnOn();
        (view.findViewById(R.id.on_off_button)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_power_on_1));
        setTransparencyAll(255);
        setEnabledAll(true);
        ((MainActivity)getActivity()).startRequestLocation();
    }


    /*
        Set transparency of All views except switch
     */
    private void setTransparencyAll(int transparency) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.send_fragment_layout);
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View view = viewGroup.getChildAt(i);
            if (view == null)
                continue;
            if (view.getId() == R.id.on_off_button) {
                continue;
            }
            view.setAlpha(transparency / 255.0f);
        }
        (getActivity().findViewById(android.R.id.tabs)).setAlpha(transparency / 255.0f);
        (getActivity().findViewById(R.id.connection_status)).setAlpha(transparency / 255.0f);
    }

    /*
    Set Enabled of All views except switch
 */
    private void setEnabledAll(boolean enabled) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.send_fragment_layout);
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View view = viewGroup.getChildAt(i);
            if (view == null)
                continue;
            if (view.getId() == R.id.on_off_button) {
                continue;
            }
            view.setEnabled(enabled);
        }
        (getActivity().findViewById(android.R.id.tabs)).setEnabled(enabled);
    }

    public void messageSpinnerAddItem(String item) {
        Spinner spinner = (Spinner) view.findViewById(R.id.message_spinner);
        SpinnerAdapter sa = spinner.getAdapter();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sa.getCount() - 1; i++) {
            list.add((String) sa.getItem(i));
        }
        list.add(item);
        list.add((String) sa.getItem(sa.getCount() - 1));
        ((Global) getActivity().getApplication()).setMessages(list.toArray(new String[1]));
        ArrayAdapter<String> adapter = new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, list, true);
        spinner.setAdapter(adapter);
    }

    public void radiusSpinnerAddItem(String item) {
        Spinner spinner = (Spinner) view.findViewById(R.id.radius_spinner);
        SpinnerAdapter sa = spinner.getAdapter();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sa.getCount() - 1; i++) {
            list.add((String) sa.getItem(i));
        }
        list.add(item);
        list.add((String) sa.getItem(sa.getCount() - 1));
        ((Global) getActivity().getApplication()).setRadii(list.toArray(new String[1]));
        ArrayAdapter<String> adapter = new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, list, false);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PARENT_PICTURE && resultCode == getActivity().RESULT_OK) {
            Log.d(TAG, "photo done");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ((Global)getActivity().getApplication()).setParentPicture(getRoundedCornerBitmapWithBorder(imageBitmap));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(((Global) getActivity().getApplication()).getParentPicture(), 300, 300, false);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapByte = stream.toByteArray();
            Log.d(TAG, "image size: " + bitmapByte.length);
            Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", SendMessageService.SEND_PARENT_PICTURE);
            intent.putExtra("message", bitmapByte);
            intent.putExtra("source", "phone");
            getActivity().startService(intent);
        } else if (requestCode == REQUEST_BACKGROUND && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView button = ((ImageView)(getView().findViewById(R.id.background_pic)));
            button.setBackground(new BitmapDrawable(getResources(), imageBitmap));
            ((Global)getActivity().getApplication()).setBckgrdPicture(imageBitmap);
        } else if (requestCode == REQUEST_SPEECH_TO_TEXT && resultCode == getActivity().RESULT_OK) {
            Log.d(TAG, "speech2");
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String message = result.get(0);
            Log.d(TAG, "speech to text result: " + message);
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 240);
            toast.show();
            Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", ((Global) getActivity().getApplication()).getMessage().getBytes());
            intent.putExtra("source", "phone");
            getActivity().startService(intent);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public static int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        int dimension;
        if (bitmap.getWidth() >= bitmap.getHeight())
        {
            dimension = bitmap.getHeight();
        }
        else
        {
            dimension = bitmap.getWidth();
        }
        return dimension;
    }

    public static Bitmap getRoundedCornerBitmapWithBorder(Bitmap bitmap) {

        int dimension = getSquareCropDimensionForBitmap(bitmap);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff4242DB;
        final Paint paint2 = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = bitmap.getWidth()/2;

        paint2.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint2.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint2);

        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint2);

        output = Bitmap.createScaledBitmap(output, 250, 250, false);

        return output;
    }

}
