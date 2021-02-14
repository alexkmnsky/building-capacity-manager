package com.example.storecounter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    public String message;
    private reader r;

    /** Runs on MainActivity creation, initializes the connection thread */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = new reader();
        r.start();
    }

    /** Runs near startup, creates our action bar menu with the settings icon, etc.*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actions, menu);
        return true;
    }

    /** Called when reset button is clicked. Presents warning before proceeding. */
    public void onResetClick(View view) {
        // Create a new AlertDialog builder and set the title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to reset the count?");

        // Create proceed button, send command !RESET to sensor when clicked
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                send sendCode = new send();
                message = "!RESET";
                sendCode.execute();
            }
        });

        // Create cancel button, close the AlertDialog when clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Complete AlertDialog building, show to user
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Called when set current count button is clicked. Presents dialog with number field. */
    public void onSetCountClick(View view) {
        // Create a new AlertDialog builder and set the title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Current Count");

        // Retrieve set_number_dialog view which contains the number field
        final View layout = getLayoutInflater().inflate(R.layout.set_number_dialog, null);
        // Add the view to the dialog
        builder.setView(layout);


        // Create set button, send command !SET [number] to sensor when clicked
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = layout.findViewById(R.id.editTextNumber);
                send sendCode = new send();
                message = "!SET " + editText.getText().toString();
                sendCode.execute();
            }
        });

        // Create cancel button, close the AlertDialog when clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Complete AlertDialog building, show to user
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Called when set maximum capacity button is clicked. Presents dialog with number field. */
    public void onSetCapacityClick(View view) {
        // Create a new AlertDialog builder and set the title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Maximum Capacity");

        // Retrieve set_number_dialog view which contains the number field
        final View layout = getLayoutInflater().inflate(R.layout.set_number_dialog, null);
        builder.setView(layout);

        // Create set button, send command !MAXIMUM [number] to sensor when clicked
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = layout.findViewById(R.id.editTextNumber);
                send sendCode = new send();
                message = "!MAXIMUM " + editText.getText().toString();
                sendCode.execute();
            }
        });

        // Create cancel button, close the AlertDialog when clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Complete AlertDialog building, show to user
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Called when the entrance menu selection is clicked.
     * Sends the command !INCREMENT to the sensor.
     */
    public void onIncrementClick(MenuItem item) {
        send sendCode = new send();
        message = "!INCREMENT";
        sendCode.execute();
    }

    /**
     * Called when the exit menu selection is clicked.
     * Sends the command !DECREMENT to the sensor.
     */
    public void onDecrementClick(MenuItem item) {
        send sendCode = new send();
        message = "!DECREMENT";
        sendCode.execute();
    }

    /** Called when the settings action button is clicked */
    public void onSettingsClick(MenuItem item) {
        // Start the SettingsActivity activity
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /**
     * Class with functionality for asynchronously sending messages to the sensor.
     * When called, the task will use the variable message to determine the output sent.
     */
    class send extends AsyncTask<Void, Void, Void> {
        Socket s;
        PrintWriter pw;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Retrieve user preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                // Get IP and port for connection
                String ip = preferences.getString("ip_address", "");
                String portSetting = preferences.getString("port", "");

                // Attempt to parse port, if fails, defaults to 5050
                int port = 5050;
                try {
                    port = Integer.parseInt(portSetting);
                } catch (NumberFormatException e) {}

                // Create a new socket, connect to address with a timeout of 2 seconds
                s = new Socket();
                s.connect(new InetSocketAddress(ip, port), 2000);

                // Create a PrintWriter for sending output to the sensor server
                pw = new PrintWriter(s.getOutputStream());

                // Build a header 64 characters long, containing the size of the message
                StringBuilder header = new StringBuilder(Integer.toString(message.length()));
                int headerLength = header.length();
                for (int i = 0; i < 64 - headerLength; i++) {
                    header.append(" ");
                }

                // Write the header followed by the message
                pw.write(header.toString());
                pw.write(message);

                // Flush to ensure the message is sent
                pw.flush();

                // Cleanup
                pw.close();
                s.close();

            } catch (UnknownHostException e) {
                Log.e("StoreCounter", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("StoreCounter", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

    }

    /**
     * Thread which refreshes information displayed to the user based on sensor/server broadcasts.
     * Keeps track of connection status and lets the user know if they are disconnected.
     * Will attempt reconnection in the case that the sensor or the user lose connection.
     */
    class reader extends Thread {
        Socket s;
        BufferedReader in;
        boolean shouldRun = true;
        boolean connected = false;

        public void run() {

            while (shouldRun) {

                // Update user interface information, lets the user know if they are disconnected
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView disconnectedText = (TextView)findViewById(R.id.disconnectedText);
                        ProgressBar disconnectedSpinner = (ProgressBar)findViewById(R.id.disconnectedSpinner);
                        if (connected) {
                            disconnectedText.setVisibility(View.INVISIBLE);
                            disconnectedSpinner.setVisibility(View.INVISIBLE);
                        } else {
                            disconnectedText.setVisibility(View.VISIBLE);
                            disconnectedSpinner.setVisibility(View.VISIBLE);
                            TextView numberOfPeopleText = (TextView)findViewById(R.id.numberOfPeopleText);
                            TextView maximumCapacityText = (TextView)findViewById(R.id.maximumCapacityText);
                            numberOfPeopleText.setText("?");
                            maximumCapacityText.setText("Maximum Capacity: ?");
                        }
                    }
                });

                // Try to reconnect if the user is currently disconnected
                if (!connected) {
                    try {
                        // Sleep for 1 second in order to avoid spamming requests
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Retrieve user preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                    // Get IP and port for connection
                    String ip = preferences.getString("ip_address", "");
                    String portSetting = preferences.getString("port", "");

                    // Attempt to parse port, abort connection if fails
                    int port;
                    try {
                        port = Integer.parseInt(portSetting);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    try {
                        // Create a new socket
                        s = new Socket();
                        Log.d("StoreCounter", "Attempting Connection...");

                        // Connect to address with a timeout of 2 seconds
                        s.connect(new InetSocketAddress(ip, port), 2000);

                        // Set I/O timeout to 5 seconds
                        s.setSoTimeout(5000);

                        // Create new BufferedReader to store messages sent by the sensor
                        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        connected = true;

                    } catch (UnknownHostException e) {
                        try {
                            // Cleanup if disconnected
                            s.close();
                        } catch (IOException ioException) {
                            Log.e("StoreCounter", "IOException on close");
                        }
                        Log.e("StoreCounter", "UnknownHostException on connection attempt");
                        continue;
                    } catch (IOException e) {
                        try {
                            // Cleanup if disconnected
                            s.close();
                        } catch (IOException ioException) {
                            Log.e("StoreCounter", "IOException on close");
                        }
                        Log.e("StoreCounter", "IOException on connection attempt");
                        continue;
                    }
                }

                // Parse server messages
                try {
                    // Create a buffer with a length of 64 characters and read from the stream
                    char headerBuffer[] = new char[64];
                    int headerCharsIn = in.read(headerBuffer, 0, 64);

                    // Convert the char array into a string
                    StringBuilder header = new StringBuilder(headerCharsIn);
                    header.append(headerBuffer, 0, headerCharsIn);

                    // Parse the size passed by the header
                    int size = Integer.parseInt(header.toString().trim());

                    // Based on the size of the header, create a buffer with the correct length,
                    // read the message from the stream
                    char messageBuffer[] = new char[size];
                    int messageCharsIn = in.read(messageBuffer, 0, size);

                    // Convert the char array into a string
                    StringBuilder message = new StringBuilder(messageCharsIn);
                    message.append(messageBuffer, 0, messageCharsIn);

                    // Parse message to retrieve the number of people in the store and the current
                    // maximum capacity
                    String numberOfPeople = message.toString().substring(0, message.indexOf(" "));
                    String maximumCapacity = message.toString().substring(message.indexOf(" ")+1, message.length());

                    // Find UI elements which contain the number of people/maximum capacity
                    TextView numberOfPeopleText = (TextView)findViewById(R.id.numberOfPeopleText);
                    TextView maximumCapacityText = (TextView)findViewById(R.id.maximumCapacityText);

                    // Replace old values with new ones
                    numberOfPeopleText.setText(numberOfPeople);
                    maximumCapacityText.setText("Maximum Capacity: " + maximumCapacity);
                } catch (IOException e) {
                    connected = false;
                    try {
                        // Cleanup if disconnected
                        s.close();
                    } catch (IOException ioException) {
                        Log.e("StoreCounter", "IOException on close");
                    }
                    Log.e("StoreCounter", "IOException on read");
                    continue;
                }
            }
            try {
                // Cleanup if thread should stop running
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}