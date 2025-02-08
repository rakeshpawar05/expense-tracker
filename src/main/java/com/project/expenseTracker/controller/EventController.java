package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.EventDto;
import com.project.expenseTracker.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventController {


    private EventService EventService;

    @GetMapping("/{id}")
    public EventDto getEventById(@PathVariable Long id){
        return EventService.getEventById(id);
    }

    @GetMapping
    public List<EventDto> getEvents(@RequestParam Long userId){
        return EventService.getEventByMonthId(userId);
    }

    @PostMapping
    public long createEvent(@RequestBody EventDto EventDto){
        return EventService.createEvent(EventDto);
    }

    @PutMapping("/{id}")
    public EventDto updateEvent(@RequestBody EventDto EventDto, @PathVariable Long id){
        return EventService.updateEvent(EventDto, id);
    }


    @DeleteMapping("/{id}")
    public long deleteEvent(@PathVariable Long id){
        return EventService.deleteEventById(id);
    }
}
