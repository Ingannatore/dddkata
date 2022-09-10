package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.entity.Release
import me.ingannatore.dddkata.entity.Sprint
import me.ingannatore.dddkata.repo.ProductRepository
import me.ingannatore.dddkata.repo.ReleaseRepository
import me.ingannatore.dddkata.repo.SprintRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.persistence.EntityNotFoundException

@Transactional
@RestController
class ReleaseService(
    private val releaseRepository: ReleaseRepository,
    private val productRepository: ProductRepository,
    private val sprintRepository: SprintRepository,
) {
    @PostMapping("product/{productId}/release/{sprintId}")
    fun createRelease(@PathVariable productId: Long, @PathVariable sprintId: Long): Release {
        val product = productRepository.findById(productId).orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id " + productId) }
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        val previouslyReleasedIteration = product.releases
            .map { it.sprint }
            .maxOfOrNull { it.iteration } ?: 0
        val releasedIteration = sprint.iteration
        val releasedItems = product.sprints
            .sortedBy { it.iteration }
            .filter { s -> (s.iteration in (previouslyReleasedIteration + 1)..releasedIteration) }
            .flatMap { s -> s.items }
        val release = Release(
            product = product,
            sprint = sprint,
            releasedItems = releasedItems,
            date = LocalDate.now(),
            version = "${product.incrementAndGetVersion()}.0"
        )
        product.releases.add(release)
        releaseRepository.save(release)
        return release
    }
}
