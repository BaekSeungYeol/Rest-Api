package me.whitewin.restapiwithspring.events;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder()
        .name("Spring Reset Api")
        .description("Rest Api development")
        .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        //Given
        String name = "Event";
        String descrption = "Spring";

        //When
        Event event = new Event();
        event.setName("Event");
        event.setDescription(descrption);

        //Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(descrption);

    }
}