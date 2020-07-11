package it.unifi.stud.my_career.controller;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.view.MyCareerView;
import it.unifi.stud.my_career.service.MyCareerService;

public class MyCareerController {

	private MyCareerView myCareerView;
	private MyCareerService myCareerService;

	public MyCareerController(MyCareerView myCareerView, MyCareerService myCareerService) {
		this.myCareerView = myCareerView;
		this.myCareerService = myCareerService;
	}
	
	// Student Section
	
	public void getAllStudents() {
		myCareerView.showAllStudents(myCareerService.getAllStudents());
	}
	
	public void addStudent(Student student) {
		Student existingStudent = myCareerService.findStudent(student);
		
		if(existingStudent != null) {
			myCareerView.showStudentError("Already exists a student with id " + student.getId(), existingStudent);
			return;
		}
		
		myCareerService.saveStudent(student);
		myCareerView.studentAdded("Student account created", student);
	}

	public void deleteStudent(Student student) {
		Student existingStudent = myCareerService.findStudent(student);
		
		if(existingStudent == null) {
			myCareerView.showStudentError("Student does not exist", student);
			return;
		}
		
		myCareerService.deleteStudent(student);
		myCareerView.studentRemoved("Student account deleted", student);
		
	}
	
	public void getCoursesByStudent(Student student) {
		if(myCareerService.findStudent(student) == null) {
			myCareerView.showStudentError("Student does not exist", student);
			return;
		}
		
		myCareerView.showAllCourses(myCareerService.getCoursesByStudent(student));
	}
	
	// Course Section

	public void addCourse(Student student, Course course) {
		if(myCareerService.findStudent(student) == null) {
			myCareerView.showCourseError("The student does not exist", course);
			return;			
		}
		
		if(myCareerService.findSingleCourseByStudent(student, course) != null) {
			myCareerView.showCourseError("The course already exists", course);
			return;
		}
		
		myCareerService.saveCourse(student, course);
		myCareerView.courseAdded("Added a new course", course);
		
	}

	public void removeCourse(Student student, Course course) {
		if(myCareerService.findStudent(student) == null) {
			myCareerView.showCourseError("The student does not exist", course);
			return;			
		}
		
		if(myCareerService.findSingleCourseByStudent(student, course) == null) {
			myCareerView.showCourseError("The course does not exist", course);
			return;
		}
			
		myCareerService.removeCourse(student, course);
		myCareerView.courseRemoved("The course has been removed", course);
		
	}


	public void getStudentsByCourse(Course course) {
		if(myCareerService.findCourse(course) == null) {
			myCareerView.showCourseError("The course does not exist", course);
			return;
		}
		
		myCareerView.showAllStudents(myCareerService.getStudentsByCourse(course));
	}




}
