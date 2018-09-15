package io.pivotal.cfapp.repository;

import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;

@Profile("jdbc")
@Component
public class JdbcServiceDetailAggregator implements ServiceDetailAggregator {

private Database database;
	
	@Autowired
	public JdbcServiceDetailAggregator(Database database) {
		this.database = database;
	}
    
    public List<ServiceCount> countServicesByType() {
    	return database
    			.select("SELECT service, COUNT(id) AS total FROM service_detail GROUP BY service")
    			.get(rs -> new ServiceCount(rs.getString(1), rs.getInt(2)))
    			.toList()
    			.blockingGet();
    }
    
    public List<OrganizationCount> countServicesByOrganization() {
    	return database
				.select("SELECT organization, COUNT(id) AS total FROM service_detail GROUP BY organization")
				.get(rs -> new OrganizationCount(rs.getString(1), rs.getInt(2)))
				.toList()
				.blockingGet();
    }
    
}
