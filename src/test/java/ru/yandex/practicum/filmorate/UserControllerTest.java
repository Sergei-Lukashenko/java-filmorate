package ru.yandex.practicum.filmorate;

import com.google.gson.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.LocalDateAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private static final ConfigurableApplicationContext run = SpringApplication.run(FilmorateApplication.class);
    private static Gson gson;

    @BeforeAll
    public static void beforeAll() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gson = gsonBuilder.create();
    }

    @AfterAll
    public static void afterAll()  {
        run.close();
    }

    @Test
    void getUsers() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/users");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode(), "Bad HTTP status on GET users request");
    }

    @Test
    void postValidUserCreation() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("s@ru");
        user.setLogin("serg");
        user.setBirthday(LocalDate.of(1975, 01, 27));

        String userJson = gson.toJson(user);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/users");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Bad HTTP status on POST request for user creation");
    }

    @Test
    void postRefuseUserCreationWhenLoginIsNotSpecified() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("s@ru");

        String userJson = gson.toJson(user);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/users");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Bad HTTP status on POST request for user without login");
    }

    @Test
    void putUserUpdateAfterCreation() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("s2@ru");
        user.setLogin("serg2");
        user.setBirthday(LocalDate.of(1975, 01, 27));

        String userJson = gson.toJson(user);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/users");
        HttpRequest postRequest = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, postResponse.statusCode(), "Bad HTTP status on POST request for user creation");

        // передаем парсеру тело ответа в виде строки, содержащей данные в формате JSON
        JsonElement jsonElement = JsonParser.parseString(postResponse.body());
        // преобразуем результат разбора текста в JSON-объект
        JsonObject jsonResponseObject = jsonElement.getAsJsonObject();

        // получаем из ответа идентификатор созданного пользователя
        Long id = jsonResponseObject.get("id").getAsLong();
        final User userUpdate = new User();
        user.setId(id);
        userUpdate.setName("serg2 name");

        String userUpdateJson = gson.toJson(user);

        HttpRequest putRequest = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(userUpdateJson))
                .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, putResponse.statusCode(), "Bad HTTP status on PUT request for user update");
    }
}
