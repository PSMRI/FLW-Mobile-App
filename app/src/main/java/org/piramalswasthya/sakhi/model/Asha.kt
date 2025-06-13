import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "ASHA")
data class AshaCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var userId: Int,
    var usrMappingId: Int,
    var name: String,
    var userName: String,
    var serviceId: Int,
    var serviceName: String,
    var stateId: Int,
    var stateName: String,
    var workingDistrictId: Int?,
    var workingDistrictName: String?,
    var workingLocationId: Int?,
    var serviceProviderId: Int,
    var locationName: String?,
    var workingLocationAddress: String?,
    var roleId: Int,
    var roleName: String,
    var providerServiceMapId: Int,
    var blockid: Int,
    var blockname: String,
    var villageid: String,
    var villagename: String
)

@JsonClass(generateAdapter = true)
data class AshaNetwork(
    var userId: Int = 0,
    var usrMappingId: Int = 0,
    var name: String = "",
    var userName: String = "",
    var serviceId: Int = 0,
    var serviceName: String = "",
    var stateId: Int = 0,
    var stateName: String = "",
    var workingDistrictId: Int? = 0,
    var workingDistrictName: String? = "",
    var workingLocationId: Int? = 0,
    var serviceProviderId: Int = 0,
    var locationName: String? = "",
    var workingLocationAddress: String? = "",
    var roleId: Int = 0,
    var roleName: String = "",
    var providerServiceMapId: Int = 0,
    var agentId: Any?,
    var psmStatusId: Int = 0,
    var psmStatus: String = "",
    var userServciceRoleDeleted: Boolean,
    var userDeleted: Boolean,
    var serviceProviderDeleted: Boolean,
    var roleDeleted: Boolean,
    var providerServiceMappingDeleted: Boolean,
    var blockid: Int = 0,
    var blockname: String = "",
    var villageid: String = "",
    var villagename: String = "",
    var national: Boolean,
    var inbound: Any?,
    var outbound: Any?,
) {
    fun asCacheModel(): AshaCache {
        return AshaCache(
        userId = userId,
        usrMappingId = usrMappingId,
        name = name,
        userName = userName,
        serviceId = serviceId,
        serviceName = serviceName,
        stateId = stateId,
        stateName = stateName,
        workingDistrictId = workingDistrictId,
        workingDistrictName = workingDistrictName,
        workingLocationId = workingLocationId,
        serviceProviderId = serviceProviderId,
        locationName = locationName,
        workingLocationAddress = workingLocationAddress,
        roleId = roleId,
        roleName = roleName,
        providerServiceMapId = providerServiceMapId,
        blockid = blockid,
        blockname = blockname,
        villageid = villageid,
        villagename = villagename,
        )
    }
}


@JsonClass(generateAdapter = true)
data class AshaListResponse(
    val data: AshaNetwork,
    val statusCode: Int,
    val status: String
)