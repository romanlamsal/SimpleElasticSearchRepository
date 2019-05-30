import de.lamsal.esrepo.repository.SimpleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SimpleRepositoryTestIT : BaseIntegrationTest() {
    private val savableEntity = Entity("some text", 1337, true, listOf(mapOf("foo" to "bar")))

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
        val savableEntityChanged = Entity("", 0, false, emptyList())

        // when
        repository.save(savableEntityChanged, receivedId)

        // then
        val receivedEntity = repository.getById(receivedId)
        assertEquals(savableEntityChanged, receivedEntity)
    }

    data class Entity(val text: String, val number: Int, val boolean: Boolean, val list: List<Map<String, Any>>)
}