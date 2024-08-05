package com.example.gestionequipos.ui.partediario

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.gestionequipos.data.ListarPartesDiarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import java.io.IOException
import java.net.URL

class ParteDiarioPagingSource(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val equipo: String,
    private val fechaInicio: String,
    private val fechaFin: String
) : PagingSource<Int, ListarPartesDiarios>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListarPartesDiarios> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val url = URL("$baseUrl/get_partes_diarios.php?page=$page&pageSize=$pageSize&equipo=$equipo&fechaInicio=$fechaInicio&fechaFin=$fechaFin")

            val request = Request.Builder()
                .url(url)
                .build()
            Log.d("ParteDiarioPagingSource", "Request URL: $url")

            // Logging
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val clientWithLogging = client.newBuilder()
                .addInterceptor(logging)
                .build()

            val response = withContext(Dispatchers.IO) {
                clientWithLogging.newCall(request).execute()
            }

            val jsonData = response.body?.string() ?: ""
            val jsonArray = JSONArray(jsonData)

            Log.d("ParteDiarioPagingSource", "Response: $response")
            Log.d("ParteDiarioPagingSource", "jsonArray: $jsonArray")
            val partesDiarios = mutableListOf<ListarPartesDiarios>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val parteDiario = ListarPartesDiarios(
                    id_parte_diario = jsonObject.getInt("id_parte_diario"),
                    fecha = jsonObject.getString("fecha"),
                    equipo_id = jsonObject.getInt("equipo_id"),
                    interno = jsonObject.getString("interno"),
                    horas_inicio = jsonObject.getInt("horas_inicio"),
                    horas_fin = jsonObject.getInt("horas_fin"),
                    horas_trabajadas = jsonObject.getInt("horas_trabajadas"),
                    observaciones = jsonObject.optString("observaciones"),
                    obra_id = jsonObject.getInt("obra_id"),
                    user_created = jsonObject.getInt("user_created"),
                    estado_id = jsonObject.getInt("estado_id")
                )
                partesDiarios.add(parteDiario)
            }

            LoadResult.Page(
                data = partesDiarios,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (partesDiarios.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            Log.e("ParteDiarioPagingSource", "IOException: ${e.message}", e)
            LoadResult.Error(e)
        } catch (e: Exception) {
            Log.e("ParteDiarioPagingSource", "Exception: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListarPartesDiarios>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
