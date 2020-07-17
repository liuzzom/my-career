package it.unifi.stud.my_career.service;

import java.util.ArrayList;
import java.util.List;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.repository.StudentRepository;
import it.unifi.stud.my_career.repository.TransactionManager;

public class MyCareerService {

	private TransactionManager transactionManager;

	public MyCareerService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public List<Student> getAllStudents() {
		return transactionManager.studentTransaction(StudentRepository::findAll);
	}

	public Student findStudent(Student student) {
		return transactionManager.studentTransaction(studentRepository -> studentRepository.findById(student.getId()));
	}

	public void saveStudent(Student student) {
		transactionManager.studentTransaction(studentRepository -> {
			studentRepository.save(student);
			return null;
		});
	}

	public void deleteStudent(Student student) {
		transactionManager.studentTransaction(studentRepository -> {
			studentRepository.delete(student.getId());
			return null;
		});
	}

	public List<Course> getCoursesByStudent(Student student) {
		List<Course> courses = new ArrayList<>();

		transactionManager.careerTransaction((studentRepository, courseRepository) -> {
			List<String> coursesIds = studentRepository.getParticipatedCoursesIdByStudentId(student.getId());

			for (String courseId : coursesIds) {
				courses.add(courseRepository.findById(courseId));
			}

			return null;
		});

		return courses;
	}

	public Course findCourse(Course course) {
		return transactionManager.courseTransaction(courseRepository -> courseRepository.findById(course.getId()));
	}

	public Course findSingleCourseByStudent(Student student, Course course) {
		Course courseFound = null;

		courseFound = transactionManager.careerTransaction((studentRepository, courseRepository) -> {
			List<String> coursesIds = studentRepository.getParticipatedCoursesIdByStudentId(student.getId());
			if (coursesIds.contains(course.getId())) {
				return courseRepository.findById(course.getId());
			}
			return null;
		});

		return courseFound;
	}

	public void saveCourse(Student student, Course course) {
		transactionManager.careerTransaction((studentRepository, courseRepository) -> {
			if (courseRepository.findById(course.getId()) == null)
				courseRepository.save(course);

			courseRepository.addCourseParticipant(course.getId(), student);
			studentRepository.addStudentParticipation(student.getId(), course);
			return null;
		});

	}

	public void removeCourse(Student student, Course course) {
		transactionManager.careerTransaction((studentRepository, courseRepository) -> {
			courseRepository.deleteCourseParticipant(course.getId(), student);
			studentRepository.deleteStudentParticipation(student.getId(), course);

			if (courseRepository.getParticipantsStudentsIdByCourseId(course.getId()).isEmpty())
				courseRepository.delete(course.getId());

			return null;
		});

	}

	public List<Student> getStudentsByCourse(Course course) {
		List<Student> students = new ArrayList<>();

		transactionManager.careerTransaction((studentRepository, courseRepository) -> {
			List<String> studentsIds = courseRepository.getParticipantsStudentsIdByCourseId(course.getId());
			for (String studentId : studentsIds) {
				students.add(studentRepository.findById(studentId));
			}
			return null;
		});

		return students;
	}

}
