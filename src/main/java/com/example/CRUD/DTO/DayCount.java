package com.example.CRUD.DTO;
import java.io.Serializable;
import java.time.LocalDate;
public record DayCount(LocalDate date, long orderCount) implements Serializable { }