package com.studyIn.modules.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventList {

    private List<Event> AllEvents = new ArrayList<>();

    private List<Event> newEvents = new ArrayList<>();

    private List<Event> oldEvents = new ArrayList<>();
}
