package it.unifi.stud.my_career.app;

import java.awt.EventQueue;
import java.util.Scanner;
import java.util.concurrent.Callable;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.repository.mongo.CourseRepositoryMongo;
import it.unifi.stud.my_career.repository.mongo.StudentRepositoryMongo;
import it.unifi.stud.my_career.repository.mongo.TransactionManagerMongo;
import it.unifi.stud.my_career.service.MyCareerService;
import it.unifi.stud.my_career.view.cli.MyCareerCLIView;
import it.unifi.stud.my_career.view.swing.MyCareerSwingView;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class MyCareerApp implements Callable<Void> {

	// Command Line argument for UI
	@Option(names = { "--ui", "--user-interface" }, description = "User Interface type (allowable values: gui, cli)")
	private String userInterface = "gui";

	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "career";

	@Option(names = { "--db-student-collection" }, description = "Students collection name")
	private String studentCollectionName = "students";

	@Option(names = { "--db-course-collection" }, description = "Coruses collection name")
	private String courseCollectionName = "courses";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new CommandLine(new MyCareerApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					
					if (!userInterface.equals("gui") && !userInterface.equals("cli")) {
						System.err
								.println("Invalid value for option '--ui/--user-interface'\nUse --ui=gui or --ui=cli");
						System.exit(1);
					}
					
					MongoClient mongoClient = new MongoClient(new ServerAddress(mongoHost, mongoPort));
					StudentRepositoryMongo studentRepo = new StudentRepositoryMongo(mongoClient, databaseName,
							studentCollectionName);
					CourseRepositoryMongo courseRepo = new CourseRepositoryMongo(mongoClient, databaseName,
							courseCollectionName);
					
					TransactionManagerMongo txManager = new TransactionManagerMongo(mongoClient, studentRepo, courseRepo);
					MyCareerService service = new MyCareerService(txManager);

					if (userInterface.equals("gui")) {
						MyCareerSwingView frame = new MyCareerSwingView();
						MyCareerController controller = new MyCareerController(frame,service);
						frame.setMyCareerController(controller);
						frame.setVisible(true);
						controller.getAllStudents();
					}

					if (userInterface.equals("cli")) {
						MyCareerCLIView cli = new MyCareerCLIView(System.in, System.out);
						MyCareerController controller = new MyCareerController(cli, service);
						cli.setMyCareerController(controller);
						String choice;
						Scanner scanner = new Scanner(System.in);

						do {
							cli.showMenu();
							System.out.print("Enter a valid digit: ");
							choice = scanner.nextLine();
							switch (choice) {
							case "1":
								cli.addStudent();
								break;
							case "2":
								cli.getAllStudents();
								break;
							case "3":
								cli.deleteStudent();
								break;
							case "4":
								cli.getCoursesByStudent();
								break;
							case "5":
								cli.addCourse();
								break;
							case "6":
								cli.removeCourse();
								break;
							case "7":
								cli.getStudentsByCourse();
								break;
							case "8":
								System.out.println("Goodbye");
								break;
							default:
								System.out.println("Not a valid input");
								break;
							}
						} while (!choice.equals("8"));

						scanner.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}
}
