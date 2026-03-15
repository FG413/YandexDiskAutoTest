package org.example.yandexdiskautotest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Arrays;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.RequestEntity.BodyBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class YandexDiskAutoTestApplication
{
    public static HttpClient  client = HttpClient.newHttpClient();

    public static HttpRequest.Builder builder = HttpRequest.newBuilder()
            .header("Authorization", "y0__xDhxNe1AhiH9T4g4qOZ3hYw1tea2wiaSQDAbPs3YJWWjJfHDQkcPmLHjQ");

    public static void main(String[] args) throws IOException, InterruptedException
    {

        getContent();


    }
    public static void getContent() throws IOException, InterruptedException
    {
        HttpRequest request = builder
                .uri(URI.create("https://downloader.disk.yandex.ru/disk/f3b713d0830057021228d6be1f0261b95f2dbadfaa6de2724c7556a55d247fcd/69b72c57/rYI433GkZGBiVwy4hvz-f5NMYxfBo6u6dHo3jY7xngkz-PZG0z9PM0OF7pjITQTi1ZIe3OgI4ogXzHf7i_u0dg%3D%3D?uid=649454177&filename=moxe.png&disposition=inline&hash=&limit=0&content_type=image%2Fpng&owner_uid=649454177&fsize=366730&hid=67446186309c39697f86a5842aba8e54&media_type=image&tknv=v3&etag=1e7a24f137b42496b8bcf68a47dd8694"))
                .GET() // Default method, but explicitly set here

                .build();

        // 3. Send the request and receive the response synchronously
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response.body());

        // 4. Process the response
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.headers().map().get("location").get(0));
        HttpRequest request1 = builder.uri(URI.create(response.headers().map().get("location").get(0))).GET().build();
        HttpResponse<byte[]> response1 = client.send(request1, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("asda"+response1.statusCode());
        File convFile = new File("src/main/resources/static/img.png");
              var x =  Files.readAllBytes(convFile.toPath());
        System.out.println(Arrays.equals(x, response1.body()));

    }

    public static void getAllContent() throws IOException, InterruptedException
    {
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk"))
                .GET() // Default method, but explicitly set here
                .build();

        // 3. Send the request and receive the response synchronously
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response.body());
        //src/main/resources/static/img.png
        File convFile = new File("src/main/resources/static/img.png");

        System.out.println(convFile.getAbsolutePath());
        // 4. Process the response
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + jsonNode);
    }

    public static void postContent() throws IOException, InterruptedException
    {

        File convFile = new File("src/main/resources/static/img.png");
        HttpRequest request = builder
                .uri(URI.create("https://uploader358klg.disk.yandex.net:443/upload-target/20260315T210010.682.utd.ahy55rcvtyv7fl4c4ua3zpeh9-k358klg.4096598"))
                .PUT(HttpRequest.BodyPublishers.ofFile(convFile.toPath())) // Default method, but explicitly set here
                .build();

        // 3. Send the request and receive the response synchronously
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();


        // 4. Process the response
        System.out.println("Status Code: " + response.statusCode());
    }

}
