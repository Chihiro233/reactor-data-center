package pers.nanahci.reactor.datacenter;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
//@EnableJpaRepositories(basePackages = "pers.nanahci.reactor.datacenter.dal.entity.repo")
//@EntityScan(basePackages = "pers.nanahci.reactor.datacenter.dal.entity")
@EnableR2dbcRepositories(basePackages = "pers.nanahci.reactor.datacenter.dal.entity.repo")
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }

}
