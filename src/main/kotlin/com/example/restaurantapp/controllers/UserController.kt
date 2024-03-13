package com.example.restaurantapp.controllers

import com.example.restaurantapp.model.Dish
import com.example.restaurantapp.model.User
import com.example.restaurantapp.repositories.UserRepository
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

interface UserController {
    @PostMapping("/registerUser")
    fun registerUser(
        @RequestParam newUsername: String,
        @RequestParam newPassword: String,
        @RequestParam userType: String
    ): String

    @GetMapping("/authenticateUser")
    fun authenticateUser(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam userType: String
    ): String

    @DeleteMapping("/logoutUser")
    fun logoutUser(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam userType: String
    ): String

    @GetMapping("/getAllDishes")
    fun getAllDishes(): MutableList<Any?>?

    @GetMapping("/getCurrentUser")
    fun getCurrentUser(): Any
}

@RestController
@RequestMapping("/api/user")
class UserControllerImpl(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val clientController: ClientControllerImpl,
    @Autowired private val adminController: AdminControllerImpl,
) : UserController {

    @Autowired
    private lateinit var entityManager: EntityManager
    private var currentUser: User? = null

    @PostMapping("/registerUser")
    override fun registerUser(
        @RequestParam newUsername: String,
        @RequestParam newPassword: String,
        @RequestParam userType: String
    ): String {
        if (userType != "admin" && userType != "client") {
            return "Invalid user type. Only 'admin' or 'client' allowed."
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM users WHERE account_name = '$newUsername' AND role = '$userType'",
            User::class.java
        )
        if (query.resultList.isNotEmpty()) {
            return "User with this username and type already exists"
        }
        val newUser = User(UUID.randomUUID(), newUsername, newPassword, userType)
        try {
            userRepository.save(newUser)
        } catch (e: Exception) {
            return "Failed to register user. ${e.message}"
        }
        println("User $newUsername registered.")
        return "Registered Successfully."
    }

    @GetMapping("/authenticateUser")
    override fun authenticateUser(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam userType: String
    ): String {
        if (userType != "admin" && userType != "client") {
            return "Invalid user type. Only 'admin' or 'client' allowed."
        }
        if (currentUser != null && currentUser!!.accountName == username) {
            return "You are already logged in."
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM users WHERE account_name = '$username' AND secret = '$password' AND role = '$userType'",
            User::class.java
        )
        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "User not found or invalid credentials."
        }
        currentUser = resultList.first() as User
        if (currentUser!!.role == "client") {
            clientController.currClient = currentUser
            adminController.managementUser = null
        } else {
            adminController.managementUser = currentUser
            clientController.currClient = null
        }
        println("User $username logged in.")
        return "User logged in successfully."
    }

    @DeleteMapping("/logoutUser")
    override fun logoutUser(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam userType: String
    ): String {
        if (userType != "admin" && userType != "client") {
            return "Invalid user type. Only 'admin' or 'client' allowed."
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM users WHERE account_name = '$username' AND secret = '$password' AND role = '$userType'",
            User::class.java
        )
        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "User not found or invalid credentials."
        }
        currentUser = resultList.first() as User
        if (currentUser!!.role == "client") {
            clientController.currClient = null
        } else {
            adminController.managementUser = null
        }
        currentUser = null
        println("User $username logged out.")
        return "User logged out successfully."
    }

    @GetMapping("/getAllDishes")
    override fun getAllDishes(): MutableList<Any?>? {
        val query = entityManager.createNativeQuery("SELECT * FROM menu", Dish::class.java)
        return query.resultList
    }

    @GetMapping("/getCurrentUser")
    override fun getCurrentUser(): Any {
        return if (currentUser == null) {
            "No logged user"
        } else {
            currentUser!!
        }
    }
}
