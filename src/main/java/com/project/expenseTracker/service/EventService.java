package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.EventDto;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.entity.Event;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.EventRepository;
import com.project.expenseTracker.repository.ExpenseRepository;
import com.project.expenseTracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository EventRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public long createEvent(EventDto EventDto){
        User user = userRepository.findById(EventDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + EventDto.getUserId()));
        Event Event = mapDtoToEntity(null, EventDto, user);
        return EventRepository.save(Event).getId();
    }

    public EventDto updateEvent(EventDto EventDto, Long id){
        Event existingEvent = EventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        Event Event = EventRepository.save(mapDtoToEntity(existingEvent, EventDto, null));
        return mapEntityToDto(Event);
    }

    public EventDto getEventById(Long id){
        Event Event = EventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapEntityToDto(Event);
    }

    public List<EventDto> getEventByUserId(Long userId){
        List<Event> events = EventRepository.findByUserId(userId);
        return events.stream().map(EventService::mapEntityToDto).toList();
    }

    public List<EventDto> getEvents(Long userId){
        return getEventByUserId(userId);
    }

    public long deleteEventById(Long id){
        List<Expense> expenses = expenseRepository.findByEventId(id);
        expenses.forEach(expense -> {
            expense.setEvent(null);
            expenseRepository.save(expense);
        });
        EventRepository.deleteById(id);
        return id;
    }

    private Event mapDtoToEntity(Event existingEvent, EventDto EventDto, User user){
        if(existingEvent != null){
            return existingEvent.toBuilder()
                    .name(EventDto.getName())
                    .build();
        }
        return Event.builder()
                .name(EventDto.getName())
                .user(user)
                .build();
    }

    public static EventDto mapEntityToDto(Event event){
        if (event == null) return null;
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .userId(event.getUser() != null ? event.getUser().getId() : null)
                .expenses(event.getExpenses() != null
                        ? event.getExpenses().stream().map(ExpenseService::mapEntityToDTo).toList()
                        : Collections.emptyList())
                .savings(event.getSavings() != null
                        ? event.getSavings().stream().map(SavingService::mapEntityToDTo).toList()
                        : Collections.emptyList())
                .build();
    }

}
