package it.unifi.stud.my_career.repository;

public interface TransactionManager {

	<T> T careerTransaction(TransactionCode<T> code);

	<T> T studentTransaction(StudentTransactionCode<T> code);

	<T> T courseTransaction(CourseTransactionCode<T> code);

}
