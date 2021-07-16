package com.example.testapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment {

    Context context;
    private View view;
    ImageView selectedImage;
    ImageButton imagePicker;
    Button postbtn;
    Bitmap bitmap;
    String encodedimage;
    public LocationManager locationManager;
    TextView latitude, longitude, tvDistrict;
    EditText ettitle;

//    private long backPressedTime;
//    private Toast backToast;

    String selectedDistrict;
    private Spinner spinnerDistrict;

    public static SQLiteHelper sqLiteHelper;
    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;

    private ProgressDialog dialog;
    private SharedPreferences userPref;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home, container, false);
        context = container.getContext();
        init();
        return view;
    }

    private void init() {

        userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        selectedImage = view.findViewById(R.id.displayImageView);
        postbtn = view.findViewById(R.id.postbtn);
        latitude = view.findViewById(R.id.latitude);
        longitude = view.findViewById(R.id.longitude);
        ettitle = view.findViewById(R.id.ettitle);
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(true);
        location();
        sqLiteHelper = new SQLiteHelper(context, "PostDB.sqlite", null, 1);
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS POST(Id INTEGER PRIMARY KEY AUTOINCREMENT, desc VARCHAR, lat VARCHAR, longi VARCHAR, areaa VARCHAR, photo BLOB, status INTEGER)");

        imagePicker = view.findViewById(R.id.imagePicker);

        imagePicker.setOnClickListener(v -> ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(900)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start());

        postbtn.setOnClickListener(v -> {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo datac = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((wifi != null & datac != null) && (wifi.isConnected() || datac.isConnected())) {
                post();

            } else {
                //no connection
                insertToLocalDB();
            }

        });

        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        tvDistrict = view.findViewById(R.id.tvDistrict);
        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_districts, R.layout.spinner_layout);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDistrict = spinnerDistrict.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void insertToLocalDB() {
        try {
            sqLiteHelper.insertData(
                    ettitle.getText().toString(),
                    latitude.getText().toString(),
                    longitude.getText().toString(),
                    selectedDistrict,
                    imageViewToByte(selectedImage),
                    SYNC_STATUS_FAILED
            );

            Toasty.info(context, "Saved on device", Toast.LENGTH_SHORT, true).show();

            ettitle.setText("");
            selectedImage.setImageResource(R.drawable.placeholder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] imageViewToByte(ImageView selectedImage) {

        Bitmap bitmap = ((BitmapDrawable) selectedImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void location() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, location -> {
            longitude.setText(String.valueOf(location.getLongitude()));
            latitude.setText(String.valueOf(location.getLatitude()));
        });

    }

    private void post() {

        if (selectedDistrict.equals("Select Your District")) {
            Toasty.warning(context, "Please select district", Toast.LENGTH_SHORT, true).show();
            tvDistrict.setError("Required!");
        } else {
            tvDistrict.setError(null);
        }
        dialog.setMessage("Sending");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_POST, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    ettitle.setText("");
                    selectedImage.setImageResource(R.drawable.placeholder);
                    Toasty.success(context, "Sent!", Toast.LENGTH_SHORT, true).show();

                }
            } catch (JSONException e) {
                Toasty.error(context, "Error! Try Again", Toast.LENGTH_SHORT, true).show();
                e.printStackTrace();
            }
            dialog.dismiss();

        }, error -> {
            error.printStackTrace();
            Toasty.error(context, "Error! Try Again", Toast.LENGTH_SHORT, true).show();
            dialog.dismiss();
        }) {

            // add token to header


            @Override
            public Map<String, String> getHeaders() {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Accept", "application/json");
                map.put("Authorization", "Bearer " + token);
                return map;
            }
            // add params

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("desc", ettitle.getText().toString().trim());
                map.put("lat", latitude.getText().toString().trim());
                map.put("longi", longitude.getText().toString().trim());
                map.put("area", selectedDistrict);
                map.put("photo", encodedimage);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);

    }

    private void encodebitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteofimages = byteArrayOutputStream.toByteArray();
        encodedimage = android.util.Base64.encodeToString(byteofimages, Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri filepath = data.getData();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(filepath);
            bitmap = BitmapFactory.decodeStream(inputStream);
            selectedImage.setImageBitmap(bitmap);
            encodebitmap(bitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    //    @Override
//    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_account, menu);
//        return true;
//    }
//
//    @SuppressLint("NonConstantResourceId")
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        switch (item.getItemId()) {
//
//            case R.id.item_maps: {
//                startActivity(new Intent(this, MapsActivity.class));
//                break;
//            }
//            case R.id.item_local: {
//                startActivity(new Intent(this, PostList.class));
//                break;
//            }
//            case R.id.item_sync: {
//
//                dialog.setMessage("SYNCING");
//                dialog.show();
//
//                ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//                android.net.NetworkInfo datac = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//                if ((wifi != null & datac != null) && (wifi.isConnected() || datac.isConnected())) {
//
//                    SQLiteHelper sqLiteHelper = new SQLiteHelper(this);
//                    sqLiteHelper.getWritableDatabase();
//
//                    Cursor cursor = HomeActivity.sqLiteHelper.getData("SELECT Id,desc,lat,longi,areaa,photo,status FROM POST");
//
//                    while (cursor.moveToNext()) {
//
//                        int Id = cursor.getInt(cursor.getColumnIndex("Id"));
//                        String desc = cursor.getString(cursor.getColumnIndex("desc"));
//                        String lat = cursor.getString(cursor.getColumnIndex("lat"));
//                        String longi = cursor.getString(cursor.getColumnIndex("longi"));
//                        String areaa = cursor.getString(cursor.getColumnIndex("areaa"));
//                        byte[] photo = cursor.getBlob(cursor.getColumnIndex("photo"));
//                        int status = cursor.getInt(cursor.getColumnIndex("status"));
//
//                        if (status == SYNC_STATUS_FAILED) {
//
//
//                            StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_POST, response -> {
//
//                                try {
//                                    JSONObject object = new JSONObject(response);
//                                    if (object.getBoolean("success")) {
//                                        Toasty.success(this, "Synced!", Toast.LENGTH_SHORT, true).show();
//
//                                        sqLiteHelper.updateData(Id);
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                dialog.dismiss();
//
//                            }, Throwable::printStackTrace) {
//
//                                // add token to header
//
//
//                                @Override
//                                public Map<String, String> getHeaders() {
//                                    String token = userPref.getString("token", "");
//                                    HashMap<String, String> map = new HashMap<>();
//                                    map.put("Accept", "application/json");
//                                    map.put("Authorization", "Bearer " + token);
//                                    return map;
//                                }
//                                // add params
//
//                                @Override
//                                protected Map<String, String> getParams() {
//                                    HashMap<String, String> map = new HashMap<>();
//                                    map.put("desc", desc);
//                                    map.put("lat", lat);
//                                    map.put("longi", longi);
//                                    map.put("area", areaa);
//                                    map.put("photo", encodeimage(photo));
//                                    return map;
//                                }
//                            };
//
//                            RequestQueue queue = Volley.newRequestQueue(this);
//                            queue.add(request);
//
//                        } else {
//                            dialog.dismiss();
//                            Toasty.success(this, "Nothing to sync!", Toast.LENGTH_SHORT, true).show();
//
//                        }
//
//                    }
//
//                } else {
//                    dialog.dismiss();
//                    Toasty.error(this, "No Internet!", Toast.LENGTH_SHORT, true).show();
//
//                }
//                break;
//            }
//            case R.id.item_logout: {
//                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
//                builder.setTitle("");
//                builder.setMessage("Do you want to logout?");
//                builder.setPositiveButton("Logout", (dialog, which) -> logout());
//                builder.setNegativeButton("Cancel", (dialog, which) -> {
//                });
//                builder.show();
//                break;
//            }
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private String encodeimage(byte[] photo) {
//        return Base64.encodeToString(photo, Base64.DEFAULT);
//    }
//
//    private void logout() {
//        StringRequest request = new StringRequest(Request.Method.GET, Constant.LOGOUT, res -> {
//
//            try {
//                JSONObject object = new JSONObject(res);
//                if (object.getBoolean("success")) {
//                    SharedPreferences.Editor editor = userPref.edit();
//                    editor.clear();
//                    editor.apply();
//                    startActivity(new Intent(getActivity(), AuthActivity.class));
//                    Toasty.success(getActivity(), "Logged Out!", Toast.LENGTH_SHORT, true).show();
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        }, Throwable::printStackTrace) {
//            @Override
//            public Map<String, String> getHeaders() {
//                String token = userPref.getString("token", "");
//                HashMap<String, String> map = new HashMap<>();
//                map.put("Authorization", "Bearer " + token);
//                return map;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(getActivity());
//        queue.add(request);
//    }

}