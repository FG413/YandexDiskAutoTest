package org.example.yandexdiskautotest;

import static org.example.yandexdiskautotest.YandexDiskAutoTestApplication.builder;
import static org.example.yandexdiskautotest.YandexDiskAutoTestApplication.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YandexDiskAutoTestApplicationTests
{
    @DisplayName("Тест получения списка файлов")
    @Test
    @Order(1)
    void getAllContent()
    {
        //тестируем запрос
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources/files"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);

        Assertions.assertEquals(200, response.statusCode());

    }


    @DisplayName("Тест получения списка файлов упорядоченных по дате загрузки")
    @Test
    @Order(2)
    void getAllContentSortedByTime()
    {
        //тестируем запрос
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources/last-uploaded"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);

        Assertions.assertEquals(200, response.statusCode());

        JSONArray items = new JSONObject(response.body()).getJSONArray("items");

        ArrayList<Date> dates = new ArrayList<>();
        for (int i = 0; i < items.length(); i++)
        {
            dates.add(Date.from(Instant.parse(items.getJSONObject(i).getString("created"))));

        }
        boolean isSorted = IntStream.range(0, dates.size() - 1)
                .allMatch(i -> !dates.get(i).before(dates.get(i + 1)));
        Assertions.assertTrue(isSorted, "проверка отсортированности по дате");

    }

    @DisplayName("Тест получения метаинформации о диске")
    @Test
    @Order(3)
    void getContentInfo()
    {
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);

        Assertions.assertEquals(200, response.statusCode());

    }

    @DisplayName("Тест получения файла по пути")
    @Test
    @Order(4)
    void getContentByPath()
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources/last-uploaded"))
                .GET()
                .build();

        HttpResponse<String> allContentResponse = getResponseString(client, allContentRequest);

        Assertions.assertEquals(200, allContentResponse.statusCode());
        String path = new JSONObject(allContentResponse.body()).getJSONArray("items")
                .getJSONObject(0)
                .getString("path");
        path = URLEncoder.encode(path, StandardCharsets.UTF_8);
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=" + path))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        Assertions.assertEquals(200, response.statusCode());
    }

    @DisplayName("Тест создания папки")
    @Test
    @Order(5)
    void createFolder()
    {
        HttpRequest createFolderRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolder"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> putResponse = getResponseString(client, createFolderRequest);

        Assertions.assertEquals(201, putResponse.statusCode());

        String path = new JSONObject(putResponse.body()).getString("href");
        HttpRequest request = builder
                .uri(URI.create(path))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        Assertions.assertEquals(200, response.statusCode());
    }

    @DisplayName("Тест копирования папки")
    @Test
    @Order(6)
    void copyContent()
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create(
                        "https://cloud-api.yandex.net/v1/disk/resources/copy?from=disk:/testFolder&&path=disk"
                                + ":/testFolderCopy"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> postResponse = getResponseString(client, allContentRequest);


        Assertions.assertTrue(postResponse.statusCode() == 201 || postResponse.statusCode() == 202);

        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolderCopy"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        Assertions.assertEquals(200, response.statusCode());
    }

    @DisplayName("Тест перемещения папки")
    @Test
    @Order(7)
    void moveContent()
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create(
                        "https://cloud-api.yandex.net/v1/disk/resources/move?from=disk:/testFolder&&path=disk"
                                + ":/testFolderMoved"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> postResponse = getResponseString(client, allContentRequest);


        Assertions.assertTrue(postResponse.statusCode() == 201 || postResponse.statusCode() == 202);

        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolderMoved"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        Assertions.assertEquals(200, response.statusCode());

        HttpRequest deleteRequest1 = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolderMoved"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse1 = getResponseString(client, deleteRequest1);
        HttpRequest deleteRequest2 = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolderCopy"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse2 = getResponseString(client, deleteRequest2);
        HttpRequest deleteRequest3 = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/testFolder"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse3 = getResponseString(client, deleteRequest3);

    }

    @DisplayName("Тест загрузки файла по ссылке")
    @Test
    @Order(8)
    void uploadFileByUrl()
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create(
                        "https://cloud-api.yandex.net/v1/disk/resources/upload?path=disk:/orange.jpg&&url=https://www"
                                + ".buyfreshonline.co.uk/wp-content/uploads/2024/12/Fresh-Orange.webp"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> postResponse = getResponseString(client, allContentRequest);


        Assertions.assertEquals(202, postResponse.statusCode());

        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/orange.jpg"))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        Assertions.assertEquals(200, response.statusCode());
    }

    @DisplayName("Тест загрузки файла")
    @Test
    @Order(9)
    void uploadFile() throws FileNotFoundException
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources/upload?path=disk:/newUpload"))
                .GET()
                .build();

        HttpResponse<String> getResponse = getResponseString(client, allContentRequest);

        String path = new JSONObject(getResponse.body()).getString("href");

        Assertions.assertEquals(200, getResponse.statusCode());
        File convFile = new File("src/main/resources/static/img.png");
        HttpRequest request = builder
                .uri(URI.create(path))
                .PUT(HttpRequest.BodyPublishers.ofFile(convFile.toPath()))
                .build();

        HttpResponse<String> response = getResponseString(client, request);


        HttpRequest getRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/newUpload"))
                .GET()
                .build();

        HttpResponse<String> getContentResponse = getResponseString(client, getRequest);


        Assertions.assertEquals(200, getContentResponse.statusCode());
    }

    @DisplayName("Тест скачивания файла")
    @Test
    @Order(10)
    void downloadFile() throws IOException
    {
        HttpRequest allContentRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources/download?path=disk:/newUpload"))
                .GET()
                .build();

        HttpResponse<String> getResponse = getResponseString(client, allContentRequest);

        String path = new JSONObject(getResponse.body()).getString("href");

        Assertions.assertEquals(200, getResponse.statusCode());
        File convFile = new File("src/main/resources/static/img.png");
        HttpRequest request = builder
                .uri(URI.create(path))
                .GET()
                .build();

        HttpResponse<String> response = getResponseString(client, request);

        HttpRequest downloadRequest = builder
                .uri(URI.create(response.headers().map().get("location").get(0)))
                .GET()
                .build();

        HttpResponse<byte[]> downloadResponse = null;
        try
        {
            downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());
        }
        catch (IOException | InterruptedException e)
        {
            Assertions.fail("Exception:" + e);
        }


        var file = Files.readAllBytes(convFile.toPath());
        Assertions.assertArrayEquals(file, downloadResponse.body());

    }

    @DisplayName("Тест удаления файла")
    @Test
    @Order(11)
    void deleteContent()
    {
        HttpRequest deleteContentRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/newUpload"))
                .DELETE()
                .build();

        HttpResponse<String> deleteContentResponse = getResponseString(client, deleteContentRequest);

        Assertions.assertEquals(204, deleteContentResponse.statusCode());

        HttpRequest getContentRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/newUpload"))
                .GET()
                .build();

        HttpResponse<String> getContentResponse = getResponseString(client, getContentRequest);

        Assertions.assertEquals(404, getContentResponse.statusCode());
    }

    @DisplayName("Тест востановления файла из мусора")
    @Order(12)
    @Test
    void restoreFromTrash()
    {
        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/trash/resources?path=trash:/"))
                .GET() // Default method, but explicitly set here
                .build();

        // 3. Send the request and receive the response synchronously
        HttpResponse<String> response = getResponseString(client, request);

        Assertions.assertEquals(200, response.statusCode());

        JSONObject jsonObject = new JSONObject(response.body());
        String path = null;
        JSONArray jsonArray = jsonObject.getJSONObject("_embedded").getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++)
        {

            if (jsonArray.getJSONObject(i).getString("name").equals("newUpload"))
            {
                path = jsonArray.getJSONObject(i).getString("path");
                break;
            }
        }

        Assertions.assertNotNull(path, "проверка на наличие url");

        HttpRequest restoreRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/trash/resources/restore?path=" + path))
                .PUT(HttpRequest.BodyPublishers.ofString("")) // Default method, but explicitly set here
                .build();
        HttpResponse<String> restoreResponse = getResponseString(client, restoreRequest);

        Assertions.assertEquals(201, restoreResponse.statusCode());

        // 4. Process the response
        HttpRequest checkRestoreRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/newUpload"))
                .GET() // Default method, but explicitly set here
                .build();
        HttpResponse<String> checkRestoreResponse = getResponseString(client, checkRestoreRequest);

        Assertions.assertEquals(200, checkRestoreResponse.statusCode());
    }

    @DisplayName("Тест очистки мусорки")
    @Order(13)
    @Test
    void clearTrash() throws InterruptedException
    {

        HttpRequest request = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/resources?path=disk:/newUpload"))
                .DELETE()
                .build();

        HttpResponse<String> response = getResponseString(client, request);

        Assertions.assertEquals(204, response.statusCode());

        HttpRequest clearTrashRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/trash/resources"))
                .DELETE()
                .build();

        HttpResponse<String> clearTrashResponse = getResponseString(client, clearTrashRequest);

        Assertions.assertTrue(clearTrashResponse.statusCode() == 204 || clearTrashResponse.statusCode() == 202);

        TimeUnit.SECONDS.sleep(4);

        HttpRequest checkTrashRequest = builder
                .uri(URI.create("https://cloud-api.yandex.net/v1/disk/trash/resources?path=trash:/"))
                .GET() // Default method, but explicitly set here
                .build();

        // 3. Send the request and receive the response synchronously
        HttpResponse<String> checkTrashResponse = getResponseString(client, checkTrashRequest);

        Assertions.assertEquals(200, checkTrashResponse.statusCode());

        JSONObject jsonObject = new JSONObject(checkTrashResponse.body());

        JSONArray jsonArray = jsonObject.getJSONObject("_embedded").getJSONArray("items");

        Assertions.assertTrue(jsonArray.isEmpty());

    }

    HttpResponse<String> getResponseString(HttpClient client, HttpRequest request)
    {
        HttpResponse<String> response = null;
        try
        {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e)
        {
            Assertions.fail("Exception:" + e);
        }
        return response;
    }

}
