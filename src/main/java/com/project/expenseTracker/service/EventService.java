package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.EventDto;
import com.project.expenseTracker.entity.Event;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.EventRepository;
import com.project.expenseTracker.repository.ExpenseRepository;
import com.project.expenseTracker.repository.MonthRepository;
import com.project.expenseTracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.project.expenseTracker.service.MonthService.getMonthName;
import static com.project.expenseTracker.service.MonthService.getMonthYear;

@Service
@AllArgsConstructor
public class EventService {

    private EventRepository EventRepository;
    private UserRepository userRepository;

    public Long createEvent(EventDto EventDto){
        User user = userRepository.findById(EventDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found"));

        Event Event = mapDtoToEntity(null, EventDto, user);
        return EventRepository.save(Event).getId();
    }

    public EventDto updateEvent(EventDto EventDto, Long id){
        Event existingEvent = EventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
        Event Event = EventRepository.save(mapDtoToEntity(existingEvent, EventDto, null));
        return mapEntityToDto(Event);
    }

    public EventDto getEventById(Long id) {
        Event Event = EventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Event not found"));
        return mapEntityToDto(Event);
    }

    public List<EventDto> getEventByMonthId(Long userId){
        List<Event> categories = EventRepository.findByUserId(userId);
        return categories.stream().map(EventService::mapEntityToDto).toList();
    }

    public Long deleteEventById(Long id){
        EventRepository.deleteById(id);
        return id;
    }

    private Event mapDtoToEntity(Event existingEvent, EventDto EventDto, User user){
        if(existingEvent != null){
            existingEvent.setName(EventDto.getName());
            return existingEvent;
        }
        return Event.builder()
                .name(EventDto.getName())
                .user(user)
                .build();
    }

    public static EventDto mapEntityToDto(Event Event){
        return EventDto.builder()
                .id(Event.getId())
                .name(Event.getName())
                .userId(Event.getUser().getId())
                .expenses(Event.getExpenses().stream()
                        .map(ExpenseService::mapEntityToDTo).toList())
                .build();
    }

}
