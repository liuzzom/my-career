package it.unifi.stud.my_career.repository;

import com.mongodb.Function;

@FunctionalInterface
public interface StudentTransactionCode<T> extends Function<StudentRepository, T> {

}
