package midnightrush.waterlevel;

import android.app.Activity;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.adc.ads1xxx.Ads1xxx;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DELAY_MS = 1000; // 2 samples/second

    private Ads1xxx adcDriver;
    private Handler handler;
    private HandlerThread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "Initializing ADC Driver");
            adcDriver = new Ads1xxx(BoardDefaults.getI2CPort(), Ads1xxx.Configuration.ADS1015);  //, 0x48
            // Increase default range to fit +3.3V
            adcDriver.setInputRange(Ads1xxx.RANGE_4_096V);


            // Set up I/O polling thread
            readThread = new HandlerThread("ADC Reader");
            readThread.start();
            handler = new Handler(readThread.getLooper());
            handler.post(readAction);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize ADC driver", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        readThread.quit();

        try {
            adcDriver.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close ADC driver", e);
        } finally {
            adcDriver = null;
        }
    }

    /* Read a single analog sample and log the result */
    private Runnable readAction = new Runnable() {
        @Override
        public void run() {
            try {
                // Read differential between IN0+/IN1-
                final int waterLevel = adcDriver.readDifferentialInput(Ads1xxx.INPUT_DIFF_0P_1N);

                Log.i(TAG, "Current water level: " + waterLevel);

                if (waterLevel < 0)
                    // Negative values mean that there is no water
                    createRequest(0);
                else
                    createRequest(waterLevel);

            } catch (IOException e) {
                Log.e(TAG, "Unable to read analog sample!", e);
            }

            handler.postDelayed(this, DELAY_MS);
        }
    };

    private void createRequest(int value){
        try
        {
            URL url = new URL("http://192.168.43.201:2029/addData");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());

            String valueJSON = "value=" + value;
            out.write(valueJSON.getBytes());

            out.flush();
            out.close();

            int responseCode = con.getResponseCode();
            //Log.d(TAG, "Response Code: " + String.valueOf(responseCode));

        } catch (Exception ex) {
            Log.e(TAG, "Can't do rest call!", ex);
        }
    }
}

