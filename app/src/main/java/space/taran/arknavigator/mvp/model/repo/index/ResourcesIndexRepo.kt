package space.taran.arknavigator.mvp.model.repo.index

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import space.taran.arknavigator.mvp.model.dao.ResourceDao
import space.taran.arknavigator.mvp.model.repo.FoldersRepo
import space.taran.arknavigator.mvp.model.repo.RootAndFav
import space.taran.arknavigator.mvp.model.repo.index.PlainResourcesIndex.Companion.listAllFiles
import space.taran.arknavigator.mvp.model.repo.index.PlainResourcesIndex.Companion.loadResources
import space.taran.arknavigator.mvp.model.repo.index.PlainResourcesIndex.Companion.scanResources
import space.taran.arknavigator.utils.RESOURCES_INDEX
import java.nio.file.Path
import kotlin.system.measureTimeMillis

class ResourcesIndexRepo(
    private val dao: ResourceDao,
    private val foldersRepo: FoldersRepo
) {
    private val provideMutex = Mutex()
    private val indexByRoot = mutableMapOf<Path, PlainResourcesIndex>()

    suspend fun loadFromDatabase(root: Path): PlainResourcesIndex = withContext(Dispatchers.IO) {
        Log.d(RESOURCES_INDEX, "loading index for $root from the database")

        val resources = dao.query(root.toString())

        Log.d(RESOURCES_INDEX, "${resources.size} resources retrieved from DB")

        val index = PlainResourcesIndex(root, dao, loadResources(resources))
        Log.d(RESOURCES_INDEX, "index created")

        index.reindexRoot(index.calculateDifference())
        indexByRoot[root] = index
        return@withContext index
    }

    suspend fun buildFromFilesystem(root: Path): PlainResourcesIndex = withContext(Dispatchers.IO) {
        Log.d(RESOURCES_INDEX, "building index from root $root")

        var files: List<Path>

        val time1 = measureTimeMillis {
            files = listAllFiles(root)
        }
        Log.d(RESOURCES_INDEX, "listed ${files.size} files in $time1 milliseconds")

        var metadata: Map<Path, ResourceMeta>

        val time2 = measureTimeMillis {
            metadata = scanResources(files)
        }
        Log.d(RESOURCES_INDEX, "resources metadata retrieved in $time2 milliseconds")

        val index = PlainResourcesIndex(root, dao, metadata)

        index.persistResources(index.metaByPath)
        indexByRoot[root] = index
        return@withContext index
    }

    suspend fun provide(rootAndFav: RootAndFav): ResourcesIndex = withContext(Dispatchers.IO) {
        val roots = foldersRepo.resolveRoots(rootAndFav)

        provideMutex.withLock {
            val indexShards = roots.map { root ->
                indexByRoot[root] ?: let {
                    val index = loadFromDatabase(root)
                    indexByRoot[root] = index
                    index
                }
            }

            return@withContext AggregatedResourcesIndex(indexShards)
        }
    }

    suspend fun provide(root: Path): ResourcesIndex = provide(RootAndFav(root.toString(), favString = null))

    suspend fun isIndexed(rootAndFav: RootAndFav): Boolean {
        val roots = foldersRepo.resolveRoots(rootAndFav)
        roots.forEach { root ->
            if (!indexByRoot.contains(root))
                return false
        }
        return true
    }
}