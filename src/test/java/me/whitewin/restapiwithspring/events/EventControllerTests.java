package me.whitewin.restapiwithspring.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class EventControllerTests {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    EventRepository eventRepository;
    @Before()
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    }

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void createEvent() throws Exception {
        Event event = Event.builder()
                .name("Spring")
                .description("Rest Api Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,12,28,12,11))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,12,29,12,11))
                .beginEventDateTime(LocalDateTime.of(2019,12,29,12,30))
                .endEventDateTime(LocalDateTime.of(2019,12,30,12,30))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("노원역 D2 팩토리")
                .build();
        event.setId(10);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(jsonPath("id").exists());
    }
}
