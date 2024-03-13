package com.example.restaurantapp.controllers

import com.example.restaurantapp.model.Dish
import com.example.restaurantapp.model.User
import com.example.restaurantapp.repositories.MenuRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

interface AdminController {
    @PostMapping("/publishDish")
    fun publishDish(
        @RequestParam dishTitle: String,
        @RequestParam summary: String,
        @RequestParam price: BigDecimal,
        @RequestParam preparationDuration: Int
    ): String

    @DeleteMapping("/discardDish")
    fun discardDish(
        @RequestParam dishTitle: String,
        @RequestParam summary: String,
        @RequestParam price: BigDecimal,
        @RequestParam preparationDuration: Int
    ): String

    @GetMapping("/calculateEarnings")
    fun calculateEarnings(): BigDecimal

    @GetMapping("/listTopDishs")
    fun listTopDishs(): List<String>

    var managementUser: User?
}

@RestController
@RequestMapping("/api/management")
class AdminControllerImpl(
    @Autowired val menuRepo: MenuRepository
) : AdminController {
    override var managementUser: User? = null

    @PersistenceContext
    private lateinit var em: EntityManager

    @PostMapping("/publishDish")
    override fun publishDish(
        @RequestParam dishTitle: String,
        @RequestParam summary: String,
        @RequestParam price: BigDecimal,
        @RequestParam preparationDuration: Int
    ): String {
        if (managementUser == null) {
            return "Administrator not signed in"
        }
        val dish = Dish(
            id = 0,
            title = dishTitle,
            summary = summary,
            cost = price,
            preparationTime = preparationDuration,
            createdBy = managementUser!!.accountName
        )
        val findDish = menuRepo.findDishByTitleAndSummaryAndCostAndPreparationTime(dishTitle, summary, price, preparationDuration)
        if (findDish.isNullOrEmpty()) {
            return try {
                menuRepo.save(dish)
                "Meal successfully added to the menu"
            } catch (e: Exception) {
                "Failed to add meal"
            }
        } else {
            return "$dishTitle is already exist"
        }
    }

    @DeleteMapping("/discardDish")
    @Transactional
    override fun discardDish(dishTitle: String, summary: String, price: BigDecimal, preparationDuration: Int): String {
        if (managementUser == null) return "Administrator not signed in"

        val dish = menuRepo.findDishByTitleAndSummaryAndCostAndPreparationTime(dishTitle, summary, price, preparationDuration)
        return if (!dish.isNullOrEmpty()) {
            menuRepo.delete(dish.first())
            "Meal removed from the menu"
        } else {
            "Meal not found"
        }
    }

    @GetMapping("/calculateEarnings")
    override fun calculateEarnings(): BigDecimal {
        if (managementUser == null) {
            return BigDecimal.ZERO
        }
        val query = em.createNativeQuery("SELECT SUM(total_cost) FROM orders")
        return query.singleResult as BigDecimal? ?: BigDecimal.ZERO
    }

    @GetMapping("/listTopDishs")
    override fun listTopDishs(): List<String> {
        if (managementUser == null) {
            return emptyList()
        }
        var query =
            em.createNativeQuery("SELECT dish_ids FROM order_dish_ids GROUP BY dish_ids ORDER BY COUNT(*) DESC")
        val dishIds = query.resultList as List<*>
        val ans: MutableList<String> = mutableListOf()
        for (id in dishIds) {
            query = em.createNativeQuery("SELECT * FROM menu WHERE id = '$id'", Dish::class.java)
            val dish = query.singleResult as Dish
            ans.add(dish.title)
        }
        return ans
    }
}
