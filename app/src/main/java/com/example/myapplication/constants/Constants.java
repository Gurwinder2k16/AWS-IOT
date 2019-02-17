package com.example.myapplication.constants;

import com.amazonaws.regions.Regions;

public class Constants {
    // --- Constants to modify per your configuration ---
    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    public static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2byvpqwxqghn3-ats.iot.eu-west-2.amazonaws.com";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "raspberry_pi_access";
    // Region of AWS IoT
    public static final Regions MY_REGION = Regions.EU_WEST_2;
    // Filename of KeyStore file on the filesystem
    public static final String KEYSTORE_NAME = "iot_keystore.bks";
    // Password for the private key in the KeyStore
    public static final String KEYSTORE_PASSWORD = "Asdfghjkl@123";
    // Certificate and key aliases in the KeyStore
    public static final String CERTIFICATE_ID = "iot";
    public static final String clientId = "raspberrypi_test_client";
    public static final String keystorePath = "/storage/emulated/0/";
}
