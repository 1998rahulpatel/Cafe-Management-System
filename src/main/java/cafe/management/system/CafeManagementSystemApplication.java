package cafe.management.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@CrossOrigin(origins = "*")
@OpenAPIDefinition(
    info = @Info(
        title = "Cafe Management System API",
        version = "1.0",
        description = "API for managing a cafe's products, orders, and users.",
        summary = """
                This API provides endpoints for managing a cafe's products, orders, and users.
                It uses JWT (JSON Web Tokens) for authentication and authorization.
            """,
        termsOfService = "T&C",
        contact = @Contact(
            name = "Cafe Management System",
            email = "cafemgmtsystem@gmail.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
public class CafeManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeManagementSystemApplication.class, args);
	}

}
