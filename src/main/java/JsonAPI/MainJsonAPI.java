package JsonAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainJsonAPI {
    private static final String USER_API = "https://jsonplaceholder.typicode.com/users";
    private static final String POSTS_API = "https://jsonplaceholder.typicode.com/users/%d/posts";
    private static final String COMMENTS_API =
            "https://jsonplaceholder.typicode.com/users/%d/comments";
    private static final String TODOS_API = "https://jsonplaceholder.typicode.com/users/%d/todos";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        // Створення нового користувача
        Map<String, Object> newUser = Map.of(
                "name", "NewUser",
                "username", "newuser",
                "email", "newuser@example.com"
        );
        String createdUserResponse = createUser(newUser);
        System.out.println("Created User:\n" + createdUserResponse);

        // Оновлення існуючого користувача (ID 4)
        Map<String, Object> updatedUser = Map.of(
                "name", "Updated User",
                "email", "updated@example.com"
        );
        String updatedUserResponse = updateUser(4, updatedUser);
        System.out.println("\nUpdate User (ID 4):\n" + updatedUserResponse);

        // Видалення користувача (3й зайвий)
        String deleteResponse = deleteUser(3);
        System.out.println("\nResults delete user (ID 3):\n" + deleteResponse);

        // Отримання інформації про всіх користувачів
        List<Map<String, Object>> allUsers = getAllUsers();
        System.out.println("\nAll Users: ");
        allUsers.forEach(System.out::println);

        // Отримання інформації про користувача за ID (ID 2)
        Map<String, Object> userById = getUserInfoId(2);
        System.out.println("\nUser with ID 2:\n" + userById);

        // Отримання інформації про користувача за username
        List<Map<String, Object>> usersByUsername = getUsername("Karianne");
        System.out.println("\nUsers with username:");
        usersByUsername.forEach(System.out::println);

        // Виведення та запис коментарів до останнього поста користувача з ID 1
        getWriteComments(1);

        // Виведення всіх відкритих задач для користувача з ID 1
        System.out.println("\nAll open task:");
        printOpenTodos(1);
    }

    private static void validResp(Response response, int expectedStatusCode) {
        if (expectedStatusCode != response.statusCode()) {
            System.err.println("\nExpected status code " + expectedStatusCode +
                    ", but got " + response.statusCode());
            System.err.println(response.body());
        }
    }

    // Створення нового користувача
    public static String createUser(Map<String, Object> userData) throws IOException {
        Response response = Jsoup.connect(USER_API)
                .requestBody(gson.toJson(userData))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .header("Content-Type", "application/json; charset=UTF-8")
                .execute();
        validResp(response, 201);
        return response.body();
    }

    // Оновлення існуючого користувача за ID
    public static String updateUser(int id, Map<String, Object> userData) throws IOException {
        Response response = Jsoup.connect(USER_API + "/" + id)
                .requestBody(gson.toJson(userData))
                .method(Connection.Method.PUT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .header("Content-Type", "application/json; charset=UTF-8")
                .execute();
        validResp(response, 200);
        return response.body();
    }

    // Видалення користувача за ID
    public static String deleteUser(int id) throws IOException {
        Response response = Jsoup.connect(USER_API + "/" + id)
                .method(Connection.Method.DELETE)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        validResp(response, 200);
        return response.body();
    }

    // Отримання інформації про всіх користувачів
    public static List<Map<String, Object>> getAllUsers() throws IOException {
        Response response = Jsoup.connect(USER_API)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        validResp(response, 200);
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response.body(), listType);
    }

    // Отримання інформації про користувача за ID
    public static Map<String, Object> getUserInfoId(int id) throws IOException {
        Response response = Jsoup.connect(USER_API + "/" + id)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(response.body(), mapType);
    }

    // Отримання інформації про користувача за username
    public static List<Map<String, Object>> getUsername(String username) throws IOException {
        Response response = Jsoup.connect(USER_API + "?username=" + username)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response.body(), listType);
    }

    // Отримання всіх постів користувача за ID
    public static List<Map<String, Object>> getUserPosts(int userId) throws IOException {
        Response response = Jsoup.connect(String.format(POSTS_API, userId))
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response.body(), listType);
    }

    // Получение комментариев по ID
    public static List<Map<String, Object>> getPostComments(int postId) throws IOException {
        Response response = Jsoup.connect(String.format(COMMENTS_API, postId))
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response.body(), listType);
    }

    // Метод для получения и записи комментариев в последний пост пользователя
    public static void getWriteComments(int userId) throws IOException {
        List<Map<String, Object>> userPosts = getUserPosts(userId);

        if (userPosts != null && !userPosts.isEmpty()) {
            // Знаходимо пост з найбільшим id (останній пост)
            Map<String, Object> lastPost = userPosts.stream()
                    .max(Comparator.comparingInt(
                            post -> ((Number) post.get("id")).intValue()))
                    .orElse(null);


            if (lastPost != null) {
                int postId = ((Number) lastPost.get("id")).intValue();
                List<Map<String, Object>> comments = getPostComments(postId);

                // Записуємо коментарі у файл
                try (FileWriter fileWriter = new FileWriter(
                        "src/main/resources/user-X-post-Y-comments.json")) {
                    gson.toJson(comments, fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Получение всех задач пользователя по ID
    public static List<Map<String, Object>> getUserTodos(int userId) throws IOException {
        String response = Jsoup.connect(String.format(TODOS_API, userId))
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute()
                .body();
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }

    // Метод для виведення всіх відкритих задач для користувача за ID
    public static void printOpenTodos(int userId) throws IOException {
        List<Map<String, Object>> todos = getUserTodos(userId);

        if (todos != null && !todos.isEmpty()) {
            todos.stream()
                    .filter(
                            todo -> !((Boolean) todo.get("completed")))
                    .forEach(
                            todo -> System.out.println("- " + todo.get("title")));
        }
    }
}

