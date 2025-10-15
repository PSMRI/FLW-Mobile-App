import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

data class Ashas(
    var success: Boolean,
    var message: Any?,
    var `data`: List<Data>
) {
    data class Data(
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
        var agentId: Any?,
        var psmStatusId: Int,
        var psmStatus: String,
        var userServciceRoleDeleted: Boolean,
        var userDeleted: Boolean,
        var serviceProviderDeleted: Boolean,
        var roleDeleted: Boolean,
        var providerServiceMappingDeleted: Boolean,
        var blockid: Int,
        var blockname: String,
        var villageid: String,
        var villagename: String,
        var national: Boolean,
        var inbound: Any?,
        var outbound: Any?
    )
}