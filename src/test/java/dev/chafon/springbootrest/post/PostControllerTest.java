package dev.chafon.springbootrest.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.chafon.springbootrest.Constants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String API_PATH = "/api/v1/posts";

    @Test
    void shouldReturnEmptyListAndStatusOkWhenNoPosts() throws Exception {
        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnPostsAndStatusOkWhenPostsExist() throws Exception {
        List<Post> posts = List.of(
                new Post(1, 123, "My first post", "My first post content"),
                new Post(2, 123, "My second post", "My second post content")
        );
        given(postService.getPosts())
                .willReturn(posts);

        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(posts.getFirst().id())))
                .andExpect(jsonPath("$[0].userId", equalTo(posts.getFirst().userId())))
                .andExpect(jsonPath("$[0].title", equalTo(posts.getFirst().title())))
                .andExpect(jsonPath("$[0].body", equalTo(posts.getFirst().body())))
                .andExpect(jsonPath("$[1].id", equalTo(posts.get(1).id())))
                .andExpect(jsonPath("$[1].userId", equalTo(posts.get(1).userId())))
                .andExpect(jsonPath("$[1].title", equalTo(posts.get(1).title())))
                .andExpect(jsonPath("$[1].body", equalTo(posts.get(1).body())));
    }

    @Test
    void shouldReturnPostAndStatusOkWhenPostExists() throws Exception {
        Post post = new Post(1, 123, "My first post", "My first post content");
        given(postService.getPost(post.id()))
                .willReturn(post);

        mvc.perform(get(API_PATH + "/{id}", post.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(post.id())))
                .andExpect(jsonPath("$.userId", equalTo(post.userId())))
                .andExpect(jsonPath("$.title", equalTo(post.title())))
                .andExpect(jsonPath("$.body", equalTo(post.body())));
    }

    @Test
    void shouldReturnStatusNotFoundWhenPostDoesNotExist() throws Exception {
        Integer idToGet = 100;
        willThrow(new PostNotFoundException(idToGet))
                .given(postService).getPost(idToGet);

        mvc.perform(get(API_PATH + "/{id}", idToGet))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", equalTo(POST_NOT_FOUND_EXCEPTION_MESSAGE + idToGet)));
    }

    @Test
    void shouldCreatePostAndReturnPostAndLocationAndStatusCreated() throws Exception {
        Post postToCreate = new Post(null, 567, "Java post", "Java post content");
        Post postCreated = new Post(123, 567, "Java post", "Java post content");

        given(postService.createPost(postToCreate))
                .willReturn(postCreated);

        mvc.perform(post(API_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(postCreated.id())))
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost" + API_PATH + "/" + postCreated.id()));
    }

    @Test
    void shouldReturnStatusBadRequestWhenPostToCreateIsInvalid() throws Exception {
        Post postToCreate = new Post(null, null, null, null);

        mvc.perform(post(API_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postToCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(POST_USER_CANNOT_BE_NULL)))
                .andExpect(jsonPath("$.detail", containsString(POST_TITLE_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(POST_BODY_CANNOT_BE_BLANK)));
    }

    @Test
    void shouldUpdatePostAndReturnStatusNoContent() throws Exception {
        Post postToUpdate = new Post(1, 123, "My first post", "My first post content");

        mvc.perform(put(API_PATH + "/{id}", postToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postToUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStatusBadRequestWhenPostToUpdateIsInvalid() throws Exception {
        Post postToUpdate = new Post(123, null, null, null);

        mvc.perform(put(API_PATH + "/{id}", postToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postToUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(POST_USER_CANNOT_BE_NULL)))
                .andExpect(jsonPath("$.detail", containsString(POST_TITLE_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(POST_BODY_CANNOT_BE_BLANK)));
    }

    @Test
    void shouldReturnStatusNotFoundWhenPostToUpdateDoesNotExist() throws Exception {
        Post postToUpdate = new Post(100, 123, "My first post", "My first post content");

        willThrow(new PostNotFoundException(postToUpdate.id()))
                .given(postService).updatePost(postToUpdate.id(), postToUpdate);

        mvc.perform(put(API_PATH + "/{id}", postToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(POST_NOT_FOUND_EXCEPTION_MESSAGE + postToUpdate.id())));
    }

    @Test
    void shouldDeletePostAndReturnStatusNoContent() throws Exception {
        mvc.perform(delete(API_PATH + "/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStatusNotFoundWhenPostToDeleteDoesNotExist() throws Exception {
        Integer idToDelete = 100;
        willThrow(new PostNotFoundException(idToDelete))
                .given(postService).deletePost(idToDelete);

        mvc.perform(delete(API_PATH + "/{id}", idToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(POST_NOT_FOUND_EXCEPTION_MESSAGE + idToDelete)));
    }
}
