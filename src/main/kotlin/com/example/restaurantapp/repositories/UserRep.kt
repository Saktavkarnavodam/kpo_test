package com.example.restaurantapp.repositories

import com.example.restaurantapp.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID>
