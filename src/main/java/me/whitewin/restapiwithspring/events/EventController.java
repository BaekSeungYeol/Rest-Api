package me.whitewin.restapiwithspring.events;

import me.whitewin.restapiwithspring.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }


    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {

        if(errors.hasErrors()) {
            return badRequest(errors);
        }
        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return badRequest(errors);
        }
        Event event =modelMapper.map(eventDto, Event.class);
        //유료, 무료 여부 변경
        event.update();
        Event newEvent = this.eventRepository.save(event);

        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

   @GetMapping
   public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

       Page<Event> page = this.eventRepository.findAll(pageable);
       var pagedResources = assembler.toResource(page, e-> new EventResource(e));
       pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
       return ResponseEntity.ok(pagedResources);
   }

   @GetMapping("/{id}")
   public ResponseEntity getEvent(@PathVariable Integer id) {
       Optional<Event> optionalEvent = this.eventRepository.findById(id);
       if(optionalEvent.isEmpty()) {
           return ResponseEntity.notFound().build();
       }
       Event event = optionalEvent.get();
       EventResource eventResource = new EventResource(event);
       eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
       return ResponseEntity.ok(eventResource);
   }

   @PutMapping("/{id}")
   public ResponseEntity upDateEvent(@PathVariable Integer id,
                                     @RequestBody @Valid EventDto eventDto,
                                     Errors errors) {
       Optional<Event> optionalEvent = this.eventRepository.findById(id);
       if (optionalEvent.isEmpty()) {
           return ResponseEntity.notFound().build();
       }

       if (errors.hasErrors()) {
           return badRequest(errors);
       }

       this.eventValidator.validate(eventDto, errors);
       if(errors.hasErrors()) {
           return badRequest(errors);
       }

       Event existingEvent = optionalEvent.get();
       this.modelMapper.map(eventDto, existingEvent);
       Event savedEvent = this.eventRepository.save(existingEvent);

       EventResource eventResource = new EventResource(savedEvent);
       eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

       return ResponseEntity.ok(eventResource);


   }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
