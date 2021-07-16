package com.example.testapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class PostList extends Fragment {

    GridView gridView;
    ArrayList<Post> list;
    PostListAdapter adapter = null;
    private View view;
    Button btnsync;
    private ProgressDialog dialog;
    private SharedPreferences userPref;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.post_list_activity, container, false);
        init();
        return view;
        }

    private void init() {

        gridView = view.findViewById(R.id.gridView);
        list = new ArrayList<>();
        adapter = new PostListAdapter(getContext(), R.layout.post_items, list);
        gridView.setAdapter(adapter);
        btnsync = view.findViewById(R.id.btnsync);
        btnsync.setOnClickListener(v -> sync());
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        userPref = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);

        Cursor cursor = HomeFragment.sqLiteHelper.getData("SELECT * FROM POST WHERE status = 1");
        list.clear();
        while (cursor.moveToNext()) {
            int Id = cursor.getInt(0);
            String desc = cursor.getString(1);
            String lat = cursor.getString(2);
            String longi = cursor.getString(3);
            String areaa = cursor.getString(4);
            byte[] photo = cursor.getBlob(5);
            int status = cursor.getInt(6);

            list.add(new Post(Id, desc, lat, longi, areaa, photo, status));
        }
        adapter.notifyDataSetChanged();

        gridView.setOnItemLongClickListener((parent, view, position, id) -> false);
    }

    private void sync() {
        dialog.setMessage("SYNCING");
        dialog.show();

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo datac = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null & datac != null) && (wifi.isConnected() || datac.isConnected())) {

            SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext());
            sqLiteHelper.getWritableDatabase();

            Cursor cursor = HomeFragment.sqLiteHelper.getData("SELECT Id,desc,lat,longi,areaa,photo,status FROM POST");

            while (cursor.moveToNext()) {

                int Id = cursor.getInt(cursor.getColumnIndex("Id"));
                String desc = cursor.getString(cursor.getColumnIndex("desc"));
                String lat = cursor.getString(cursor.getColumnIndex("lat"));
                String longi = cursor.getString(cursor.getColumnIndex("longi"));
                String areaa = cursor.getString(cursor.getColumnIndex("areaa"));
                byte[] photo = cursor.getBlob(cursor.getColumnIndex("photo"));
                int status = cursor.getInt(cursor.getColumnIndex("status"));

                if (status == 1) {


                    StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_POST, response -> {

                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getBoolean("success")) {
                                Toasty.success(getActivity(), "Synced!", Toast.LENGTH_SHORT, true).show();

                                sqLiteHelper.updateData(Id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                    }, Throwable::printStackTrace) {

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
                            map.put("desc", desc);
                            map.put("lat", lat);
                            map.put("longi", longi);
                            map.put("area", areaa);
                            map.put("photo", encodeimage(photo));
                            return map;
                        }
                    };

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    queue.add(request);

                } else {
                    dialog.dismiss();
                    Toasty.success(getActivity(), "Nothing to sync!", Toast.LENGTH_SHORT, true).show();

                }

            }

        } else {
            dialog.dismiss();
            Toasty.error(getContext(), "No Internet!", Toast.LENGTH_SHORT, true).show();

        }
    }

    private String encodeimage(byte[] photo) {
        return Base64.encodeToString(photo, Base64.DEFAULT);
    }


}
