package de.ludetis.android.mqttpahomessenger;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;


public class MainActivity extends Activity implements View.OnClickListener, MqttCallback {


    private static final String MQTT_URI = "tcp://h8.ludetis-spiele.de:1883";
    private static final String CLIENT_NAME = Build.DEVICE;
    private static final String MQTT_TOPIC = "mqttpahomessenger";
    private static final int QOS = 2;
    private MqttAndroidClient client;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.message);

        findViewById(R.id.send).setOnClickListener(this);
    }


    private void connect() {
        MqttDefaultFilePersistence mdfp = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());
        client = new MqttAndroidClient(this, MQTT_URI, CLIENT_NAME, mdfp);
        client.setCallback(this);

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // makes sure subscriptions are cleared. Otherwise we could have duplicates.
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            client.connect(options, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(MainActivity.this, "connected to MQTT broker", Toast.LENGTH_SHORT).show();
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(MainActivity.this, "failed to connect: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Toast.makeText(this, "could not connect to MQTT broker at " + MQTT_URI, Toast.LENGTH_SHORT).show();
        }
    }

    private void subscribe() {
        try {
            IMqttToken token = client.subscribe(MQTT_TOPIC, QOS, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(MainActivity.this, "subscription successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(MainActivity.this, "subscription failed: " + throwable, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (MqttException e) {
            Toast.makeText(this, "could not subscribe", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (client == null || !client.isConnected()) {
            connect();
        }
    }


//    @Override
//    protected void onDestroy() {
//        try {
//            client.disconnect();
//        } catch (MqttException e) {
//            Toast.makeText(this, "could not disconnect from MQTT broker at " + MQTT_URI, Toast.LENGTH_SHORT).show();
//        }
//        super.onDestroy();
//    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send) {
            send("<b>" + CLIENT_NAME + "</b>: " + editText.getText().toString() + "<br/>");
            editText.setText("");
        }
    }

    private void send(String text) {
        if (client == null || !client.isConnected()) {
            Toast.makeText(this, "sorry, currently not connected...", Toast.LENGTH_SHORT).show();
            return;
        }
        MqttMessage message = new MqttMessage(text.getBytes());
        message.setQos(QOS);
        try {
            client.publish(MQTT_TOPIC, message, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(MainActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(MainActivity.this, "failed to send message: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Toast.makeText(this, "could not send message to MQTT broker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Toast.makeText(this, "connection lost", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        Toast.makeText(this, "message received on topic " + s, Toast.LENGTH_SHORT).show();
        TextView tv = (TextView) findViewById(R.id.chat);
        String text = (String) tv.getTag();
        if (text == null) text = "";
        text += mqttMessage.toString();
        tv.setTag(text);
        tv.setText(Html.fromHtml(text));

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Toast.makeText(this, "delivery complete", Toast.LENGTH_SHORT).show();
    }
}
