package org.paradrops.encounter.data

import org.paradrops.encounter.view.EncounterInfo

class EncounterInfoDataStore {
    private val KeyEncounterNameList = "KeyEncounterInfoList"
    private val KeyEncounterDateList = "KeyEncounterDateList"

    fun get() : List<EncounterInfo> {
        val result = mutableListOf<EncounterInfo>()

        val dateList = getEncounterDateList()
        getEncounterNameList().forEachIndexed { position, name ->
            result.add(EncounterInfo(name, dateList[position]))
        }
        return result
    }

    fun save(encounterInfo: EncounterInfo) {
        val nameList = getEncounterNameList().toMutableList()
        nameList.add(encounterInfo.name)

        val dateList = getEncounterDateList().toMutableList()
        dateList.add(encounterInfo.date)

        SharedPreferencesWrapper.save(KeyEncounterNameList, nameList.joinToString(separator = ","))
        SharedPreferencesWrapper.save(KeyEncounterDateList, dateList.joinToString(separator = ","))
    }

    private fun getEncounterNameList() : List<String> {
        return SharedPreferencesWrapper.get(KeyEncounterNameList).split(delimiters = ",")
    }

    private fun getEncounterDateList() : List<String> {
        return SharedPreferencesWrapper.get(KeyEncounterDateList).split(delimiters = ",")
    }
}