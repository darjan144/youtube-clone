package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.RegistrationDTO;
import isa.vezbe1.spring_boot_example.dto.UserDTO;
import isa.vezbe1.spring_boot_example.model.Address;
import isa.vezbe1.spring_boot_example.model.Role;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.repository.RoleRepository;
import isa.vezbe1.spring_boot_example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    @Transactional
    public UserDTO registerUser(RegistrationDTO registrationDTO) {

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Address address = new Address();
        address.setStreet(registrationDTO.getStreet());
        address.setCity(registrationDTO.getCity());
        address.setCountry(registrationDTO.getCountry());
        address.setPostalCode(registrationDTO.getPostalCode());

        User user = new User(
                registrationDTO.getUsername(),
                passwordEncoder.encode(registrationDTO.getPassword()),
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getEmail()
        );

        user.setAddress(address);
        user.setEnabled(false); // Not enabled until email activation

        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);

        Timestamp expiryDate = new Timestamp(System.currentTimeMillis() + 86400000); // 24 hours
        user.setTokenExpiryDate(expiryDate);

        Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        emailService.sendActivationEmail(savedUser.getEmail(), activationToken);

        return new UserDTO(savedUser);
    }


    @Transactional
    public void activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if (user.getTokenExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            throw new RuntimeException("Activation token has expired");
        }

        user.setEnabled(true);
        user.setActivationToken(null);
        user.setTokenExpiryDate(null);

        userRepository.save(user);
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }


    public UserDTO getUserById(Long id) {
        User user = findById(id);
        return new UserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .toList();
    }

    @Transactional
    public UserDTO updateUser(Long userId, User currentUser) {
        User user = findById(userId);

        if (!user.getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You don't have permission to update this user");
        }

        User updatedUser = userRepository.save(user);
        return new UserDTO(updatedUser);
    }
}