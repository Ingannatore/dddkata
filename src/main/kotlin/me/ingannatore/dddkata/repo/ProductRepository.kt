package me.ingannatore.dddkata.repo

import me.ingannatore.dddkata.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun existsByCode(code: String?): Boolean
}
