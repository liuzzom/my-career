package it.unifi.stud.my_career.repository;

import java.util.function.BiFunction;

@FunctionalInterface
public interface TransactionCode<T> extends BiFunction<StudentRepository, CourseRepository, T>{

}
