package com.android.face.http.bean;

import java.util.ArrayList;
import java.util.List;

public class RoomNumbers {
    String room;
    List<String> numbers = new ArrayList<>();

    public RoomNumbers() {
    }

    public RoomNumbers(String room, List<String> numbers) {
        this.room = room;
        this.numbers.addAll(numbers);
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers.addAll(numbers);
    }

    public void addNumber(String number) {
        numbers.add(number);
    }

    public void removeNumber(String number) {
        numbers.remove(number);
    }
}
