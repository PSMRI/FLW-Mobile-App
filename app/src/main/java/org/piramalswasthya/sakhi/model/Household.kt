package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.configuration.FormDataModel


data class HouseholdFamily(
    var familyHeadName: String? = null,
    var familyName: String? = null,
    var familyHeadPhoneNo: Long? = null,
    var houseNo: String? = null,
    var wardNo: String? = null,
    var wardName: String? = null,
    var mohallaName: String? = null,
    var rationCardDetails: String? = null,
    var povertyLine: String? = null,
    var povertyLineId: Int = 0,
)

data class HouseholdDetails(
    //household Details
    var residentialArea: String? = null,
    var residentialAreaId: Int = 0,
    var otherResidentialArea: String? = null,

    var houseType: String? = null,
    var houseTypeId: Int = 0,
    var otherHouseType: String? = null,

    var isHouseOwned: String? = null,
    var isHouseOwnedId: Int = 0,

    var isLandOwned: Boolean? = null,
    var isLandIrrigated: Boolean? = null,
    var isLivestockOwned: Boolean? = null,
    var street: String = "null",
    var colony: String = "null",
    var pincode: Int = 0,
)

data class HouseholdAmenities(
    //HH Amenities
    var separateKitchen: String? = null, var separateKitchenId: Int = 0,
    var fuelUsed: String? = null, var fuelUsedId: Int = 0,
    var otherFuelUsed: String? = null,
    var sourceOfDrinkingWater: String? = null, var sourceOfDrinkingWaterId: Int = 0,
    var otherSourceOfDrinkingWater: String? = null,
    var availabilityOfElectricity: String? = null, var availabilityOfElectricityId: Int = 0,
    var otherAvailabilityOfElectricity: String? = null,
    var availabilityOfToilet: String? = null, var availabilityOfToiletId: Int = 0,
    var otherAvailabilityOfToilet: String? = null,
    var motorizedVehicle: String? = null, var motorizedVehicleId: Int = 0,
    var otherMotorizedVehicle: String? = null,
)

@Entity(tableName = "HOUSEHOLD")
data class HouseholdCache(

    @PrimaryKey var householdId: Long,
    @ColumnInfo(index = true) var ashaId: Int,
    var benId: Long? = null,
    @Embedded(prefix = "fam_") var family: HouseholdFamily? = null,
    @Embedded(prefix = "det_") var details: HouseholdDetails? = null,
    @Embedded(prefix = "amn_") var amenities: HouseholdAmenities? = null,
    @Embedded(prefix = "loc_") val locationRecord: LocationRecord,
    //Spam
    var registrationType: String? = null,
    var serverUpdatedStatus: Int = 0,
    var createdBy: String? = null,
    var createdTimeStamp: Long? = null,
    var updatedBy: String? = null,
    var updatedTimeStamp: Long? = null,
    var processed: String,
    var isDraft: Boolean,
    val isDeactivate: Boolean =false
) : FormDataModel {


    fun asNetworkModel(user: User): HouseholdNetwork {
        return HouseholdNetwork(
            Countyid = locationRecord.country.id,
            Processed = processed,
            providerServiceMapID = user.serviceMapId,
//            VanID = userCache.vanId,
            ashaId = ashaId,
            availabilityOfToilet = amenities?.availabilityOfToilet,
            availabilityofToiletId = amenities?.availabilityOfToiletId ?: 0,
            availabilityOfElectricity = amenities?.availabilityOfElectricity,
            avalabilityofElectricityId = amenities?.availabilityOfElectricityId ?: 0,
            blockid = locationRecord.block.id,
            povertyLineId = family?.povertyLineId ?: 0,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdTimeStamp),
            districtid = locationRecord.district.id,
            familyHeadName = family?.familyHeadName,
            familyHeadPhoneNo = family?.familyHeadPhoneNo.toString(),
            fuelUsed = amenities?.fuelUsed,
            fuelUsedId = amenities?.fuelUsedId ?: 0,
            isHouseOwned = details?.isHouseOwned,
            houseOwnerShipId = details?.isHouseOwnedId ?: 0,
            houseType = details?.houseType,
            houseTypeId = details?.houseTypeId ?: 0,
            houseNo = family?.houseNo ?: "null",
            householdId = householdId.toString(),
            otherAvailabilityOfToilet = amenities?.otherAvailabilityOfToilet ?: "null",
            otherAvailabilityOfElectricity = amenities?.otherAvailabilityOfElectricity ?: "null",
            otherFuelUsed = amenities?.otherFuelUsed ?: "",
            otherHouseType = details?.otherHouseType ?: "",
            otherMotorizedVehicle = amenities?.otherMotorizedVehicle ?: "null",
            otherResidentialArea = details?.otherResidentialArea ?: "",
            otherSourceOfDrinkingWater = amenities?.otherSourceOfDrinkingWater ?: "null",
            residentialArea = details?.residentialArea ?: "null",
            residentialAreaId = details?.residentialAreaId ?: 0,
            separateKitchen = amenities?.separateKitchen,
            seperateKitchenId = amenities?.separateKitchenId ?: 0,
            serverUpdatedStatus = serverUpdatedStatus,
            sourceOfDrinkingWater = amenities?.sourceOfDrinkingWater,
            sourceofDrinkingWaterId = amenities?.sourceOfDrinkingWaterId ?: 0,
            state = locationRecord.state.name,
            stateid = locationRecord.state.id,
            povertyLine = family?.povertyLine,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedTimeStamp),
            village = locationRecord.village.name,
            villageid = locationRecord.village.id,
            familyName = family?.familyName,

            wardNo = family?.wardNo,
            wardName = family?.wardName,
            mohallaName = family?.mohallaName,
            rationCardDetails = family?.rationCardDetails,
            districtname = locationRecord.district.name,
            isDeactivate = isDeactivate
        )

    }
}


@JsonClass(generateAdapter = true)
data class HouseholdNetwork(
    @Json(name = "houseoldId") val householdId: String,
    @Json(name = "ashaid") val ashaId: Int,
    @Json(name = "benficieryid") val benId: Long = 0,
    @Json(name = "id") val dummyIdMayBe: Int = 1,
    //FamilyDetails

    val familyHeadName: String? = null,

    val familyName: String? = null,

    val familyHeadPhoneNo: String,
    @Json(name = "houseno") val houseNo: String,

    val wardNo: String? = null,

    val wardName: String? = null,

    val mohallaName: String? = null,

    val rationCardDetails: String? = null,
    @Json(name = "type_bpl_apl") val povertyLine: String? = null,
    @Json(name = "bpl_aplId") val povertyLineId: Int = 0,
    //household Details
    val residentialArea: String,

    @Json(name = "residentialAreaId") val residentialAreaId: Int = 0,
    @Json(name = "other_residentialArea") val otherResidentialArea: String,

    val houseType: String? = null,
    @Json(name = "houseTypeId") val houseTypeId: Int = 0,

    @Json(name = "other_houseType") val otherHouseType: String? = null,

    @Json(name = "houseOwnerShip") val isHouseOwned: String? = null,
    @Json(name = "houseOwnerShipId") val houseOwnerShipId: Int = 0,

    @Json(name = "landOwned") val isLandOwned: String = "< 2acres",
    @Json(name = "landOwnedId") val landOwnedId: Int = 0,

    @Json(name = "landIrregated") val landIrregated: String = "None",
    @Json(name = "landIrregatedId") val landIrregatedId: Int = 0,

    @Json(name = "liveStockOwnerShip") val isLivestockOwned: String = "No",
    @Json(name = "liveStockOwnerShipId") val liveStockOwnerShipId: Int = 0,

    @Json(name = "Street") val street: String = "null",

    @Json(name = "Colony") val colony: String = "null",

    @Json(name = "Pincode") val pincode: Int = 0,

    //HH Amenities
    @Json(name = "seperateKitchen") val separateKitchen: String? = null,
    @Json(name = "seperateKitchenId") val seperateKitchenId: Int = 0,

    val fuelUsed: String? = null,
    @Json(name = "fuelUsedId") val fuelUsedId: Int = 0,
    @Json(name = "other_fuelUsed") val otherFuelUsed: String,

    @Json(name = "sourceofDrinkingWater") val sourceOfDrinkingWater: String? = null,
    @Json(name = "sourceofDrinkingWaterId") val sourceofDrinkingWaterId: Int = 0,
    @Json(name = "other_sourceofDrinkingWater") val otherSourceOfDrinkingWater: String,

    @Json(name = "avalabilityofElectricity") val availabilityOfElectricity: String? = null,
    @Json(name = "avalabilityofElectricityId") val avalabilityofElectricityId: Int = 0,
    @Json(name = "other_avalabilityofElectricity") val otherAvailabilityOfElectricity: String,

    @Json(name = "availabilityofToilet") val availabilityOfToilet: String? = null,
    @Json(name = "availabilityofToiletId") val availabilityofToiletId: Int = 0,
    @Json(name = "other_availabilityofToilet") val otherAvailabilityOfToilet: String? = null,
    @Json(name = "motarizedVehicle") val motorizedVehicle: String = "Motor Bike",
    @Json(name = "motarizedVehicleId") val motarizedVehicleId: Int = 0,
    @Json(name = "other_motarizedVehicle") val otherMotorizedVehicle: String? = null,

    val registrationType: String? = null,
    @Json(name = "state") val state: String? = null,

    @Json(name = "district") val district: String? = null,

    @Json(name = "block") val block: String? = null,

    @Json(name = "villages") val village: String? = null,

    @Json(name = "serverUpdatedStatus") val serverUpdatedStatus: Int = 0,

    @Json(name = "createdBy") val createdBy: String? = null,

    @Json(name = "createdDate") val createdDate: String? = null,

    @Json(name = "updatedBy") val updatedBy: String? = null,

    @Json(name = "updatedDate") val updatedDate: String? = null,

    @Json(name = "ProviderServiceMapID") val providerServiceMapID: Int,

//    @Json(name = "VanID") val VanID: Int,

    @Json(name = "Processed") val Processed: String,

    @Json(name = "Countyid") val Countyid: Int,

    @Json(name = "stateid") val stateid: Int = 0,

    @Json(name = "districtid") val districtid: Int = 0,

    @Json(name = "districtname") val districtname: String? = null,

    @Json(name = "blockid") val blockid: Int = 0,

    @Json(name = "villageid") val villageid: Int = 0,
    val isDeactivate: Boolean =false

    )

data class HouseholdBasicCache(
    @Embedded
    val household: HouseholdCache,
    val numMembers: Int,
) {
    fun asBasicDomainModel(): HouseHoldBasicDomain {
        return HouseHoldBasicDomain(
            hhId = household.householdId,
            headName = household.family?.familyHeadName ?: "Not Available",
            contactNumber = household.family?.familyHeadPhoneNo?.toString() ?: "Not Available",
            headSurname = household.family?.familyName ?: "Not Available",
            headFullName = "${household.family?.familyHeadName} ${household.family?.familyName ?: ""}",
            numMembers = numMembers,
            isDeactivate = household.isDeactivate

        )
    }
}

data class HouseHoldBasicDomain(
    val hhId: Long,
    val headName: String,
    val headSurname: String,
    val contactNumber: String,
    val headFullName: String = "$headName $headSurname",
    val numMembers: Int,
    val isDeactivate: Boolean =false
)