package it.unifi.stud.my_career.repository.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import it.unifi.stud.my_career.repository.CourseRepository;
import it.unifi.stud.my_career.repository.CourseTransactionCode;
import it.unifi.stud.my_career.repository.StudentRepository;
import it.unifi.stud.my_career.repository.StudentTransactionCode;
import it.unifi.stud.my_career.repository.TransactionCode;
import it.unifi.stud.my_career.repository.TransactionManager;

public class TransactionManagerMongo implements TransactionManager {

	private MongoClient mClient;
	private StudentRepository studentRepository;
	private CourseRepository courseRepository;

	public TransactionManagerMongo(MongoClient client, StudentRepository studentRepo, CourseRepository courseRepo) {
		mClient = client;
		studentRepository = studentRepo;
		courseRepository = courseRepo;
	}

	public <T> T careerTransaction(TransactionCode<T> code) {

		T rValue = null;
		ClientSession clientSession = mClient.startSession();
		try {
			clientSession.startTransaction();
			rValue = code.apply(studentRepository, courseRepository);
			clientSession.commitTransaction();
		} catch (RuntimeException e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		return rValue;

	};

	public <T> T studentTransaction(StudentTransactionCode<T> code) {

		T rValue = null;
		ClientSession clientSession = mClient.startSession();
		try {
			clientSession.startTransaction();
			rValue = code.apply(studentRepository);
			clientSession.commitTransaction();
		} catch (RuntimeException e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		return rValue;

	};

	public <T> T courseTransaction(CourseTransactionCode<T> code) {

		T rValue = null;
		ClientSession clientSession = mClient.startSession();
		try {
			clientSession.startTransaction();
			rValue = code.apply(courseRepository);
			clientSession.commitTransaction();
		} catch (RuntimeException e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		return rValue;

	};
}
