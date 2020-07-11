package it.unifi.stud.my_career.view;

import java.util.List;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public interface MyCareerView {
	
	// Show an error message related to the student management
	void showStudentError(String message, Student student);
	
	// Show all students related to a course or all students of the system
	void showAllStudents(List<Student> students);
	
	// Show a message when a student is added with success and update the student list
	void studentAdded(String message, Student student);
	
	// Show a message when a student is removed with success and update the student list	
	void studentRemoved(String message, Student student);
	
	// Show an error message related to the course management (i.e. during login or account removing)
	void showCourseError(String message, Course course);
	
	// Show all courses related to a student or all courses of the system
	void showAllCourses(List<Course> courses);
	
	// Show a message when a course is added with success and update the course list
	void courseAdded(String message, Course course);
	
	// Show a message when a course is removed with success and update the course list
	void courseRemoved(String message, Course course);
}
