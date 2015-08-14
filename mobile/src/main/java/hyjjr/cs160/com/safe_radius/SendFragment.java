package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class SendFragment extends Fragment {

    private static final String TAG = SendFragment.class.getSimpleName();
    public static final String FONTS_GOTHAM_TTF = "fonts/gotham.ttf";
    private View view;

    public SharedPreferences prefs;

    private static final int REQUEST_PARENT_PICTURE = 12;
    private static final int REQUEST_BACKGROUND = 24;
    private static final int TRANSPARENCY_WHEN_OFF = 30;
    private static final String MESSAGE_PATH = "/message_mobile_to_wear";


    private Spinner radiusSpinner;
    private Spinner messageSpinner;
    private ImageButton addParentButton;
    private ImageButton backgroundButton;
    private ImageButton sendButton;
    private ImageButton powerButton;
    private ImageView backgroundImage;

    private ArrayList<String> messages;
    private ArrayList<String> radiuses;

    private View.OnClickListener powerButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (prefs.getBoolean(Global.KEY_POWER_BOOLEAN, false)) {
                turnOff();
            } else {
                turnOn();
            }
        }
    };

    private View.OnClickListener backgroundImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_BACKGROUND);
        }
    };

    private View.OnClickListener parentImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_PARENT_PICTURE);
        }
    };

    private View.OnClickListener sendButtonListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", prefs.getString(Global.KEY_MESSAGE_STRING, "").getBytes());
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
        view = getView();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), FONTS_GOTHAM_TTF);
        ((TextView)getActivity().findViewById(R.id.radius_text)).setTypeface(custom_font);

        String[] defaultMessage = getResources().getStringArray(R.array.message_choices);
        int messageCount = prefs.getInt(Global.KEY_MESSAGE_COUNT, defaultMessage.length);
        messages = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            messages.add(prefs.getString(Global.KEY_MESSAGES_+i, i<defaultMessage.length?defaultMessage[i]:""));
        }
        messageSpinner = (Spinner)view.findViewById(R.id.message_spinner);
        messageSpinner.setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, messages, messageSpinner, true ,"Please write the new message"));
        messageSpinner.setSelection(prefs.getInt(Global.KEY_MESSAGE_POSITION_INT, 0));
        editor.putString(Global.KEY_MESSAGE_STRING, messages.get(prefs.getInt(Global.KEY_MESSAGE_POSITION_INT, 0)));

        String[] defaultRadius = getResources().getStringArray(R.array.radius_choices);

        int radiusCount = prefs.getInt(Global.KEY_RADIUS_COUNT, defaultRadius.length);
        radiuses = new ArrayList<>();
        for (int i = 0; i < radiusCount; i++) {
            radiuses.add(prefs.getString(Global.KEY_RADIUSES_+i, i<defaultRadius.length?defaultRadius[i]:""));
        }
        radiusSpinner = (Spinner)view.findViewById(R.id.radius_spinner);
        radiusSpinner.setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner,radiuses, radiusSpinner, false, "Please write the new radius"));
        radiusSpinner = (Spinner)view.findViewById(R.id.radius_spinner);
        radiusSpinner.setSelection(prefs.getInt(String.valueOf(R.id.radius_spinner), 3));
        editor.putString(Global.KEY_RADIUS_STRING, radiuses.get(prefs.getInt(Global.KEY_RADIUS_POSITION_INT, 3)));


        powerButton = (ImageButton)view.findViewById(R.id.on_off_button);
        powerButton.setOnClickListener(powerButtonListener);

        addParentButton = (ImageButton)view.findViewById(R.id.add_parent_button);
        addParentButton.setOnClickListener(parentImageListener);
        String parentImageString = prefs.getString(Global.KEY_ADD_PARENT_STRING, null);
        if (parentImageString == null) {
            addParentButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_new_ppl));
        } else {
            addParentButton.setImageBitmap(getRoundedCornerBitmapWithBorder(string2Bitmap(parentImageString)));
            addParentButton.setBackgroundColor(0x00000000);
        }

        backgroundButton = (ImageButton)view.findViewById(R.id.change_background);
        backgroundButton.setOnClickListener(backgroundImageListener);
        backgroundImage = (ImageView)view.findViewById(R.id.background_pic);
        String backgroundString = prefs.getString(Global.KEY_BACKGROUND_STRING, null);
        if (backgroundString == null) {
            backgroundImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title_safe_radius));
        } else {
            backgroundImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.title_safe_radius));
            backgroundImage.setBackground(new BitmapDrawable(getResources(),string2Bitmap(backgroundString)));
        }

        if (prefs.getBoolean(Global.KEY_POWER_BOOLEAN, true))
            turnOn();
        else
            turnOff();

        sendButton = (ImageButton)view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(sendButtonListener);

        if (prefs.getInt(Global.KEY_MESSAGE_POSITION_INT, 0) == 0) {
            sendButton.setClickable(false);
            sendButton.setEnabled(false);
        }

        editor.apply();
    }

    @Override
    public void onStop() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(String.valueOf(R.id.radius_spinner), radiusSpinner.getSelectedItemPosition());
        editor.apply();
        super.onStop();
    }

    private void turnOff() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Global.KEY_POWER_BOOLEAN, false);
        editor.apply();
        (view.findViewById(R.id.on_off_button)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_power_off));
        setTransparencyAll(TRANSPARENCY_WHEN_OFF);
        setEnabledAll(false);
        ((MainActivity)getActivity()).stopRequestLocation();
    }

    private void turnOn() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Global.KEY_POWER_BOOLEAN, true);
        editor.apply();
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

    public static String bitmap2String(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap string2Bitmap(String imageString) {
        byte[] b = Base64.decode(imageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PARENT_PICTURE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "photo done");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = getRoundedCornerBitmapWithBorder(imageBitmap);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Global.KEY_ADD_PARENT_STRING, bitmap2String(imageBitmap));
            editor.apply();

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 300, 300, false);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapByte = stream.toByteArray();
            Log.d(TAG, "image size: " + bitmapByte.length);
            Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", SendMessageService.SEND_PARENT_PICTURE);
            intent.putExtra("message", bitmapByte);
            intent.putExtra("source", "phone");
            getActivity().startService(intent);
        } else if (requestCode == REQUEST_BACKGROUND && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Global.KEY_BACKGROUND_STRING, bitmap2String(imageBitmap));
            editor.apply();
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
