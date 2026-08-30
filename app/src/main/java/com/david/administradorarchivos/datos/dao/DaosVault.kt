package com.david.administradorarchivos.datos.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.david.administradorarchivos.datos.entidades.ClaveEntidad
import com.david.administradorarchivos.datos.entidades.GrupoEntidad
import com.david.administradorarchivos.datos.entidades.HostEntidad
import com.david.administradorarchivos.datos.entidades.SnippetEntidad
import com.david.administradorarchivos.datos.entidades.TunelEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupos ORDER BY orden, nombre")
    fun observar(): Flow<List<GrupoEntidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(item: GrupoEntidad)

    @Delete
    suspend fun borrar(item: GrupoEntidad)
}

@Dao
interface HostDao {
    @Query("SELECT * FROM hosts ORDER BY alias")
    fun observar(): Flow<List<HostEntidad>>

    @Query("SELECT * FROM hosts WHERE id = :id")
    suspend fun porId(id: String): HostEntidad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(item: HostEntidad)

    @Query("DELETE FROM hosts WHERE id = :id")
    suspend fun borrar(id: String)
}

@Dao
interface ClaveDao {
    @Query("SELECT * FROM claves ORDER BY nombre")
    fun observar(): Flow<List<ClaveEntidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(item: ClaveEntidad)

    @Query("DELETE FROM claves WHERE id = :id")
    suspend fun borrar(id: String)
}

@Dao
interface TunelDao {
    @Query("SELECT * FROM tuneles WHERE hostId = :hostId")
    fun porHost(hostId: String): Flow<List<TunelEntidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(item: TunelEntidad)

    @Query("DELETE FROM tuneles WHERE id = :id")
    suspend fun borrar(id: String)
}

@Dao
interface SnippetDao {
    @Query("SELECT * FROM snippets ORDER BY nombre")
    fun observar(): Flow<List<SnippetEntidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(item: SnippetEntidad)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun borrar(id: String)
}
