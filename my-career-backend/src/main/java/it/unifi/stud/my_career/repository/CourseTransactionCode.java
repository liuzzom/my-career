package it.unifi.stud.my_career.repository;

import com.mongodb.Function;

@FunctionalInterface
public interface CourseTransactionCode<T> extends Function<CourseRepository, T> {

}
