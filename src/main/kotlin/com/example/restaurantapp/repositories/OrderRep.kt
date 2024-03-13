package com.example.restaurantapp.repositories

import com.example.restaurantapp.model.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Int>
