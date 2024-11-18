package cafe.management.system.jwt;

import cafe.management.system.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class CustomUsersDetailsService implements UserDetailsService {

	@Autowired
	UserDao userDao;

	private cafe.management.system.model.User userDetails;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.info("Inside loadUserByUsername {}", email);
		userDetails = this.userDao.findByEmail(email);
		if (!Objects.isNull(userDetails)) {
			return new org.springframework.security.core.userdetails.User(userDetails.getEmail(),
					userDetails.getPassword(), new ArrayList<>());
		} else {
			throw new UsernameNotFoundException("User not found.");
		}
	}

	public cafe.management.system.model.User getUserDetails() {
		return this.userDetails;
	}
}
