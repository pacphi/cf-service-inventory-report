package io.pivotal.cfapp.repository;

import java.sql.Connection;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.exceptions.SQLRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Profile("jdbc")
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final Database database;

	@Autowired
	public DatabaseCreator(Database database) {
		this.database = database;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection c = database.connection().blockingGet()) {
            c.setAutoCommit(true);
            c.prepareStatement("create table service_detail ( id int identity primary key, organization varchar(100), space varchar(100), service_id varchar(50), name varchar(100), service varchar(100), description varchar(1000), plan varchar(50), type varchar(30), bound_applications clob(20M), last_operation varchar(50), last_updated timestamp, dashboard_url varchar(250), requested_state varchar(25) )")
    		.execute();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
	}

}
