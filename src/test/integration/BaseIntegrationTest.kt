import de.lamsal.esrepo.ElasticSearchConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.elasticsearch.ElasticsearchContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    protected val configuration: ElasticSearchConfiguration
    private val server: ElasticsearchContainer = ElasticsearchContainer(
        "docker.elastic.co/elasticsearch/elasticsearch:6.4.1"
    ).withExposedPorts(9200)

    init {
        server.start()

        this.configuration = ElasticSearchConfiguration(server.containerIpAddress, server.getMappedPort(9200))
    }

    @AfterAll
    fun teardown() {
        server.stop()
    }
}