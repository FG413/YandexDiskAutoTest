package org.example.yandexdiskautotest;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class YandexDiskAutoTestApplication
{

    public static HttpClient  client = HttpClient.newHttpClient();



    static String token = "y0__xDhxNe1AhiH9T4g4qOZ3hYw1tea2wiaSQDAbPs3YJWWjJfHDQkcPmLHjQ";



    public static HttpRequest.Builder builder = HttpRequest.newBuilder()
            .header("Authorization", token);


}
