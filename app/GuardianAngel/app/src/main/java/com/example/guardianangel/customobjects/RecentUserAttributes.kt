import com.google.gson.annotations.SerializedName

data class RecentUserAttributes(
    @SerializedName("user_attributes")
    val userAttributes: List<HealthData>
)

data class HealthData(
    @SerializedName("blood_oxygen")
    val bloodOxygen: Int,

    @SerializedName("calories_burnt")
    val caloriesBurnt: Int,

    @SerializedName("heart_rate")
    val heartRate: Int,

    @SerializedName("id")
    val id: String,

    @SerializedName("respiratory_rate")
    val respiratoryRate: Int,

    @SerializedName("sleep")
    val sleep: Int,

    @SerializedName("steps_count")
    val stepsCount: Int,

    @SerializedName("timestamp")
    val timestamp: String
)
