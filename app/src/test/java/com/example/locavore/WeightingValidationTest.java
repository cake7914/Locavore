package com.example.locavore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.util.Log;

import com.example.locavore.Models.Event;
import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class WeightingValidationTest {

   @Test
    public void mergeSortReturnsCorrectOrder() {
       Event event1 = mock(Event.class);
       Event event2 = mock(Event.class);
       Event event3 = mock(Event.class);
       Event event4 = mock(Event.class);
       Event event5 = mock(Event.class);

       event1.mWeight = 561;
       event2.mWeight = 365;
       event3.mWeight = 241;
       event4.mWeight = 442;
       event5.mWeight = 15;

       List<Event> events = new ArrayList<>();
       events.add(event1);
       events.add(event5);
       events.add(event4);
       events.add(event3);
       events.add(event2);

       DataManager.mergeSort(events, events.size());
       assertSame(events.get(0), event1);
       assertSame(events.get(1), event4);
       assertSame(events.get(2), event2);
       assertSame(events.get(3), event3);
       assertSame(events.get(4), event5);
    }

    @Test
    public void shortInputMergeSort() {
      Event event1 = mock(Event.class);
      Event event2 = mock(Event.class);

      event1.mWeight = 322;
      event2.mWeight = 540;

      List<Event> events = new ArrayList<>();

      events.add(event1);
      events.add(event2);

      DataManager.mergeSort(events, events.size());
      assertSame(events.get(0), event2);
      assertSame(events.get(1), event1);
    }
}
