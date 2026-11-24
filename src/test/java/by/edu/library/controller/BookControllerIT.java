package by.edu.library.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты API: поднимается весь контекст Spring, запросы идут через MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired
    MockMvc mvc;

    // добавляет книгу и возвращает её id
    private long addBook(String title, String genre) throws Exception {
        String json = "{\"title\":\"" + title + "\",\"author\":\"Автор\",\"year\":2000"
                + (genre == null ? "" : ",\"genre\":\"" + genre + "\"") + "}";
        MvcResult res = mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String body = res.getResponse().getContentAsString();
        // {"id":5,...} - вытаскиваем число после "id":
        String id = body.substring(body.indexOf("\"id\":") + 5, body.indexOf(','));
        return Long.parseLong(id);
    }

    @Test
    void addAndGetBook() throws Exception {
        long id = addBook("Мастер и Маргарита", "FICTION");
        mvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Мастер и Маргарита"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void genreIsOtherIfNotSent() throws Exception {
        long id = addBook("Без жанра", null);
        mvc.perform(get("/api/books/" + id)).andExpect(jsonPath("$.genre").value("OTHER"));
    }

    @Test
    void listFilteredByGenre() throws Exception {
        addBook("Букварь", "CHILDREN");
        mvc.perform(get("/api/books").param("genre", "CHILDREN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].genre", everyItem(is("CHILDREN"))));
    }

    @Test
    void unknownGenreParamIs400() throws Exception {
        mvc.perform(get("/api/books").param("genre", "POEMS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void issueBookByPut() throws Exception {
        long id = addBook("Капитанская дочка", "FICTION");
        mvc.perform(put("/api/books/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ISSUED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.genre").value("FICTION"));
    }

    @Test
    void changeGenreByPut() throws Exception {
        long id = addBook("Сказки", "FICTION");
        mvc.perform(put("/api/books/" + id + "/genre").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genre\":\"CHILDREN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genre").value("CHILDREN"));
    }

    @Test
    void emptyStatusIs400() throws Exception {
        long id = addBook("Книга", null);
        mvc.perform(put("/api/books/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankTitleIs400() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"author\":\"Пушкин\",\"year\":1833}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Название книги обязательно"));
    }

    @Test
    void wrongYearIs400() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Книга\",\"author\":\"Автор\",\"year\":3000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteThenNotFound() throws Exception {
        long id = addBook("Удалить", null);
        mvc.perform(delete("/api/books/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/books/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
