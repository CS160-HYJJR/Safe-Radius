package hyjjr.cs160.com.safe_radius;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;


public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static View view;
    private static final int REQUEST_IMAGE_CAPTURE = 12345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "log ok");
        if (getView() != null) {
            Log.d(TAG, "imageListener");
            ((ImageButton)getView().findViewById(R.id.add_parent_button)).setOnClickListener(parentImageListener);
            ((ImageButton) getView().findViewById(R.id.add_parent_button)).
                    setBackground(new BitmapDrawable(getResources(), ((Global) getActivity().getApplication()).getParentPicture()));

                    ((Spinner) getView().findViewById(R.id.radius_spinner)).setOnItemSelectedListener(radiusSpinnerListener);
                  ((Spinner) getView().findViewById(R.id.radius_spinner)).setSelection(((Global) getActivity().getApplication()).getSafeRadiusSelected());
        }
    }


    private View.OnClickListener parentImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "image clicked");
            final Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent2, REQUEST_IMAGE_CAPTURE);
        }
    };

    private AdapterView.OnItemSelectedListener radiusSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ((Global) getActivity().getApplication()).setSafeRadiusSelected(position);
            int length = parent.getSelectedItem().toString().length();
            ((Global) getActivity().getApplication()).setSafeRadius(Double.valueOf(parent.getSelectedItem().toString().substring(0, length-3)));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Log.d(TAG, "Camera");
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ((Global)getActivity().getApplication()).setParentPicture(imageBitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapByte = stream.toByteArray();
            Intent intent = new Intent(getActivity(), SendMessageService.class);
            intent.putExtra("message_path", SendMessageService.SEND_PARENT_PICTURE);
            intent.putExtra("message", bitmapByte);
            getActivity().startService(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}

