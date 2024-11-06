package com.example.projectsos;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int REQUEST_CALL_PERMISSION = 2;
    private static final int REQUEST_LOCATION_PERMISSION = 3;
    private String[] phoneNumbers = {"+916282237900", "+917025185532", "+918714733600", "+918589078474", "+919947640096"}; // Replace with your emergency numbers
    private String message = "This is an SOS message. I need help!"; // You can customize the message if needed
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Mobile Data
        enableMobileData(this);

        // Enable Wi-Fi
        enableWifi(this);

        // Enable Location
        enableLocation(this);

        ImageButton sosButton = findViewById(R.id.sos_button);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (sosButton != null) {
            sosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check and request permissions for CALL_PHONE and SEND_SMS
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS},
                                REQUEST_CALL_PERMISSION); // You can use the same request code for both permissions
                    } else {
                        // Permissions granted, perform actions
                        sendSMS();
                        makeRepetitiveCalls();
                        shareLocation();
                    }
                }
            });
        } else {
            // Handle the case where the SOS button is not found in the layout
            String layoutName = getResources().getResourceEntryName(R.layout.activity_main);
            Log.e("MainActivity", "SOS button not found in layout: " + layoutName);
            throw new RuntimeException("SOS button not found. Check your layout file.");
        }

        showPopupWindow();
    }

    private void showPopupWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome!");
        builder.setMessage("This is a popup window displayed when the app opens.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void makePhoneCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    public void sendSMS() {
        getLocationAndSendSMS(); // Get location and send SMS with location
    }

    private void getLocationAndSendSMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Got the location, now send SMS with location
                            sendSMSWithLocation(location);
                        } else {
                            // Handle location retrieval failure
                            Toast.makeText(MainActivity.this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                            // You might want to send SMS without location in this case
                            // sendSMSWithoutLocation(); // Create a separate method for this if needed
                        }
                    }
                });
    }

    private void sendSMSWithLocation(Location location) {
        String locationMessage = "My current location: https://www.google.com/maps/search/?api=1&query=" +
                location.getLatitude() + "," + location.getLongitude();

        SmsManager smsManager = SmsManager.getDefault();
        for (String number : phoneNumbers) {
            smsManager.sendTextMessage(number, null, message + "\n" + locationMessage, null, null); // Append location to message
            Toast.makeText(getApplicationContext(), "SMS with location sent to " + number, Toast.LENGTH_SHORT).show();
        }
    }

    private void makeRepetitiveCalls() {
        for (String number : phoneNumbers) {
            makePhoneCall(number); // Assuming you have a makePhoneCall(String number) method
            // Add a delay between calls if needed (e.g., using a Handler or Thread.sleep())
        }
        // You might want to add a mechanism to stop the repetitive calls (e.g., a button or timer)
    }

    private void shareLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return; // Exit the method if permissions are not granted
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String locationMessage = "My current location: https://www.google.com/maps/search/?api=1&query=" +
                                    location.getLatitude() + "," + location.getLongitude();

                            // Send location via SMS
                            SmsManager smsManager = SmsManager.getDefault();
                            for (String number : phoneNumbers) {
                                smsManager.sendTextMessage(number, null, locationMessage, null, null);
                                Toast.makeText(MainActivity.this, "Location shared via SMS to " + number, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeRepetitiveCalls(); // Call makeRepetitiveCalls() here
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, you can now get the location
                shareLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall() {
        // This method is now empty as makeRepetitiveCalls() handles the calls
    }

    // Method to enable Mobile Data
    private void enableMobileData(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(connectivityManager, true);
            Toast.makeText(context, "Mobile data enabled", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error enabling mobile data: " + e.getMessage());
            // Handle the exception appropriately (e.g., show an error message to the user)
        }
    }

    // Method to enable Wi-Fi
    private void enableWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(context, "Wi-Fi enabled", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to enable Location
    private void enableLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Redirect user to location settings to enable GPS
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error enabling location: " + e.getMessage());
        }
    }
}