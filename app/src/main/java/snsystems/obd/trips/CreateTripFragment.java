package snsystems.obd.trips;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import snsystems.obd.R;
import snsystems.obd.classes.MyApplication;
import snsystems.obd.classes.T;
import snsystems.obd.classes.Validations;
import snsystems.obd.classes.VolleyResponseClass;
import snsystems.obd.dashboard.DashboardActivity;
import snsystems.obd.database.DBHelper;
import snsystems.obd.database.S;
import snsystems.obd.interfaces.Constants;
import snsystems.obd.interfaces.VolleyCallback;


/**
 * Created by shree on 07-Feb-17.
 */
public class CreateTripFragment extends Fragment implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,// for google api client
        GoogleApiClient.OnConnectionFailedListener// for google api client
{


   //@Bind(R.id.sourceAddressEditText)
    EditText sourceAddressEditText;

   //@Bind(R.id.destinationAddressEditText)
    EditText destinationAddressEditText;

    TextInputLayout sourceTextInputLayout,destinatioTextInputLayout;

   // @Bind(R.id.sourceRecyclerView)
    RecyclerView sourceRecyclerView;

   // @Bind(R.id.destinationRecyclerView)
    RecyclerView destinationRecyclerView;

    Button buttonStartTrip,buttonStopTrip;

    protected GoogleApiClient
            mGoogleApiClient;

    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;

    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(-0, 0), new LatLng(0, 0));

    private String sourceLatLong,destinationLatLong;

    private SharedPreferences preferences;

   // private TextView textViewTripstatus;

    public CreateTripFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.create_trip_fragment, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        buildGoogleApiClient();
       // ButterKnife.bind(getActivity());
        //lat/lng: (18.5018322,73.8635912)
        initialize();
        setListner();

        setDefaultTripPreference();

        checkLocationServices();

       // startAnimation();


        return view;
    }

    private void startAnimation()
    {

//        Animation anim = new AlphaAnimation(0.0f, 1.0f);
//        anim.setDuration(100); //You can manage the blinking time with this parameter
//        anim.setStartOffset(70);
//        anim.setRepeatMode(Animation.REVERSE);
//        anim.setRepeatCount(Animation.INFINITE);
//        textViewTripstatus.startAnimation(anim);
    }

    private void setDefaultTripPreference()
    {
        try
        {
            preferences = getActivity().getSharedPreferences(Constants.TRIP_PREFERENCE, 0);
            SharedPreferences.Editor editor = preferences.edit();
            //editor.putString("status", "stop");
            editor.commit();


            String statuss = preferences.getString("status","");

            if(statuss.equals("stop"))
            {
                buttonStartTrip.setEnabled(true);
                buttonStopTrip.setEnabled(false);
            }
            else if(statuss.equals("start"))
            {
                buttonStartTrip.setEnabled(false);
                buttonStopTrip.setEnabled(true);
            }
            else if(statuss.equals(null))
            {
                buttonStartTrip.setEnabled(true);
                buttonStopTrip.setEnabled(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

//
//
//
//        String sourceLatLong = preferences.getString(Constants.SOURCE_LATLONG, "");
//        String destinationLatLong = preferences.getString(Constants.DESTINATION_LATLONG, "");
//        String sourceAddress = preferences.getString(Constants.SOURCE_ADDRESS, "");
//        String destinationAddress = preferences.getString(Constants.DESTINATION_ADDRESS, "");
//        String sourceDatTime = preferences.getString(Constants.SOURCE_DATE_TIME, "");
//        String destinationDatTime = preferences.getString(Constants.DESTINATION_DATE_TIME, "");
//
//        Log.e("TRIPS_START", "" + sourceLatLong);
//        Log.e("TRIPS_START",""+destinationLatLong);
//        Log.e("TRIPS_START",""+sourceAddress);
//        Log.e("TRIPS_START", "" + destinationAddress);
//        Log.e("TRIPS_START", "" + sourceDatTime);
//        Log.e("TRIPS_START", "" + destinationDatTime);
    }

    private void checkLocationServices()
    {
        try
        {
            if ( T.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) )
            {
                ArrayList<Boolean> gpsNetwork = T.checkLocationServices(getActivity());

                if(!gpsNetwork.get(0) && !gpsNetwork.get(1))
                {

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setConfirmText("Enable")
                            .setCancelText("Cancel")
                            .setContentText("Location Off...")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();

                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(myIntent);

                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {

                                    sweetAlertDialog.dismissWithAnimation();

                                }
                            })
                            .show();
                }
                else
                {

                }
            }
            else T.askPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);

        }
        catch (Exception ex)
        {
            //Toast.makeText(getApplicationContext(),ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }


    }

    //build google api client
    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }
    private void initialize()
    {

        sourceAddressEditText = (EditText)view.findViewById(R.id.sourceAddressEditText);
        destinationAddressEditText = (EditText)view.findViewById(R.id.destinationAddressEditText);

        sourceTextInputLayout  = (TextInputLayout)view.findViewById(R.id.sourceTextInputLayout);
        destinatioTextInputLayout = (TextInputLayout)view.findViewById(R.id.destinatioTextInputLayout);

        sourceRecyclerView  = (RecyclerView)view.findViewById(R.id.sourceRecyclerView);
        destinationRecyclerView  = (RecyclerView)view.findViewById(R.id.destinationRecyclerView);

        buttonStartTrip = (Button)view.findViewById(R.id.buttonStartTrip);
        buttonStopTrip = (Button)view.findViewById(R.id.buttonStopTrip);
       // textViewTripstatus = (TextView)view.findViewById(R.id.textViewTripstatus);


        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getActivity(), R.layout.searchview_adapter, mGoogleApiClient, BOUNDS_INDIA, null);


        sourceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        destinationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sourceRecyclerView.setAdapter(mAutoCompleteAdapter);
        destinationRecyclerView.setAdapter(mAutoCompleteAdapter);

    }

    private void setListner()
    {
        //stop trip here
        buttonStopTrip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {


                String device_id_get = S.getDeviceId(MyApplication.db);

                String dateTime = T.getSystemDateTime();

                String [] parameters = {

                        "device_id"+"#"+device_id_get,
                        "destination_datetime" + "#" + dateTime,
                        "trip_start_status" + "#" + "0",
                        "trip_end_status" + "#" + "1"

                };


                VolleyResponseClass.getResponseProgressDialog(
                        new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                //Log.e("VolleyResponse", "" + result);

                                parseStopResponse(result);

                            }
                        },
                        getActivity(),
                        getResources().getString(R.string.webUrl)+""+getResources().getString(R.string.stopTrip),
                        destinationAddressEditText,
                        parameters,
                        "Stoping Trips...");


            }
        });

        buttonStartTrip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(!Validations.validateEmptyField(sourceAddressEditText, "Enter source address", sourceTextInputLayout))
                {
                    return;
                }
                if(!Validations.validateEmptyField(destinationAddressEditText,"Enter destination address",destinatioTextInputLayout))
                {
                    return;
                }

                //startTrip();
                boolean c = T.checkConnection(getActivity());
                if(c)
                {

                    startTrips();
                }
                else
                {
                    T.t(getActivity(),"Network Connection Off");
//                    notificationHideRelativeLayout.setVisibility(View.VISIBLE);
//                    imgHideLayout.setImageResource(R.drawable.ic_cloud_off_black_48dp);
//                    textViewHideLayout.setText("Network connection off");
                }


                //startTrip();
            }
        });


        sourceAddressEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sourceRecyclerView.setVisibility(View.VISIBLE);
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getActivity(), "api not connected", Toast.LENGTH_SHORT).show();

                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
        destinationAddressEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                destinationRecyclerView.setVisibility(View.VISIBLE);
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getActivity(), "api not connected", Toast.LENGTH_SHORT).show();

                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
        sourceRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        Log.i("TAG", "Autocomplete item selected: " + item.description);
                        sourceAddressEditText.setText(item.description);
                        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getCount() == 1) {
                                    //Do the things here on Click.....
                                    //  Toast.makeText(getActivity(), String.valueOf(places.get(0).getLatLng()), Toast.LENGTH_SHORT).show();

                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    sourceLatLong = queriedLocation.latitude + "," + queriedLocation.longitude;
                                    Log.e("LATLOONG",""+sourceLatLong);
                                    T.t(getActivity(),""+sourceLatLong);


                                } else {
                                    Toast.makeText(getActivity(), "SOMETHING_WENT_WRONG", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Log.i("TAG", "Clicked: " + item.description);
                        Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);

                        sourceRecyclerView.setVisibility(View.GONE);
                    }
                })
        );

        destinationRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        Log.i("TAG", "Autocomplete item selected: " + item.description);
                        destinationAddressEditText.setText(item.description);
                        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */
                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getCount() == 1) {
                                    //Do the things here on Click.....
                                    //Toast.makeText(getActivity(), String.valueOf(places.get(0).getLatLng()), Toast.LENGTH_SHORT).show();
                                    //destinationLatLong = String.valueOf(places.get(0).getLatLng());
                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    destinationLatLong = queriedLocation.latitude + "," + queriedLocation.longitude;

                                    Log.e("LATLOONG",""+destinationLatLong);
                                    T.t(getActivity(),""+destinationLatLong);

                                } else {
                                    Toast.makeText(getActivity(), "SOMETHING_WENT_WRONG", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        //Log.i("TAG", "Clicked: " + item.description);
                        //Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);

                        destinationRecyclerView.setVisibility(View.GONE);
                    }
                })
        );
    }
    private void startTrips() {


        String device_id_get = S.getDeviceId(MyApplication.db);

        String [] sourecLatLongData = sourceLatLong.split(",");
        String [] destinationLatLongData = destinationLatLong.split(",");

        String dateTime = T.getSystemDateTime();

        String [] parameters = {

                "device_id"+"#"+device_id_get,
                "source_address" + "#" + sourceAddressEditText.getText().toString(),
                "destination_address" + "#" + destinationAddressEditText.getText().toString(),
                "source_lattitude"+"#"+sourecLatLongData[0],
                "source_longitude" + "#" + sourecLatLongData[1],
                "destination_lattitude" + "#" + destinationLatLongData[0],
                "destination_longitude" + "#" + destinationLatLongData[1],
                "source_detetime" + "#" + dateTime,
                "destination_datetime" + "#" + dateTime,
                "trip_start_status" + "#" + "1",
                "trip_end_status" + "#" + "0"

        };

//        for(int i = 0; i < parameters.length; i++)
//        {
//            Log.e("AMOL",""+parameters[i]);
//        }




        VolleyResponseClass.getResponseProgressDialog(
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e("VolleyResponse", "" + result);

                        parseResponse(result);

                    }
                },
                getActivity(),
                getResources().getString(R.string.webUrl)+""+getResources().getString(R.string.startTrip),
                destinationAddressEditText,
                parameters,
                "Starting Trips...");




    }
    private void parseStopResponse(String result) {

        try
        {
            if(result != null || result.length() > 0)
            {
                Object json = new JSONTokener(result).nextValue();
                if (json instanceof JSONObject)
                {
                    JSONObject tripsJsonObject = new JSONObject(result);
                    String status = tripsJsonObject.getString("status");

                    if(status.equals("1"))
                    {
                        // preferences = getActivity().getSharedPreferences(Constants.TRIP_PREFERENCE, 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("status", "stop");
                        editor.commit();

                        buttonStartTrip.setEnabled(true);
                        buttonStopTrip.setEnabled(false);

                        T.displaySuccessMessage(getActivity(),"Success", "OK", "Trip successfully stop");
                    }
                    else if(status.equals("0"))
                    {
                        T.displayErrorMessage(getActivity(), "Fail", "OK", "fail to stop trip");
                    }
                }
                else
                {
                    T.t(getActivity(),"incorect json");
                }
            }
            else
            {
                T.t(getActivity(),"0 or null json");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

        }
    }

    private void parseResponse(String result) {

        try
        {
            if(result != null || result.length() > 0)
            {
                Object json = new JSONTokener(result).nextValue();
                if (json instanceof JSONObject)
                {
                    JSONObject tripsJsonObject = new JSONObject(result);
                    String status = tripsJsonObject.getString("status");

                    if(status.equals("1"))
                    {
                        // preferences = getActivity().getSharedPreferences(Constants.TRIP_PREFERENCE, 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("status", "start");
                        editor.commit();

                        buttonStartTrip.setEnabled(false);
                        buttonStopTrip.setEnabled(true);

                        T.displaySuccessMessage(getActivity(),"Success", "OK", "Trip successfully start");
                    }
                    else if(status.equals("0"))
                    {
                        T.displayErrorMessage(getActivity(), "Fail", "OK", "Problem to start trip");
                    }
                }
                else
                {
                    T.t(getActivity(),"incorect json");
                }
            }
            else
            {
                T.t(getActivity(),"0 or null json");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

        }
    }


    private void startTrip()
    {


    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            Log.v("Google API", "Connecting");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            Log.v("Google API", "Dis-Connecting");
            mGoogleApiClient.disconnect();
        }
    }

        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }


}
