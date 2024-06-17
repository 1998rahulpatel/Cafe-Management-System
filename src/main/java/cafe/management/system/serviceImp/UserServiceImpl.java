package cafe.management.system.serviceImp;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.dao.UserDao;
import cafe.management.system.jwt.CustomUsersDetailsService;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.jwt.JWTUtil;
import cafe.management.system.model.User;
import cafe.management.system.service.UserService;
import cafe.management.system.util.CafeManagementSystemUtil;
import cafe.management.system.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserDao userDao;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	CustomUsersDetailsService customUsersDetailsService;

	@Autowired
	JWTUtil jwtUtil;

	@Autowired
	JWTFilter jwtFilter;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	private boolean validateSignUpMap(Map<String, String> requestMap) {
		log.info("Validating requestMap");
		return requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
				&& requestMap.containsKey("email") && requestMap.containsKey("password") ? true : false;
	}

	private User getUserFromMap(Map<String, String> requestMap) {
		User user = new User();
		user.setName(requestMap.get("name"));
		user.setEmail(requestMap.get("email"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setPassword(passwordEncoder.encode(requestMap.get("password")));
		user.setStatus("false");
		user.setRole("User");
		return user;
	}

	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		log.info("Inside signUp {}", requestMap);
		try {
			if (validateSignUpMap(requestMap)) {
				User user = this.userDao.findByEmail(requestMap.get("email"));
				if (Objects.isNull(user)) {
					this.userDao.save(getUserFromMap(requestMap));
					return CafeManagementSystemUtil.getResponseEntity(
							CafeManagementSystemConstant.USER_REGISTRATION_SUCCESSFUL, HttpStatus.OK);
				} else {
					return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.EMAIL_ALREADY_EXIST,
							HttpStatus.BAD_REQUEST);
				}
			} else {
				return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.INVALID_DATA,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
		log.info("Inside login {}", requestMap);
		try {
			log.info("Authenticating user {}", requestMap.get("email"));
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
			log.info("Authentication successful for user {}", requestMap.get("email"));

			if (auth.isAuthenticated()) {
				if (customUsersDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")) {
					String token = jwtUtil.generateToken(customUsersDetailsService.getUserDetails().getEmail(),
							customUsersDetailsService.getUserDetails().getRole());
					log.info("Generated JWT token for user {}", requestMap.get("email"));
					return new ResponseEntity<>(String.format("{\"token\":\"%s\"}", token), HttpStatus.OK);
				} else {
					return CafeManagementSystemUtil
							.getResponseEntity(CafeManagementSystemConstant.WAITING_FOR_ADMIN_APPROVAL, HttpStatus.OK);
				}
			} else {
				log.error("Authentication failed for user {}", requestMap.get("email"));
				return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BAD_CREDENTIAL,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Authentication exception for user {}", requestMap.get("email"), ex);
			return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BAD_CREDENTIAL,
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUsers() {
		try {
			if (jwtFilter.isAdmin()) {
				List<User> users = userDao.findAll();
				if (users.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				List<UserWrapper> userWrappers = users.stream().map(user -> new UserWrapper(user.getId(),
						user.getName(), user.getEmail(), user.getContactNumber(), user.getStatus()))
						.collect(Collectors.toList());
				return new ResponseEntity<List<UserWrapper>>(userWrappers, HttpStatus.OK);
			} else {
				return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
