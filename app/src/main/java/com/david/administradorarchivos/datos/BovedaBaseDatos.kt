package com.david.administradorarchivos.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.david.administradorarchivos.datos.dao.ClaveDao
import com.david.administradorarchivos.datos.dao.GrupoDao
import com.david.administradorarchivos.datos.dao.HostDao
import com.david.administradorarchivos.datos.dao.SnippetDao
import com.david.administradorarchivos.datos.dao.TunelDao
import com.david.administradorarchivos.datos.entidades.ClaveEntidad
import com.david.administradorarchivos.datos.entidades.GrupoEntidad
import com.david.administradorarchivos.datos.entidades.HostEntidad
import com.david.administradorarchivos.datos.entidades.SnippetEntidad
import com.david.administradorarchivos.datos.entidades.TunelEntidad

@Database(
    entities = [
        GrupoEntidad::class,
        HostEntidad::class,
        ClaveEntidad::class,
        TunelEntidad::class,
        SnippetEntidad::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BovedaBaseDatos : RoomDatabase() {
    abstract fun grupos(): GrupoDao
    abstract fun hosts(): HostDao
    abstract fun claves(): ClaveDao
    abstract fun tuneles(): TunelDao
    abstract fun snippets(): SnippetDao

    companion object {
        @Volatile private var instancia: BovedaBaseDatos? = null

        fun obtener(ctx: Context): BovedaBaseDatos =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    BovedaBaseDatos::class.java,
                    "sesiones_boveda.db"
                ).fallbackToDestructiveMigration().build().also { instancia = it }
            }
    }
}
