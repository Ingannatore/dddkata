package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.dto.ProductDto
import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.repo.ProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("products")
class ProductService(
    private val productRepository: ProductRepository,
) {
    @PostMapping
    fun createProduct(@RequestBody dto: ProductDto): Long {
        require(!productRepository.existsByCode(dto.code)) { "Code already defined" }

        val product = Product(
            code = dto.code,
            name = dto.name,
            teamMailingList = dto.mailingList,
            ownerEmail = dto.poEmail,
            ownerName = dto.poName,
            ownerPhone = dto.poPhone,
        )

        return productRepository.save(product).id!!
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ProductDto {
        val product = productRepository.findById(id).orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id " + id) }

        return ProductDto(
            id = product.id!!,
            code = product.code,
            name = product.name,
            mailingList = product.teamMailingList,
            poEmail = product.ownerEmail,
            poName = product.ownerName,
            poPhone = product.ownerPhone,
        )
    }
}
