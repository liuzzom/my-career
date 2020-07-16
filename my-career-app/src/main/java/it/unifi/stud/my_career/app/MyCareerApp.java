package it.unifi.stud.my_career.app;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
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

					TransactionManagerMongo txManager = new TransactionManagerMongo(mongoClient, studentRepo,
							courseRepo);
					MyCareerService service = new MyCareerService(txManager);

					if (userInterface.equals("gui")) {
						MyCareerSwingView frame = new MyCareerSwingView();
						MyCareerController controller = new MyCareerController(frame, service);
						frame.setMyCareerController(controller);
						frame.setVisible(true);
						controller.getAllStudents();
					}

					if (userInterface.equals("cli")) {
						LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
						Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
						rootLogger.setLevel(Level.OFF);

						MyCareerCLIView cli = new MyCareerCLIView(System.in, System.out);
						MyCareerController controller = new MyCareerController(cli, service);
						cli.setMyCareerController(controller);

						int userChoice;
						do {
							userChoice = cli.exec();
						} while (userChoice != 8);
					}
				} catch (Exception e) {
					java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,
							"Exception", e);
				}
			}
		});
		return null;
	}
}
