package io.pivotal.cfapp.repository;

import java.util.ArrayList;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;

@Profile("jdbc")
@Component
public class JdbcServiceInstanceDetailAggregator implements ServiceInstanceDetailAggregator {

private Database database;

	@Autowired
	public JdbcServiceInstanceDetailAggregator(Database database) {
		this.database = database;
	}

    public List<ServiceCount> countServicesByType() {
		List<ServiceCount> result = new ArrayList<>();
    	Flowable<ServiceCount> records = database
    			.select("SELECT service, plan, COUNT(id) AS total FROM service_detail GROUP BY service, plan")
				.get(rs -> new ServiceCount(rs.getString(1), rs.getString(2), rs.getInt(3)));
		Flux.from(records).subscribe(result::add);
		return result;
    }

    public List<OrganizationCount> countServicesByOrganization() {
		List<OrganizationCount> result = new ArrayList<>();
    	Flowable<OrganizationCount> records = database
				.select("SELECT organization, COUNT(id) AS total FROM service_detail GROUP BY organization")
				.get(rs -> new OrganizationCount(rs.getString(1), rs.getInt(2)));
		Flux.from(records).subscribe(result::add);
		return result;
    }

}
