package hyjjr.cs160.com.safe_radius;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class SendFragment extends Fragment {

    private static final String TAG = SendFragment.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;
    private View view;

    private static final int REQUEST_PARENT_PICTURE = 12;
    private static final int REQUEST_BACKGROUND = 24;
    private static final int TRANSPARENCY_WHEN_OFF = 30;

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

    private AdapterView.OnItemSelectedListener messageSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
            if (position != parent.getCount() - 1)
                ((Global) getActivity().getApplication()).setMessageSelected(position);
            else { // last message selected
                final EditText input = new EditText(getActivity());

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
            if (position != parent.getCount() - 1)
                ((Global) getActivity().getApplication()).setSafeRadiusSelected(position);
            else { // last message selected
                final EditText input = new EditText(getActivity());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Please write the new radius")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newRadius = input.getText().toString();
                                SendFragment.this.radiusSpinnerAddItem(newRadius);
                                ((Global) getActivity().getApplication()).setMessageSelected(position);
                                ((Spinner) SendFragment.this.view.findViewById(R.id.radius_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getSafeRadiusSelected());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Spinner) SendFragment.this.view.findViewById(R.id.message_spinner)).setSelection(
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
        private static final String MESSAGE_PATH = "/message_mobile_to_wear";

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), GcmSendMessage.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", ((Global) getActivity().getApplication()).getMessage().getBytes());
            intent.putExtra("source", "phone");
            getActivity().startService(intent);
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast toast = Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 240);
                    toast.show();
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        view = getView();
        if (BuildConfig.DEBUG && view == null) {
            throw new AssertionError();
        }

        ArrayList<String> arrayList = new ArrayList<>();
        for (String s: ((Global) getActivity().getApplication()).getMessages()) {
            arrayList.add(s);
        }
        ((Spinner) view.findViewById(R.id.message_spinner)).setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, arrayList));

        ((Spinner) view.findViewById(R.id.message_spinner)).setOnItemSelectedListener(messageSpinnerListener);
        ((Spinner) view.findViewById(R.id.message_spinner)).setSelection(((Global) getActivity().getApplication()).getMessageSelected());

        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String s: ((Global) getActivity().getApplication()).getRadii()) {
            arrayList2.add(s);
        }

        ((Spinner) view.findViewById(R.id.radius_spinner)).setAdapter(new CustomSpinnerAdapter(getActivity(),
                R.layout.custom_spinner, arrayList2));

        ((Spinner) getView().findViewById(R.id.radius_spinner)).setOnItemSelectedListener(radiusSpinnerListener);
        ((Spinner) getView().findViewById(R.id.radius_spinner)).setSelection(((Global) getActivity().getApplication()).getSafeRadiusSelected());

        (view.findViewById(R.id.send_button)).setOnClickListener(sendButtonListener);
        (view.findViewById(R.id.on_off_button)).setOnClickListener(powerButtonListener);

        (getView().findViewById(R.id.add_parent_button)).setOnClickListener(parentImageListener);
        ((ImageButton) getView().findViewById(R.id.add_parent_button)).setImageBitmap(getRoundedCornerBitmapWithBorder(((Global) getActivity().getApplication()).getParentPicture()));

        (getView().findViewById(R.id.change_background)).setOnClickListener(backgroundImageListener);
        if (((Global) getActivity().getApplication()).isTurnedOn())
            turnOn();
        else
            turnOff();
    }

    private void turnOff() {
        ((Global) getActivity().getApplication()).turnOff();
        (view.findViewById(R.id.on_off_button)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_power_off));
        setTransparencyAll(TRANSPARENCY_WHEN_OFF);
        setEnabledAll(false);
        if (mGoogleApiClient != null) {
            //Wearable.NodeApi.removeListener(mGoogleApiClient, connectionListener);
            mGoogleApiClient.disconnect();
        }
    }

    private void turnOn() {
        ((Global) getActivity().getApplication()).turnOn();
        (view.findViewById(R.id.on_off_button)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_power_on));
        setTransparencyAll(255);
        setEnabledAll(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
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
            view.setAlpha(transparency/255.0f);
        }
        (getActivity().findViewById(android.R.id.tabs)).setAlpha(transparency / 255.0f);
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
                R.layout.custom_spinner, list);
        spinner.setAdapter(adapter);
    }

    public void radiusSpinnerAddItem(String item) {
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
                R.layout.custom_spinner, list);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PARENT_PICTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ((Global)getActivity().getApplication()).setParentPicture(imageBitmap);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 32, 32, false);
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
            ImageButton button = ((ImageButton)(getView().findViewById(R.id.add_parent_button)));
            button.setBackground(new BitmapDrawable(getResources(), imageBitmap));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap getRoundedCornerBitmapWithBorder(Bitmap bitmap) {

        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader(bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, 100, paint);

        Bitmap output = Bitmap.createBitmap(circleBitmap.getWidth(), circleBitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff4242DB;
        final Paint paint2 = new Paint();
        final Rect rect = new Rect(0, 0, circleBitmap.getWidth(), circleBitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = circleBitmap.getWidth()/2;

        paint2.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint2.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint2);

        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint2);


        // add border begins
        final int borderSize = 10; // set border size here
        Bitmap bitmapWithBorder= Bitmap.createBitmap(output.getWidth() + borderSize * 2, output.getHeight() + borderSize * 2, output.getConfig());
        Canvas canvas3 = new Canvas(bitmapWithBorder);
        canvas3.drawColor(Color.BLACK);
        canvas3.drawBitmap(output, borderSize, borderSize, null);
        bitmap = bitmapWithBorder;

        // add border finishes
        Bitmap circleBitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader2 = new BitmapShader(bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint3 = new Paint();
        paint3.setShader(shader2);
        paint3.setAntiAlias(true);
        Canvas c2 = new Canvas(circleBitmap2);
        c2.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, 100, paint3);

        Bitmap output2 = Bitmap.createBitmap(circleBitmap2.getWidth(), circleBitmap2
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(output2);

        final int color2 = 0xff4242DB;
        final Paint paint4 = new Paint();
        final Rect rect2 = new Rect(0, 0, circleBitmap2.getWidth(), circleBitmap2.getHeight());
        final RectF rectF2 = new RectF(rect2);
        final float roundPx2 = circleBitmap2.getWidth()/2;

        paint4.setAntiAlias(true);
        canvas2.drawARGB(0, 0, 0, 0);
        paint4.setColor(color2);
        canvas2.drawRoundRect(rectF2, roundPx2, roundPx2, paint4);

        paint4.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas2.drawBitmap(bitmap, rect2, rect2, paint4);
        return output2;
    }
}
