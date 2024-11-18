package cafe.management.system.serviceImp;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.dao.UserDao;
import cafe.management.system.jwt.CustomUsersDetailsService;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.jwt.JWTUtil;
import cafe.management.system.model.User;
import cafe.management.system.service.UserService;
import cafe.management.system.util.CafeManagementSystemUtil;
import cafe.management.system.util.EmailUtil;
import cafe.management.system.util.PasswordGenerator;
import cafe.management.system.util.PasswordValidator;
import cafe.management.system.wrapper.UserWrapper;
import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
	EmailUtil emailUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private PasswordGenerator passwordGenerator;

	@Autowired
	private PasswordValidator passwordValidator;

	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		log.info("Inside signUp {}", requestMap);
		try {
			if (validateSignUpMap(requestMap)) {
				User user = this.userDao.findByEmail(requestMap.get("email"));
				if (Objects.isNull(user)) {
					if(passwordValidator.isValidPassword(requestMap.get("password"))){
						this.userDao.save(getUserFromMap(requestMap));
						return CafeManagementSystemUtil.getResponseEntity(
								CafeManagementSystemConstant.USER_REGISTRATION_SUCCESSFUL, HttpStatus.OK);
					}
					else {
						return CafeManagementSystemUtil.getResponseEntity(
								CafeManagementSystemConstant.STRONG_PASSWORD_ERROR, HttpStatus.BAD_REQUEST);
					}
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
	public ResponseEntity<List<UserWrapper>> getAllUser() {
		try {
			if (jwtFilter.isAdmin()) {
				List<UserWrapper> users = userDao.findAllUser();
				if (users.isEmpty()) {
					return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NO_CONTENT);
				}
				return new ResponseEntity<List<UserWrapper>>(users, HttpStatus.OK);
			} else {
				return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				Optional<User> optionalUser = userDao.findById(Integer.parseInt(requestMap.get("id")));
				if (!optionalUser.isEmpty()) {
					userDao.updateStatus(Integer.parseInt(requestMap.get("id")), requestMap.get("status"));
					sendMailtoAllAdmin(requestMap.get("status"), optionalUser.get().getEmail(), userDao.findAllAdmin());
					return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_STATUS_UPDATED,
							HttpStatus.OK);
				} else {
					return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_DOES_NOT_EXIST,
							HttpStatus.UNAUTHORIZED);
				}
			} else {
				return CafeManagementSystemUtil.getResponseEntity(
						CafeManagementSystemConstant.USER_NOT_ALLOWED_TO_ACCESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}


	private void sendMailtoAllAdmin(String status, String userEmail, List<String> allAdmin) {
		allAdmin.remove(jwtFilter.getCurrent());
		if (status != null && status.equalsIgnoreCase("true")) {
			emailUtil.approvalMail(userEmail, CafeManagementSystemConstant.ADMIN_APPROVED_ACCOUNT_SUBJECT,jwtFilter.getCurrent());
		} else {
			emailUtil.disableMail(userEmail, CafeManagementSystemConstant.ADMIN_DISABLED_ACCOUNT_SUBJECT,jwtFilter.getCurrent());
		}
	}

	@Override
	public ResponseEntity<String> checkToken() {
		return CafeManagementSystemUtil.getResponseEntity("true",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User user = userDao.findByEmail(jwtFilter.getCurrent());
			if(!Objects.isNull(user)) {
				log.info("Checking old password for user {}", user.getEmail());
				if (passwordEncoder.matches(requestMap.get("oldPassword"),user.getPassword())) {
					log.info("Changing password for user {}", user.getEmail());
					if(passwordValidator.isValidPassword(requestMap.get("newPassword"))){
						user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
					}
					else {
						return CafeManagementSystemUtil.getResponseEntity(
								CafeManagementSystemConstant.STRONG_PASSWORD_ERROR, HttpStatus.BAD_REQUEST);
					}
                    userDao.save(user);
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PASSWORD_CHANGED,
                            HttpStatus.OK);
				}
				return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.INCORRECT_OLD_PASSWORD,
                        HttpStatus.BAD_REQUEST);
			}
			return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (Exception ex) {
            ex.printStackTrace();
        }
		return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try {
			User user = userDao.findByEmail(requestMap.get("email"));
			if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
				log.info("Forgot password for user {}", user.getEmail());
				String randomPassword = passwordGenerator.generateRandomPassword();
				if(passwordValidator.isValidPassword(randomPassword)){
					user.setPassword(passwordEncoder.encode(randomPassword));
				}
				else {
					return CafeManagementSystemUtil.getResponseEntity(
							CafeManagementSystemConstant.STRONG_PASSWORD_ERROR, HttpStatus.BAD_REQUEST);
				}
				userDao.save(user);
				emailUtil.forgotPasswordMail(user.getEmail(), CafeManagementSystemConstant.FORGOT_PASSWORD_MAIL_SUBJECT,randomPassword);
			}
			return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.FORGOT_PASSWORD,
					HttpStatus.OK);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
