package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.dto.BacklogItemDto
import me.ingannatore.dddkata.entity.BacklogItem
import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.repo.BacklogItemRepository
import me.ingannatore.dddkata.repo.ProductRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("backlog")
class BacklogItemService(
    private val backlogItemRepository: BacklogItemRepository,
    private val productRepository: ProductRepository,
) {
    @PostMapping
    @Transactional
    fun createBacklogItem(@RequestBody dto: BacklogItemDto): Long {
        val product = productRepository.findById(dto.productId).orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id " + dto.productId) }
        val backlogItem = BacklogItem(
            product = product,
            title = dto.title,
            description = dto.description,
        )

        return backlogItemRepository.save(backlogItem).id!!
    }

    @GetMapping("/{id}")
    fun getBacklogItem(@PathVariable id: Long): BacklogItemDto {
        val backlogItem = backlogItemRepository.findById(id).orElseThrow { EntityNotFoundException("No ${BacklogItem::class.simpleName} with id " + id) }
        return BacklogItemDto(
            id = backlogItem.id!!,
            productId = backlogItem.product.id!!,
            title = backlogItem.title,
            description = backlogItem.description,
            version = backlogItem.version,
        )
    }

    @PutMapping
    fun updateBacklogItem(@RequestBody dto: BacklogItemDto) {
        // TODO if Backlog Item is COMPLETED, reject the update
        val product = productRepository.findById(dto.productId).orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id " + dto.productId) }
        val backlogItem = BacklogItem(
            id = dto.id,
            product = product,
            title = dto.title,
            description = dto.description,
            version = dto.version,
        )

        backlogItemRepository.save(backlogItem)
    }

    @DeleteMapping("/{id}")
    fun deleteBacklogItem(@PathVariable id: Long) {
        backlogItemRepository.deleteById(id)
    }
}
