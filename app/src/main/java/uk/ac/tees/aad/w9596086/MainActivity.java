package uk.ac.tees.aad.w9596086;


import android.Manifest;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle toggle;
    private RequestQueue mRequestQueue;
    private ArrayList<Book> books;
    private ProgressBar progressBar;
    private EditText searchEdt;
    private ImageButton searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_my_books:
                        startActivity(new Intent(MainActivity.this, MyEBooks.class));
//                        Toast.makeText(getApplicationContext(),)
                        return true;
                    case R.id.nav_profile:
                        startActivity(new Intent(MainActivity.this, Profile.class));
                        return true;
                    case R.id.nav_location:
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            buildAlertMessageNoGps();
                            return false;
                        }
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
//                                PackageManager.PERMISSION_GRANTED) {
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1004);
//                            return false;
//                        }
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1005);
                            return false;
                        }
                        else {
                            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                            startActivity(intent);
                            return true;
                        }
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(),"Successfully Logged out!",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finishAffinity();
                        return true;
                    default:
                        return false;
                }

            }
        });


        progressBar = findViewById(R.id.progress_circular);
        searchEdt = findViewById(R.id.idEdtSearchBooks);
        searchBtn = findViewById(R.id.idBtnSearch);
        progressBar.setVisibility(View.VISIBLE);
        getBooksInfo("Android");

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                // checking if our edittext field is empty or not.
                if (searchEdt.getText().toString().isEmpty()) {
                    searchEdt.setError("Please enter search query");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                // if the search query is not empty then we are
                // calling get book info method to load all
                // the books from the API.
                getBooksInfo(searchEdt.getText().toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getBooksInfo(String query) {
        books = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject currVolume = itemsObj.getJSONObject("volumeInfo");
                        String title = currVolume.optString("title");
                        String subtitle = currVolume.optString("subtitle");
                        JSONArray authorsArray = currVolume.getJSONArray("authors");
                        String publisher = currVolume.optString("publisher");
                        String publishedDate = currVolume.optString("publishedDate");
                        String description = currVolume.optString("description");
                        int pageCount = currVolume.optInt("pageCount");
                        JSONObject imageLinks = currVolume.optJSONObject("imageLinks");
                        String thumbnail = imageLinks.optString("smallThumbnail").replace("http", "https");
                        String previewLink = currVolume.optString("previewLink");
                        String infoLink = currVolume.optString("infoLink");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        String buyLink = saleInfoObj.optString("buyLink");


                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (authorsArray.length() != 0) {
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(i));
                            }
                        }
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        Book bookInfo = new Book(title, subtitle, authorsArray.join(", "), publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink);

                        // below line is use to pass our modal
                        // class in our array list.
                        books.add(bookInfo);

                        // below line is use to pass our
                        // array list in adapter class.
                        BookAdapter adapter = new BookAdapter(books, MainActivity.this);

                        // below line is use to add linear layout
                        // manager for our recycler view.
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
                        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.books_recycler);

                        // in below line we are setting layout manager and
                        // adapter to our recycler view.
                        mRecyclerView.setLayoutManager(linearLayoutManager);
                        mRecyclerView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(MainActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(MainActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

}