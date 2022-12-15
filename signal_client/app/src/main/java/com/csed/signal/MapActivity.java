package com.csed.signal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity
            implements OnMapReadyCallback{


    private View mapView;
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private Marker selectedMarker = null;

    private List<Marker> preferredLocations;
    private List<Marker> dislikedLocations;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        getUserData();
        setListeners();
        preferredButtonEnable(false);
        removeButtonEnable(false);
        finishButtonEnable(true);
        preferredLocations = new ArrayList<>();
        dislikedLocations = new ArrayList<>();


    }

    public void setListeners() {
        ((Button)findViewById(R.id.setPreferred)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserDataMarkerOptions(currentMarker.getPosition(), currentMarker.getTitle(), currentMarker.getSnippet(), true);
                currentMarker.remove();
                preferredButtonEnable(false);
                finishButtonEnable(true);
            }
        });
        ((Button)findViewById(R.id.setDislike)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserDataMarkerOptions(currentMarker.getPosition(), currentMarker.getTitle(), currentMarker.getSnippet(), false);
                currentMarker.remove();
                preferredButtonEnable(false);
                finishButtonEnable(true);
            }
        });
        ((Button)findViewById(R.id.removeMarker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferredLocations.remove(selectedMarker)){
                    selectedMarker.remove();
                }
                if (dislikedLocations.remove(selectedMarker)){
                    selectedMarker.remove();
                }

                finishButtonEnable(true);
                removeButtonEnable(false);
            }
        });
        ((Button)findViewById(R.id.finishButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

    }

    public void getUserData(){ // get data from user config
        Call<data_model> call = retrofit_client.getApiService().getData(new data_model("test", new ArrayList<HashMap<String, String>>(), "fast"));
        call.enqueue(new Callback<data_model>() {
            @Override
            public void onResponse(Call<data_model> call, Response<data_model> response) {
                data_model result = response.body();
                ArrayList<HashMap<String, String>> latlng = result.getLatlng();
                userType = result.getType();
                for (int i = 0; i < latlng.size(); i++){
                    Double lat = Double.parseDouble(latlng.get(i).get("lat"));
                    Double lng = Double.parseDouble(latlng.get(i).get("lng"));
                    LatLng ll = new LatLng(lat, lng);

                    boolean isPreferred;
                    if (Objects.equals(latlng.get(i).get("type"), "pref"))
                        isPreferred = true;
                    else if (Objects.equals(latlng.get(i).get("type"), "dis"))
                        isPreferred = false;
                    else
                        continue;

                    setUserDataMarkerOptions(ll, latlng.get(i).get("name"), "위도:" + lat + " 경도:" + lng, isPreferred);

                }

            }

            @Override
            public void onFailure(Call<data_model> call, Throwable t) {
                //Log.d("??", t.toString());
            }
        });
    }

    public void sendUserData(){ // send data to user config
        ArrayList<HashMap<String, String>> locationData = new ArrayList<>();

        int i = 0;
        for (Marker m : preferredLocations){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("type", "pref");
            hm.put("lat", Double.toString(m.getPosition().latitude));
            hm.put("lng", Double.toString(m.getPosition().longitude));
            //hm.put("name", m.getTitle());
            hm.put("name", "Good English Name " + i);
            i++;
            locationData.add(hm);
        }

        for (Marker m : dislikedLocations){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("type", "dis");
            hm.put("lat", Double.toString(m.getPosition().latitude));
            hm.put("lng", Double.toString(m.getPosition().longitude));
            //hm.put("name", m.getTitle());
            hm.put("name", "Good English Name " + i);
            i++;
            locationData.add(hm);
        }

        data_model dm = new data_model("test", locationData, userType);

        Call<data_model> call = retrofit_client.getApiService().setData(dm);
        call.enqueue(new Callback<data_model>() {
            @Override
            public void onResponse(Call<data_model> call, Response<data_model> response) {

            }

            @Override
            public void onFailure(Call<data_model> call, Throwable t) {

            }
        });

    }

    public void setUserDataMarkerOptions(LatLng latlng, String markerTitle, String markerSnippet, boolean isPreferred){

        String header = isPreferred ? "<Preferred> " : "<Dislike>";
        float color = isPreferred ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_BLUE;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(header + markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(false);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));

        Marker m = mMap.addMarker(markerOptions);
        if (isPreferred)
            preferredLocations.add(m);
        else
            dislikedLocations.add(m);

    }

    public void preferredButtonEnable(boolean b){
        int visibility = b ? View.VISIBLE : View.INVISIBLE;
        ((Button) findViewById(R.id.setDislike)).setVisibility(visibility);
        ((Button) findViewById(R.id.setPreferred)).setVisibility(visibility);
    }
    public void removeButtonEnable(boolean b){
        int visibility = b ? View.VISIBLE : View.INVISIBLE;
        ((Button) findViewById(R.id.removeMarker)).setVisibility(visibility);
    }
    public void finishButtonEnable(boolean b){
        int visibility = b ? View.VISIBLE : View.INVISIBLE;
        ((Button) findViewById(R.id.finishButton)).setVisibility(visibility);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);

        mMap.setMyLocationEnabled(true);

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 180, 180, 0);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                LatLng currentPosition
                        = new LatLng( latLng.latitude, latLng.longitude);

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + latLng.latitude + " 경도:" + latLng.longitude;

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(latLng, markerTitle, markerSnippet);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (preferredLocations.contains(marker) || dislikedLocations.contains(marker)){
                    preferredButtonEnable(false);
                    removeButtonEnable(true);
                    finishButtonEnable(false);
                }
                selectedMarker = marker;
                return false;
            }
        });

    }

    public String getCurrentAddress(LatLng latlng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException | IllegalArgumentException e) {
            return "Error Code";
        }

        if (addresses == null || addresses.size() == 0) {
            return "주소 미발견";
        }
        else{
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }
    }

    public void setCurrentLocation(LatLng latlng, String markerTitle, String markerSnippet){

        if (currentMarker != null)
            currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latlng);
        mMap.moveCamera(cameraUpdate);
        preferredButtonEnable(true);
        removeButtonEnable(false);
        finishButtonEnable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendUserData();
    }
}