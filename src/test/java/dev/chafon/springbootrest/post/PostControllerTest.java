package dev.chafon.springbootrest.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.chafon.springbootrest.Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mvc;

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
    void shouldReturnThePostWithStatusOkWhenPostExists() throws Exception {
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
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        Integer idToGet = 100;
        willThrow(new PostNotFoundException(idToGet))
                .given(postService).getPost(idToGet);

        mvc.perform(get(API_PATH + "/{id}", idToGet))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", equalTo(POST_NOT_FOUND_EXCEPTION_MESSAGE + idToGet)));
    }
}
