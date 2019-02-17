package com.example.myapplication.aws_utility;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.example.myapplication.callback.IOTMessageCallbacks;
import com.example.myapplication.constants.Constants;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class AWSIOT {
    protected String LOG_TAG = AWSIOT.class.getCanonicalName();
    protected AWSIotClient mIotAndroidClient;
    protected AWSIotMqttManager mqttManager;
    protected KeyStore clientKeyStore = null;
    IOTMessageCallbacks mIOIotMessageCallbacks;

    public AWSIOT(IOTMessageCallbacks pIOIotMessageCallbacks) {
        mIOIotMessageCallbacks = pIOIotMessageCallbacks;
    }
    /*
     *
     * Just
     * @param - pContext - for context
     * @param - ptopicName- for Topic to subscribe
     * */
    public void initializeAwsConfiguration(Context pContext, final String pTopicName) {
        AWSMobileClient.getInstance().initialize(pContext, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                initIoTClient(pTopicName);
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "onError: ", e);
            }
        });
    }

    private void initIoTClient(final String pTopic) {
        Region region = Region.getRegion(Constants.MY_REGION);
        // MQTT Client
        mqttManager = new AWSIotMqttManager(Constants.clientId, Constants.CUSTOMER_SPECIFIC_ENDPOINT);
        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);
        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament(
                pTopic,
                "Android client lost connection",
                AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);
        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
        mIotAndroidClient.setRegion(region);
        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(Constants.keystorePath, Constants.KEYSTORE_NAME)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(Constants.CERTIFICATE_ID,
                        Constants.keystorePath,
                        Constants.KEYSTORE_NAME,
                        Constants.KEYSTORE_PASSWORD)) {
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(
                            Constants.CERTIFICATE_ID,
                            Constants.keystorePath,
                            Constants.KEYSTORE_NAME,
                            Constants.KEYSTORE_PASSWORD);
                    /* initIoTClient is invoked from the callback passed during AWSMobileClient initialization.
                    The callback is executed on a background thread so UI update must be moved to run on UI Thread. */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connect(pTopic);
                        }
                    });
                } else {
                    Log.i(LOG_TAG, "Key/cert " + Constants.CERTIFICATE_ID + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + Constants.keystorePath + "/" + Constants.KEYSTORE_NAME + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
            mIOIotMessageCallbacks.onFailureListiner(e.getMessage());
        }
    }

    public void connect(final String pTopic) {
        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (throwable == null) {
                                subscribeClick(pTopic);
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            mIOIotMessageCallbacks.onFailureListiner(e.getMessage());
        }
    }

    public void subscribeClick(String ptopic) {
        try {
            mqttManager.subscribeToTopic(ptopic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        mIOIotMessageCallbacks.onMessageChangeListiner(topic, message);
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                        mIOIotMessageCallbacks.onFailureListiner(e.getMessage());
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    public void publishClick(final String topic, final String msg, final View view) {
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
            mIOIotMessageCallbacks.onFailureListiner(e.getMessage());
        }
    }

    public void dissconnect() {
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }

}