package me.ingannatore.dddkata

import me.ingannatore.dddkata.dto.*
import me.ingannatore.dddkata.service.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDate

@SpringBootTest
@MockBean(value = [MailingListClient::class])
class IntegrationTest(
    @Autowired private val products: ProductService,
    @Autowired private val backlogItems: BacklogItemService,
    @Autowired private val sprints: SprintService,
    @Autowired private val releases: ReleaseService,
) {
    @MockBean
    private lateinit var emailSender: EmailSender

    @Test
    fun longWorkflow() {
        val productDto = ProductDto(
            code = "PNM",
            name = "::ProductName::",
            mailingList = "::MailList::",
            poEmail = "boss@corp.intra",
            poPhone = "123DONTCALLME",
            poName = "Za bo$$",
        )
        val productId: Long = products.createProduct(productDto)
        org.assertj.core.api.Assertions.assertThatThrownBy { products.createProduct(productDto) }
            .describedAs("cannot create with same code")
        assertThat(products.getProduct(productId))
            .extracting(ProductDto::code, ProductDto::name, ProductDto::mailingList)
            .isEqualTo(listOf("PNM", "::ProductName::", "::MailList::"))
        assertThat(products.getProduct(productId))
            .extracting(ProductDto::poName, ProductDto::poEmail, ProductDto::poPhone)
            .isEqualTo(listOf("Za bo$$", "boss@corp.intra", "123DONTCALLME"))
        val sprintId: Long = sprints.createSprint(
            CreateSprintRequest(
                productId = productId,
                plannedEnd = LocalDate.now().plusDays(14),
            )
        )
        assertThat(sprints.getSprint(sprintId))
            .matches { s -> s.iteration == 1 }
            .matches { s -> s.plannedEndDate.isAfter(LocalDate.now().plusDays(13)) }
        val backlogItemId: Long = backlogItems.createBacklogItem(
            BacklogItemDto(
                productId = productId,
                title = "::itemTitle::",
                description = "::itemDescription::",
            )
        )
        val backlogDto: BacklogItemDto = backlogItems.getBacklogItem(backlogItemId)
        backlogDto.description = backlogDto.description + "More Text"
        backlogItems.updateBacklogItem(backlogDto)
        val itemId: Long = sprints.addItem(
            sprintId,
            AddBacklogItemRequest(
                fpEstimation = 2,
                backlogId = backlogItemId,
            )
        )
        sprints.startSprint(sprintId)
        org.assertj.core.api.Assertions.assertThatThrownBy { sprints.startSprint(sprintId) }
            .describedAs("cannot start again")
        org.assertj.core.api.Assertions.assertThatThrownBy { sprints.completeItem(sprintId, itemId) }
            .describedAs("must first start item")
        sprints.startItem(sprintId, itemId)
        org.assertj.core.api.Assertions.assertThatThrownBy { sprints.startItem(sprintId, itemId) }
            .describedAs("cannot start again")
        sprints.logHours(sprintId, LogHoursRequest(itemId, 10))
        sprints.completeItem(sprintId, itemId)
        verify(emailSender).sendEmail(eq("happy@corp.intra"), any(), eq("Congrats!"), any())
        sprints.endSprint(sprintId)
        println("Metrics: " + sprints.getSprintMetrics(sprintId))
        assertThat(sprints.getSprintMetrics(sprintId))
            .extracting(
                SprintMetrics::consumedHours,
                SprintMetrics::doneFP,
                SprintMetrics::hoursConsumedForNotDone,
            )
            .containsExactly(10, 2, 0)
        val release = releases.createRelease(productId, sprintId)
        assertThat(release.releaseNotes).contains("::itemTitle::")
        assertThat(release.version).isEqualTo("1.0")

        // try to update a done backlog item
        val backlogDto2: BacklogItemDto = backlogItems.getBacklogItem(backlogItemId)
        backlogDto2.description = "IllegalChange"

        // TODO new feature: uncomment below: should fail
        // assertThatThrownBy(() -> backlogItems.updateBacklogItem(backlogDto2)).describedAs("cannot edit done item");
    }
}