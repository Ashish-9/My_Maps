package com.example.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.testapp.model.ListLocationModel;
import com.example.testapp.model.LocationModel;
import com.example.testapp.network.ApiClient;
import com.example.testapp.network.ApiService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LocationModel> mListMaker = new ArrayList<>();

    String URLString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getAllDataLocation();
    }

    private void getAllDataLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting..");
        progressDialog.show();

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Call<ListLocationModel> call = apiService.getAllLocation();

        call.enqueue(new Callback<ListLocationModel>() {
            @Override
            public void onResponse(Call<ListLocationModel> call, Response<ListLocationModel> response) {
                progressDialog.dismiss();
                mListMaker = response.body().getmPosts();
                initMarker(mListMaker);
            }

            @Override
            public void onFailure(Call<ListLocationModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMarker(List<LocationModel> mListMaker) {
        for(int i = 0; i<mListMaker.size(); i++){
            LatLng location = new LatLng(Double.parseDouble(mListMaker.get(i).getLat()),
                    Double.parseDouble(mListMaker.get(i).getLongi()));
            Marker marker = mMap.addMarker(new MarkerOptions().position(location)
                    .title(mListMaker.get(i).getDesc())
                    .snippet(mListMaker.get(i).getPhoto()));

            LocationModel info = new LocationModel();
            info.setPhoto(mListMaker.get(i).getPhoto());
            marker.setTag(info);

            LatLng latLng = new LatLng(Double.parseDouble(mListMaker.get(0).getLat()), Double.parseDouble(mListMaker.get(0).getLongi()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude),11.0f));
            if(mListMaker.size() != 0){
                TestInfoWindowAdapter testInfoWindowAdapter = new TestInfoWindowAdapter(this);
                mMap.setInfoWindowAdapter(testInfoWindowAdapter);
            }
        }
    }

    private class TestInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        private final Context context;

        public TestInfoWindowAdapter(Context context){
            this.context = context;
        }

        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }

        @Nullable
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            View view = ((Activity)context).getLayoutInflater().inflate(R.layout.info_popup, null);

            TextView maptitle = view.findViewById(R.id.maptitle);
            ImageView mapphoto = view.findViewById(R.id.mapphoto);

            maptitle.setText(marker.getTitle());

            LocationModel infomodel = (LocationModel) marker.getTag();
            URLString = infomodel.getPhoto();
            Glide.with(getApplicationContext())
                    .load("http://192.168.43.152:80/storage/posts/"+URLString)
                    .into(mapphoto);
            return view;
        }
    }



}
