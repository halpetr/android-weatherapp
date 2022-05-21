package fi.tuni.tamk.weatherapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.tuni.tamk.weatherapp.forecast_weather_data.ForecastItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * ForecastRecyclerAdapter for RecyclerView
 *
 * @constructor Create ForecastRecyclerAdapter with List of ForecastItems as the data
 *
 * @param list List of ForecastItems
 */
class ForecastRecyclerAdapter(list: List<ForecastItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // List of ForecastItems
    private var items = list


    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ForecastViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.forecasts_list_item, parent, false)
        )
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the ViewHolder.itemView to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ForecastViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Inner class for creating ForecastViewHolder.
     *
     * @constructor Create ForecastViewHolder for each element of the List of ForecastItems.
     *
     * @param itemView ViewHolder.itemView
     */
    inner class ForecastViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Initialize all of the Views used by the ForecastViewHolder:
        val iconView: ImageView = itemView.findViewById(R.id.forecastItemIcon)
        val date: TextView = itemView.findViewById(R.id.forecastItemDate)
        val temp: TextView = itemView.findViewById(R.id.forecastItemTemp)
        val wind: TextView = itemView.findViewById(R.id.forecastItemWind)
        val conditions: TextView = itemView.findViewById(R.id.forecastItemWeather_conditions)

        // String for the icon:
        var iconString: String = ""

        /**
         * Bind all the values to the initialized views.
         *
         * @param forecastItem one item from the List of ForecastItems
         */
        fun bind(forecastItem: ForecastItem) {
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

        /**
         * Convert unix timestamp into date and time:
         *
         * @param timestamp Long unix timestamp
         * @return String containing the date and time in desired format
         */
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
// End of File