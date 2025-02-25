package space.taran.arknavigator.mvp.model.repo.index

import java.lang.AssertionError
import java.nio.file.Path

class AggregatedResourcesIndex(
    private val shards: Collection<PlainResourcesIndex>)
    : ResourcesIndex {

    override fun listResources(prefix: Path?): Set<ResourceMeta> =
        shards.flatMap { it.listResources(prefix) }
            .toSet()

    override fun getPath(id: ResourceId): Path =
        tryShards { it.tryGetPath(id) }

    override fun getMeta(id: ResourceId): ResourceMeta =
        tryShards { it.tryGetMeta(id) }

    override fun remove(id: ResourceId): Path =
        tryShards { it.tryRemove(id) }

    private fun <R>tryShards(f: (shard: PlainResourcesIndex) -> R?): R {
        shards.iterator()
            .forEach { shard ->
                val result = f(shard)
                if (result != null) {
                    return@tryShards result
                }
            }
        throw AssertionError("At least one of shards must yield success")
    }
}