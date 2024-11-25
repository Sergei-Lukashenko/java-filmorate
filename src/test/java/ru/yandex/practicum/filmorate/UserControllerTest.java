package ru.yandex.practicum.filmorate;

import com.google.gson.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.utils.UserRowMapper;
import ru.yandex.practicum.filmorate.utils.LocalDateAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
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
    void getUserFriendsWithNonexistentUserId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/users/-76545897/friends");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, response.statusCode(), "Bad HTTP status on GET user friends with negative user ID");
    }

    @Test
    void createUserAndGetUserFriends() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("serg@ru");
        user.setLogin("serg");
        user.setBirthday(LocalDate.of(1975, 1, 27));

        HttpClient client = HttpClient.newHttpClient();
        Long id = createUser(client, gson.toJson(user));

        URI uri = URI.create(String.format("http://localhost:8080/users/%d/friends", id));
        HttpRequest getRequest = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, getResponse.statusCode(), "Bad HTTP status on GET user friends with created user ID");
    }

    @Test
    void createUsersAndGetCommonFriends() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("serg@ru");
        user.setLogin("serg");
        user.setBirthday(LocalDate.of(1975, 1, 27));

        final User otherUser = new User();
        otherUser.setEmail("serg2@ru");
        otherUser.setLogin("serg2");
        otherUser.setBirthday(LocalDate.of(1975, 2, 28));

        HttpClient client = HttpClient.newHttpClient();

        Long id = createUser(client, gson.toJson(user));
        Long otherId = createUser(client, gson.toJson(otherUser));

        URI uri = URI.create(String.format("http://localhost:8080/users/%d/friends/common/%d", id, otherId));
        HttpRequest getRequest = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, getResponse.statusCode(), "Bad HTTP status on GET common user friends");
    }

    @Test
    void postValidUserCreation() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("serg@ru");
        user.setLogin("serg");
        user.setBirthday(LocalDate.of(1975, 1, 27));

        HttpClient client = HttpClient.newHttpClient();
        createUser(client, gson.toJson(user));
    }

    @Test
    void postRefuseUserCreationWhenLoginIsNotSpecified() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("serg@ru");

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
        user.setBirthday(LocalDate.of(1975, 1, 27));

        HttpClient client = HttpClient.newHttpClient();
        Long id = createUser(client, gson.toJson(user));

        final User userUpdate = new User();
        user.setId(id);
        userUpdate.setName("serg2 name");

        String userUpdateJson = gson.toJson(user);

        URI url = URI.create("http://localhost:8080/users");
        HttpRequest putRequest = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(userUpdateJson))
                .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, putResponse.statusCode(), "Bad HTTP status on PUT request for user update");
    }

    @Test
    void putAndDeleteUserFriends() throws IOException, InterruptedException {
        final User user = new User();
        user.setEmail("serg@ru");
        user.setLogin("serg");
        user.setBirthday(LocalDate.of(1975, 1, 27));

        final User otherUser = new User();
        otherUser.setEmail("serg2@ru");
        otherUser.setLogin("serg2");
        otherUser.setBirthday(LocalDate.of(1975, 2, 28));

        HttpClient client = HttpClient.newHttpClient();

        Long id = createUser(client, gson.toJson(user));
        Long otherId = createUser(client, gson.toJson(otherUser));

        // ******************* PUT user friend *******************
        URI url = URI.create(String.format("http://localhost:8080/users/%d/friends/%d", id, otherId));
        HttpRequest putRequest = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, putResponse.statusCode(), "Bad HTTP status on PUT user friend");

        // ******************* DELETE user friend *******************
        HttpRequest delRequest = HttpRequest.newBuilder().uri(url)
                .setHeader("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> delResponse = client.send(delRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, delResponse.statusCode(), "Bad HTTP status on DELETE user friend request");
    }

    private Long createUser(HttpClient client, String userJson)  throws IOException, InterruptedException {
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
        return jsonResponseObject.get("id").getAsLong();
    }
}
