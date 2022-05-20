package fi.tuni.tamk.weatherapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.tuni.tamk.weatherapp.ForecastWeatherData.ForecastItem
import java.text.SimpleDateFormat
import java.util.*

class ForecastRecyclerAdapter(list: List<ForecastItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var items = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("onCreateViewHolder:", "onCreateViewHolder")
        return ForecastViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.forecasts_list_item, parent, false)
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("binder:", "onBindViewHolder")
        when (holder) {
            is ForecastViewHolder -> {
                holder.bind(items[position])
                Log.d("binder:", items[position].toString())
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        Log.d("getCount in adapter", items.toString())
        return items.size
    }

    fun submitList(forecastItems: List<ForecastItem>) {
        this.items = forecastItems
    }

    class ForecastViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val iconView: ImageView = itemView.findViewById(R.id.forecastItemIcon)
        var iconString: String = ""
        val date: TextView = itemView.findViewById(R.id.forecastItemDate)
        val temp: TextView = itemView.findViewById(R.id.forecastItemTemp)
        val wind: TextView = itemView.findViewById(R.id.forecastItemWind)
        val conditions : TextView = itemView.findViewById(R.id.forecastItemWeather_conditions)

        fun bind(forecastItem: ForecastItem) {
            Log.d("forecastItem in bind()", forecastItem.toString())
            val tempString = forecastItem.main?.temp.toString() + " °C"
            val windString = forecastItem.wind?.speed.toString() + " m/s"
            date.text = forecastItem.dt?.let { setDate(it) }
            temp.text = tempString
            wind.text = windString
            conditions.text = forecastItem.weather?.get(0)?.main
            iconString = forecastItem.weather?.get(0)?.icon.toString()
            Picasso.get()
                .load("https://openweathermap.org/img/w/${iconString}.png")
                .into(iconView)
        }

        private fun setDate(timestamp: Long): String {
            val sdf = SimpleDateFormat(
                "dd.MM.yy, HH:mm",
                Locale("fi", "FI")
            )
            val date = Date(timestamp * 1000)
            return sdf.format(date)
        }
    }

}