package pers.nanahci.reactor.datacenter;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "pers.nanahci.reactor.datacenter.dal.repo")
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }

}
