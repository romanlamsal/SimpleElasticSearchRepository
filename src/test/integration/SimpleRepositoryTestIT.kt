import de.lamsal.esrepo.api.QueryParams
import de.lamsal.esrepo.dsl.bool
import de.lamsal.esrepo.dsl.query
import de.lamsal.esrepo.dsl.term
import de.lamsal.esrepo.repository.SimpleRepository
import de.lamsal.esrepo.response.GetResponse
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SimpleRepositoryTestIT : BaseIntegrationTest() {
    private val savableEntity = Entity("some text", 1337, true, listOf(mapOf("foo" to "bar")))
    private val savableEntityChanged = Entity("", 0, false, emptyList())

    private val repository = SimpleRepository(
        Entity::class.java,
        index = "someindex",
        configuration = configuration
    )

    private lateinit var receivedId: String

    @Test
    @Order(1)
    fun `should save entity`() {
        // when
        receivedId = repository.save(savableEntity, null)

        // then
        assertNotNull(receivedId)
    }

    @Test
    @Order(2)
    fun `should be able to retreive entity again`() {
        // when
        val receivedEntity = repository.getById(receivedId)

        // then
        assertEquals(savableEntity, receivedEntity)
    }

    @Test
    @Order(3)
    fun `should be able to update entity by id`() {
        // given
        // savableEntityChanged

        // when
        repository.save(savableEntityChanged, receivedId)

        // then
        val receivedEntity = repository.getById(receivedId)
        assertEquals(savableEntityChanged, receivedEntity)
    }

    @Test
    @Order(4)
    fun `should be searchable with query and successfully paginate`() {
        // given
        // updated entity and savableEntity
        repository.save(savableEntity, null)
        val query = query {
            bool {
                should {
                    +term {
                        "number" to savableEntity.number
                    }

                    +term {
                        "number" to savableEntityChanged.number
                    }
                }
            }
        }
        repository.refresh()

        // when: query for the two entities present with size=1, which should result in actual paging of the results
        val searchResponse = repository.executeQuery(query.toString(), QueryParams(size = 1, scroll = "1m"))

        // then
        searchResponse.flatten().sortedBy { it._source.number } shouldEqual listOf(
            GetResponse(savableEntityChanged),
            GetResponse(savableEntity)
        )
    }

    data class Entity(val text: String, val number: Int, val boolean: Boolean, val list: List<Map<String, Any>>)
}