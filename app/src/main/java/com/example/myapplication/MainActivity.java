package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.aws_utility.AWSIOT;
import com.example.myapplication.callback.IOTMessageCallbacks;

public class MainActivity extends AppCompatActivity implements IOTMessageCallbacks {
    TextView tvLastMessage;
    TextView tvClientId;
    private AWSIOT mAwsiot = new AWSIOT(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onViewInit();
    }

    protected void onViewInit() {
        tvLastMessage = findViewById(R.id.tvLastMessage);
        tvClientId = findViewById(R.id.tvClientId);
        mAwsiot.initializeAwsConfiguration(MainActivity.this, "aws-iot/raspberrypi");
    }

    @Override
    public void onMessageChangeListiner(String pTopicName, String pMessage) {
        if (pMessage != null && !pMessage.isEmpty()) {
            tvLastMessage.setText(tvLastMessage.getText().toString() + "\n" + pMessage);
        }
    }

    @Override
    public void onFailureListiner(String pError) {
        Toast.makeText(this, pError, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAwsiot.dissconnect();
    }
}
