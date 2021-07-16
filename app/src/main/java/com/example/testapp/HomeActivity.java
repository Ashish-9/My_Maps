package com.example.testapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private long backPressedTime;
    private Toast backToast;
    private SharedPreferences userPref;
    TextView tvName, tvEmail;
    String id,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment(),HomeFragment.class.getSimpleName()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        View headerView = navigationView.getHeaderView(0);
        name = userPref.getString("name","");
        id = userPref.getString("email","");
        tvName = headerView.findViewById(R.id.tvName);
        tvEmail = headerView.findViewById(R.id.tvEmail);
        tvName.setText(name);
        tvEmail.setText(id);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_map:
                startActivity(new Intent(this, MapsActivity.class));
                break;
            case R.id.nav_storage:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostList()).commit();
                break;
            case R.id.nav_uploads:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AccountFragment()).commit();
                break;
            case R.id.nav_about:
                break;
            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Do you want to logout?");
                builder.setPositiveButton("Logout", (dialog, which) -> logout());
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                });
                builder.show();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    private void logout() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.LOGOUT, res -> {

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")) {
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent((HomeActivity.this), AuthActivity.class));
                    finish();
                    Toasty.success(this, "Logged Out!", Toast.LENGTH_SHORT, true).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, Throwable::printStackTrace) {
            @Override
            public Map<String, String> getHeaders() {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        queue.add(request);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toasty.info(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}