package com.example.myapplication.callback;

public interface IOTMessageCallbacks {
     void onMessageChangeListiner(String pTopicName, String pMessage);

     void onFailureListiner(String pError);
}
